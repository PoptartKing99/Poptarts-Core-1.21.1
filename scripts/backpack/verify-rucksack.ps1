param([string]$JarPath = 'build/libs/poptartcore-0.0.1.jar')
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path "$PSScriptRoot/../..").Path
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::OpenRead((Join-Path $root $JarPath))
try {
    foreach ($path in @(
        'assets/poptartcore/models/block/rucksack.json',
        'assets/poptartcore/models/block/rucksack_placed.json',
        'assets/poptartcore/models/block/rucksack_straps.json',
        'assets/poptartcore/models/block/rucksack_upper.json',
        'assets/poptartcore/models/item/rucksack.json',
        'assets/poptartcore/textures/block/rucksack.png',
        'data/poptartcore/loot_table/blocks/rucksack.json')) {
        if (!$archive.GetEntry($path)) { throw "Missing packaged resource: $path" }
    }
    $reader = [IO.StreamReader]::new($archive.GetEntry('assets/poptartcore/blockstates/rucksack.json').Open())
    try { $states = $reader.ReadToEnd() | ConvertFrom-Json -AsHashtable } finally { $reader.Dispose() }
    if ($states.variants.Count -ne 32) { throw 'Expected four facing variants per half and dye state' }
    foreach ($direction in @('north','east','south','west')) {
      foreach($body in @('false','true')){foreach($flaps in @('false','true')){
        $name=if($body -eq 'true'){if($flaps -eq 'true'){'rucksack_both_dyed'}else{'rucksack_body_dyed'}}else{if($flaps -eq 'true'){'rucksack_dyed'}else{'rucksack'}}
        if ($states.variants["facing=$direction,dyed=$flaps,body_dyed=$body,half=lower"].model -ne "poptartcore:block/${name}_placed") {
            throw 'Placed rucksack must use the grounded model'
        }
        if ($states.variants["facing=$direction,dyed=$flaps,body_dyed=$body,half=upper"].model -ne 'poptartcore:block/rucksack_upper') {
            throw 'Upper half must not duplicate the lower model'
        }
      }}
    }
    Write-Output 'Packaged rucksack models, texture, loot, and all 32 blockstate variants verified.'
} finally { $archive.Dispose() }
