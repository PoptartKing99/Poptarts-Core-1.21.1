param([string]$JarPath = 'build/libs/poptartcore-0.0.1.jar')
$ErrorActionPreference = 'Stop'
$standRepo = (Resolve-Path "$PSScriptRoot/../..").Path
Add-Type -AssemblyName System.IO.Compression.FileSystem
$standArchive = [IO.Compression.ZipFile]::OpenRead((Join-Path $standRepo $JarPath))
try {
    foreach ($entry in @(
        'dev/poptartking/poptartcore/equipment/ArmorStandCape.class',
        'dev/poptartking/poptartcore/equipment/client/ArmorStandCapeLayer.class',
        'dev/poptartking/poptartcore/mixin/equipment/ArmorStandCapeMixin.class')) {
        if (!$standArchive.GetEntry($entry)) { throw "Missing armor stand cape implementation: $entry" }
    }
    $reader = [IO.StreamReader]::new($standArchive.GetEntry('poptartcore.mixins.json').Open())
    try { $config = $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
    if ($config.mixins -notcontains 'equipment.ArmorStandCapeMixin') { throw 'Armor stand interaction/drop mixin is not registered' }
} finally { $standArchive.Dispose() }
$standLog = Get-Content -Raw "$standRepo/build/game-test-run/logs/latest.log"
if ($standLog -notmatch 'All \d+ required tests passed') { throw 'Required server GameTests have not passed' }
Write-Output 'Armor stand cape interaction, drop mixin and renderer are packaged; required server tests passed. Client positioning still needs an in-game check.'
