param([string]$JarPath = 'build/libs/poptartcore-0.0.1.jar')
$ErrorActionPreference = 'Stop'
$additionRoot = (Resolve-Path "$PSScriptRoot/../..").Path
$reference = Join-Path (Split-Path $additionRoot) 'decompiled-review-2026-10-19/wayfarer_core-0.1.0/sources'
foreach ($pair in @(
    @('block/rubber_block.png','block/rubber_block.png'),
    @('block/quartz_crystal.png','block/quartz_crystal.png'),
    @('block/quartz_crystal_side.png','block/quartz_crystal_side.png'),
    @('item/rings/magnet_ring.png','item/magnet_ring.png'))) {
    if ((Get-FileHash "$reference/assets/wayfarer/textures/$($pair[0])").Hash -ne
        (Get-FileHash "$additionRoot/src/main/resources/assets/poptartcore/textures/$($pair[1])").Hash) {
        throw "Source texture mismatch: $($pair[1])"
    }
}
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::OpenRead((Join-Path $additionRoot $JarPath))
try {
    foreach ($path in @(
        'assets/poptartcore/blockstates/rubber_block.json',
        'assets/poptartcore/blockstates/quartz_crystal.json',
        'assets/poptartcore/models/block/quartz_crystal.json',
        'assets/poptartcore/models/item/quartz_crystal.json',
        'assets/poptartcore/models/item/magnet_ring.json',
        'data/poptartcore/loot_table/blocks/rubber_block.json',
        'data/poptartcore/loot_table/blocks/quartz_crystal.json')) {
        if (!$archive.GetEntry($path)) { throw "Missing packaged resource: $path" }
    }
} finally { $archive.Dispose() }
Write-Output 'Wayfarer texture hashes and packaged block/item resources verified.'
