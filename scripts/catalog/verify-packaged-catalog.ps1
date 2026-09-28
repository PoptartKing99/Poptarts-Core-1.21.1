#requires -Version 7.0
param(
    [string]$JarPath
)

$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..')).Path
if (-not $JarPath) {
    $jars = @(Get-ChildItem -LiteralPath (Join-Path $projectRoot 'build\libs') -Filter 'poptartcore-*.jar' -File | Where-Object { $_.Name -notmatch '(sources|javadoc)' })
    if ($jars.Count -ne 1) { throw "Expected one built mod JAR in build/libs, found $($jars.Count). Pass -JarPath explicitly." }
    $JarPath = $jars[0].FullName
}
$JarPath = (Resolve-Path -LiteralPath $JarPath).Path

$blockSource = Get-Content -Raw -LiteralPath (Join-Path $projectRoot 'src\main\java\dev\poptartking\poptartcore\registry\PoptartCoreBlocks.java')
$itemSource = Get-Content -Raw -LiteralPath (Join-Path $projectRoot 'src\main\java\dev\poptartking\poptartcore\registry\PoptartCoreItems.java')
$blockPattern = '(?s)CatalogBlockDefinition<[^>]+>\s+\w+\s*=\s*PoptartCatalog\.block\(\s*"(?<id>[^"]+)"(?<body>.*?);'
$itemPattern = '(?s)CatalogItemDefinition<[^>]+>\s+\w+\s*=\s*PoptartCatalog\.(?:item|simpleItem)\(\s*"(?<id>[^"]+)"(?<body>.*?);'
$blocks = [regex]::Matches($blockSource, $blockPattern)
$items = [regex]::Matches($itemSource, $itemPattern)
if ($blocks.Count -eq 0 -or $items.Count -eq 0) {
    throw 'Could not find catalog declarations; update the verifier for the current registry format.'
}

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::OpenRead($JarPath)
try {
    $entries = @{}
    foreach ($entry in $archive.Entries) { $entries[$entry.FullName] = $entry }
    $errors = [System.Collections.Generic.List[string]]::new()
    $visitedModels = [System.Collections.Generic.HashSet[string]]::new()

    function Read-JsonEntry([string]$path) {
        $stream = $entries[$path].Open()
        $reader = [System.IO.StreamReader]::new($stream)
        try { return (ConvertFrom-Json -AsHashtable -InputObject $reader.ReadToEnd()) }
        catch { throw "Cannot parse $path`: $($_.Exception.Message)" }
        finally { $reader.Dispose(); $stream.Dispose() }
    }

    function Check-Model([string]$reference, [string]$owner) {
        if ($reference -notmatch '^poptartcore:(.+)$') { return }
        $path = "assets/poptartcore/models/$($Matches[1]).json"
        if (-not $entries.ContainsKey($path)) {
            $errors.Add("$owner refers to missing model $path")
            return
        }
        if (-not $visitedModels.Add($path)) { return }
        $model = Read-JsonEntry $path
        if ($model.parent) { Check-Model ([string]$model.parent) $path }
        if ($model.textures) {
            foreach ($texture in $model.textures.Values) {
                if ($texture -match '^poptartcore:(.+)$') {
                    $texturePath = "assets/poptartcore/textures/$($Matches[1]).png"
                    if (-not $entries.ContainsKey($texturePath)) {
                        $errors.Add("$path refers to missing texture $texturePath")
                    }
                }
            }
        }
    }

    function Check-ModelReferences($node, [string]$owner) {
        if ($node -is [System.Collections.IDictionary]) {
            foreach ($key in $node.Keys) {
                if ($key -eq 'model') { Check-Model ([string]$node[$key]) $owner }
                else { Check-ModelReferences $node[$key] $owner }
            }
        } elseif ($node -is [System.Array]) {
            foreach ($child in $node) { Check-ModelReferences $child $owner }
        }
    }

    $tagEntries = @{}
    foreach ($tool in @('axe', 'pickaxe', 'shovel', 'hoe')) {
        $path = "data/minecraft/tags/block/mineable/$tool.json"
        if ($entries.ContainsKey($path)) { $tagEntries[$tool] = @( (Read-JsonEntry $path).values ) }
        else { $tagEntries[$tool] = @() }
    }
    foreach ($tier in @('stone', 'iron')) {
        $path = "data/minecraft/tags/block/needs_$($tier)_tool.json"
        if ($entries.ContainsKey($path)) { $tagEntries["needs_$tier"] = @((Read-JsonEntry $path).values) }
        else { $tagEntries["needs_$tier"] = @() }
    }

    foreach ($block in $blocks) {
        $id = $block.Groups['id'].Value
        $body = $block.Groups['body'].Value
        $statePath = "assets/poptartcore/blockstates/$id.json"
        $itemPath = "assets/poptartcore/models/item/$id.json"
        $lootPath = "data/poptartcore/loot_table/blocks/$id.json"
        if (-not $entries.ContainsKey($statePath)) {
            $errors.Add("$id is missing blockstate $statePath")
        } else {
            Check-ModelReferences (Read-JsonEntry $statePath) $statePath
        }
        if (-not $entries.ContainsKey($itemPath)) {
            $errors.Add("$id is missing item model $itemPath")
        } else {
            $itemModel = Read-JsonEntry $itemPath
            if ($itemModel.parent) { Check-Model ([string]$itemModel.parent) $itemPath }
            foreach ($texture in @($itemModel.textures.Values)) {
                if ($texture -match '^poptartcore:(.+)$') {
                    $texturePath = "assets/poptartcore/textures/$($Matches[1]).png"
                    if (-not $entries.ContainsKey($texturePath)) { $errors.Add("$itemPath refers to missing texture $texturePath") }
                }
            }
        }
        if ($body -notmatch '\.noLoot\(' -and -not $entries.ContainsKey($lootPath)) {
            $kind = if ($body -match '\.customLoot\(') { 'custom' } else { 'generated' }
            $errors.Add("$id declares $kind loot but $lootPath is missing")
        }
        foreach ($tool in @('axe', 'pickaxe', 'shovel', 'hoe')) {
            $upper = $tool.ToUpperInvariant()
            $method = $tool.Substring(0,1).ToUpperInvariant() + $tool.Substring(1)
            if ($body -match "\.mineableWith$method\(" -or
                ($tool -eq 'pickaxe' -and $body -match '\.requires(Stone|Iron)Tool\(') -or
                $body -match "BlockTags\.MINEABLE_WITH_$upper") {
                if ($tagEntries[$tool] -notcontains "poptartcore:$id") {
                    $errors.Add("$id declares $tool mining but is absent from minecraft:mineable/$tool")
                }
            }
        }
        foreach ($tier in @('stone', 'iron')) {
            $method = $tier.Substring(0,1).ToUpperInvariant() + $tier.Substring(1)
            if ($body -match "\.requires$($method)Tool\(" -and
                $tagEntries["needs_$tier"] -notcontains "poptartcore:$id") {
                $errors.Add("$id requires a $tier tool but is absent from minecraft:needs_$($tier)_tool")
            }
        }
    }

    foreach ($item in $items) {
        $id = $item.Groups['id'].Value
        $path = "assets/poptartcore/models/item/$id.json"
        if (-not $entries.ContainsKey($path)) { $errors.Add("$id is missing item model $path"); continue }
        $model = Read-JsonEntry $path
        if ($model.parent) { Check-Model ([string]$model.parent) $path }
        if ($model.textures) {
            foreach ($texture in $model.textures.Values) {
                if ($texture -match '^poptartcore:(.+)$') {
                    $texturePath = "assets/poptartcore/textures/$($Matches[1]).png"
                    if (-not $entries.ContainsKey($texturePath)) { $errors.Add("$path refers to missing texture $texturePath") }
                }
            }
        }
    }

    if ($errors.Count -gt 0) {
        $errors | ForEach-Object { Write-Host "ERROR: $_" -ForegroundColor Red }
        exit 1
    }
    Write-Host "Verified $($blocks.Count) catalog blocks and $($items.Count) catalog items in $JarPath"
} finally {
    $archive.Dispose()
}
