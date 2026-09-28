$ErrorActionPreference = 'Stop'

$repo = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..')).Path
$assets = Join-Path $repo 'src\main\resources\assets\poptartcore'
$data = Join-Path $repo 'src\main\resources\data'
$names = @('budding_flesh', 'fresh_flesh', 'flesh', 'rotten_flesh', 'lifebud')

function Require-File([string] $path) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Missing flesh asset: $path"
    }
}

function Read-Json([string] $path) {
    Require-File $path
    return Get-Content -LiteralPath $path -Raw | ConvertFrom-Json -AsHashtable
}

foreach ($name in $names) {
    $blockstate = Read-Json (Join-Path $assets "blockstates\$name.json")
    foreach ($variant in $blockstate.variants.GetEnumerator()) {
        $model = [string] $variant.Value.model
        if (-not $model.StartsWith('poptartcore:block/flesh/')) {
            throw "Unexpected model for $name : $model"
        }
        Require-File (Join-Path $assets ('models\' + $model.Substring('poptartcore:'.Length) + '.json'))
    }

    if ($name -ne 'lifebud') {
        $item = Read-Json (Join-Path $assets "models\item\$name.json")
        if ($item.parent -ne "poptartcore:block/flesh/$name") {
            throw "Unexpected item model parent for $name"
        }
    }
}

$blockModels = Get-ChildItem -LiteralPath (Join-Path $assets 'models\block\flesh') -Filter '*.json'
foreach ($modelFile in $blockModels) {
    $model = Read-Json $modelFile.FullName
    foreach ($texture in $model.textures.GetEnumerator()) {
        $id = [string] $texture.Value
        if (-not $id.StartsWith('poptartcore:block/flesh/')) {
            throw "Unexpected texture in $($modelFile.Name): $id"
        }
        Require-File (Join-Path $assets ('textures\' + $id.Substring('poptartcore:'.Length) + '.png'))
    }
}

$lifebud = Read-Json (Join-Path $assets 'blockstates\lifebud.json')
if (-not $lifebud.variants.'rooted=false' -or -not $lifebud.variants.'rooted=true') {
    throw 'Lifebud must have both rooted model variants'
}
if (Test-Path -LiteralPath (Join-Path $assets 'models\item\lifebud.json')) {
    throw 'Lifebud must not have an item model'
}

$loot = Read-Json (Join-Path $data 'poptartcore\loot_table\blocks\lifebud.json')
if ($loot.pools[0].entries[0].name -ne 'poptartcore:lifegem') {
    throw 'Lifebud must drop Lifegem'
}

$hoeTag = Read-Json (Join-Path $data 'minecraft\tags\block\mineable\hoe.json')
foreach ($name in $names | Where-Object { $_ -ne 'lifebud' }) {
    if ("poptartcore:$name" -notin $hoeTag.values) {
        throw "Missing hoe mining tag for $name"
    }
}

foreach ($sound in @('breathing1', 'breathing2')) {
    Require-File (Join-Path $assets "sounds\flesh_geode\$sound.ogg")
}

$worldgen = Join-Path $data 'poptartcore\worldgen'
$biomeModifiers = Join-Path $data 'poptartcore\neoforge\biome_modifier'
foreach ($entry in @(
    @{ Name = 'flesh_geode'; Modifier = 'add_flesh_geode'; Biome = '#minecraft:is_overworld'; Chance = 40; Pool = 'minecraft:water'; Casing = 'minecraft:tuff'; Air = $null },
    @{ Name = 'nether_flesh_geode'; Modifier = 'add_flesh_geode_nether'; Biome = '#minecraft:is_nether'; Chance = 100; Pool = $null; Casing = $null; Air = 'nomansland:toxic_gas' }
)) {
    $name = $entry.Name
    $configured = Read-Json (Join-Path $worldgen "configured_feature\$name.json")
    $placed = Read-Json (Join-Path $worldgen "placed_feature\$name.json")
    $modifier = Read-Json (Join-Path $biomeModifiers "$($entry.Modifier).json")

    if ($configured.type -ne 'poptartcore:flesh_geode' -or
            $configured.config.shell.state.Name -ne 'poptartcore:flesh' -or
            $configured.config.lining.state.Name -ne 'poptartcore:fresh_flesh' -or
            $configured.config.gem.state.Name -ne 'poptartcore:lifebud') {
        throw "Configured feature $name has an incorrect block or feature ID"
    }
    if ($configured.config.pool.state.Name -ne $entry.Pool -or
            $configured.config.casing.state.Name -ne $entry.Casing -or
            $configured.config.air.state.Name -ne $entry.Air) {
        throw "Configured feature $name differs from Wayfarer's interior or casing"
    }
    if ($placed.feature -ne "poptartcore:$name" -or
            $placed.placement[0].type -ne 'minecraft:rarity_filter' -or
            $placed.placement[0].chance -ne $entry.Chance -or
            $placed.placement[1].type -ne 'minecraft:in_square' -or
            $placed.placement[2].height.min_inclusive.above_bottom -ne 16 -or
            $placed.placement[2].height.max_inclusive.absolute -ne 48 -or
            $placed.placement[3].type -ne 'minecraft:biome') {
        throw "Placed feature $name has incorrect placement rules"
    }
    if ($modifier.type -ne 'neoforge:add_features' -or
            $modifier.biomes -ne $entry.Biome -or
            $modifier.features[0] -ne "poptartcore:$name" -or
            $modifier.step -ne 'local_modifications') {
        throw "Biome modifier for $name is incorrect"
    }
}

$tab = Get-Content -LiteralPath (Join-Path $repo 'src\main\java\dev\poptartking\poptartcore\registry\PoptartCoreTabs.java') -Raw
$order = [regex]::Match($tab, 'CREATIVE_TAB_ORDER = List\.of\((.*?)\);', 'Singleline').Groups[1].Value
$entries = @([regex]::Matches($order, '"([^"]+)"') | ForEach-Object { $_.Groups[1].Value })
foreach ($name in $names | Where-Object { $_ -ne 'lifebud' }) {
    if (@($entries | Where-Object { $_ -eq $name }).Count -ne 1) {
        throw "Creative tab must list $name exactly once"
    }
}
if ('lifebud' -in $entries) {
    throw 'Lifebud must not appear in the creative tab'
}

Write-Output 'Flesh assets, Lifebud drop, item visibility, and both Wayfarer-style geode placements verified.'
