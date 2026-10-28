param([string]$Reference = "$PSScriptRoot/../../../../decompiled-review-2026-10-24/wayward-neoforge-1.0.0+1.21.1/assets/wayward")
$ErrorActionPreference = 'Stop'
$destination = Join-Path (Resolve-Path "$PSScriptRoot/../../..").Path 'src/main/resources/assets/poptartcore'
foreach ($path in @('models/block/backpack.json', 'models/block/backpack_straps.json', 'models/item/backpack.json', 'models/block/rucksack.json', 'models/block/rucksack_straps.json', 'models/item/rucksack.json')) {
    $target = Join-Path $destination $path
    New-Item -ItemType Directory -Force -Path (Split-Path $target) | Out-Null
    $text = (Get-Content -Raw (Join-Path $Reference $path)).Replace('wayward:', 'poptartcore:')
    [IO.File]::WriteAllText($target, $text, [Text.UTF8Encoding]::new($false))
}
foreach ($id in @('backpack','rucksack')) {
    $texture = Join-Path $destination "textures/block/$id.png"
    Copy-Item -LiteralPath (Join-Path $Reference "textures/block/$id.png") -Destination $texture
    if ((Get-FileHash $texture).Hash -ne (Get-FileHash (Join-Path $Reference "textures/block/$id.png")).Hash) {
        throw "$id texture differs from reference"
    }
}
& "$PSScriptRoot/tune-backpack-display.ps1"
& "$PSScriptRoot/import-attachment-assets.ps1" -Reference $Reference
Write-Output 'Imported backpack, rucksack, and existing-item attachment assets; applied local poses and verified textures.'
