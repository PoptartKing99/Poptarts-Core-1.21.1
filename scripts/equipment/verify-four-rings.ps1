param([string]$JarPath = "$PSScriptRoot/../../build/libs/poptartcore-0.0.1.jar")
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path "$PSScriptRoot/../..").Path
$ids = @('bulwark_ring', 'ring_of_the_rat', 'sulfur_gemstone_ring', 'ring_of_silence')
$reference = "$repo/../decompiled-review-2026-10-19/wayfarer_core-0.1.0/sources/assets/wayfarer/textures/item/rings"
Add-Type -AssemblyName System.IO.Compression.FileSystem
$jar = [IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $JarPath).Path)
try {
    $lang = Get-Content "$repo/src/main/resources/assets/poptartcore/lang/en_us.json" -Raw | ConvertFrom-Json -AsHashtable
    $tag = Get-Content "$repo/src/main/resources/data/poptartcore/tags/item/equipment/rings.json" -Raw | ConvertFrom-Json
    $testTag = Get-Content "$repo/src/gameTest/resources/data/poptartcore/tags/item/equipment/rings.json" -Raw | ConvertFrom-Json
    foreach ($value in $tag.values) { if ($value -notin $testTag.values) { throw "Test tag shadows a real ring: $value" } }
    foreach ($id in $ids) {
        if ((Get-FileHash "$reference/$id.png").Hash -ne (Get-FileHash "$repo/src/main/resources/assets/poptartcore/textures/item/$id.png").Hash) { throw "Texture mismatch: $id" }
        if (!$lang["item.poptartcore.$id"] -or !$lang["item.poptartcore.$id.desc"]) { throw "Missing language: $id" }
        if (@($tag.values | Where-Object { $_ -eq "poptartcore:$id" }).Count -ne 1) { throw "Missing or duplicate ring-slot tag: $id" }
        foreach ($path in @("assets/poptartcore/textures/item/$id.png", "assets/poptartcore/models/item/$id.json")) {
            $entry = $jar.GetEntry($path)
            if (!$entry) { throw "Missing packaged resource: $path" }
            $stream = $entry.Open()
            $sha = [Security.Cryptography.SHA256]::Create()
            try { $hash = [Convert]::ToHexString($sha.ComputeHash($stream)) }
            finally { $stream.Dispose(); $sha.Dispose() }
            if ($hash -ne (Get-FileHash "$repo/src/main/resources/$path").Hash) { throw "Stale packaged resource: $path" }
        }
    }
    foreach ($entry in $jar.Entries | Where-Object { $_.FullName -match '^data/[^/]+/recipe/.+\.json$' }) {
        $reader = [IO.StreamReader]::new($entry.Open())
        try { $recipe = $reader.ReadToEnd() } finally { $reader.Dispose() }
        foreach ($id in $ids) { if ($recipe.Contains("poptartcore:$id")) { throw "Unexpected ring recipe: $($entry.FullName)" } }
    }
} finally { $jar.Dispose() }
Write-Output 'All four new rings have exact source textures, names, tooltips, ring-slot tags and packaged models. No recipes.'
