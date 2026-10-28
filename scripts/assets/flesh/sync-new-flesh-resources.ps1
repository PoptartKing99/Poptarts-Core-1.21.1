param([string]$Reference = "$PSScriptRoot/../../../../decompiled-review-2026-10-19/wayfarer_core-0.1.0/sources")
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path "$PSScriptRoot/../../..").Path
$main = "$root/src/main/resources"
$referenceRoot = (Resolve-Path $Reference).Path
function Write-Resource($relative, $text) {
    $path = Join-Path $main $relative
    New-Item -ItemType Directory -Force -Path (Split-Path $path) | Out-Null
    [IO.File]::WriteAllText($path, $text, [Text.UTF8Encoding]::new($false))
}
function Convert-Reference($text) {
    $text.Replace('wayfarer:block/','poptartcore:block/flesh/').Replace('wayfarer:item/','poptartcore:item/').Replace('wayfarer_core:','poptartcore:')
}
$names = @('flesh','living_flesh','rotten_flesh','budding_flesh','lifebud','lifebud_rooted',
    'flesh_tendrils','living_flesh_tendrils','rotten_flesh_tendrils','ulcer')
foreach ($name in $names) {
    $model = "$referenceRoot/assets/wayfarer/models/block/$name.json"
    if (Test-Path $model) { Write-Resource "assets/poptartcore/models/block/flesh/$name.json" (Convert-Reference (Get-Content -Raw $model)) }
    $state = "$referenceRoot/assets/wayfarer_core/blockstates/$name.json"
    if (Test-Path $state) { Write-Resource "assets/poptartcore/blockstates/$name.json" (Convert-Reference (Get-Content -Raw $state)) }
    $texture = "$referenceRoot/assets/wayfarer/textures/block/$name.png"
    if (Test-Path $texture) { Copy-Item -LiteralPath $texture -Destination "$main/assets/poptartcore/textures/block/flesh/$name.png" }
    if ($name -notin @('lifebud','lifebud_rooted')) {
        $item = "$referenceRoot/assets/wayfarer_core/models/item/$name.json"
        if (Test-Path $item) { Write-Resource "assets/poptartcore/models/item/$name.json" (Convert-Reference (Get-Content -Raw $item)) }
    }
}
Copy-Item -LiteralPath "$referenceRoot/assets/wayfarer/textures/block/lifebud_particle.png" -Destination "$main/assets/poptartcore/textures/block/flesh/lifebud_particle.png"
Copy-Item -LiteralPath "$referenceRoot/assets/wayfarer/textures/item/lifesalt.png" -Destination "$main/assets/poptartcore/textures/item/lifesalt.png"
Write-Resource 'assets/poptartcore/models/item/lifesalt.json' '{"parent":"minecraft:item/generated","textures":{"layer0":"poptartcore:item/lifesalt"}}'
foreach ($name in @('flesh','living_flesh','rotten_flesh','ulcer','lifebud')) {
    $loot = (Convert-Reference (Get-Content -Raw "$referenceRoot/data/wayfarer_core/loot_table/blocks/$name.json")) | ConvertFrom-Json -AsHashtable
    foreach ($pool in $loot.pools) {
        foreach ($entry in $pool.entries) {
            if ($entry.name -eq 'brewinandchewin:innards') {
                $entry.type = 'minecraft:empty'
                $entry.Remove('name')
            }
        }
    }
    Write-Resource "data/poptartcore/loot_table/blocks/$name.json" ($loot | ConvertTo-Json -Depth 32)
}
$foods = Get-Content -Raw "$referenceRoot/data/wayfarer_core/data_maps/item/ulcer_food.json" | ConvertFrom-Json -AsHashtable
$foods.values.Remove('brewinandchewin:innards')
Write-Resource 'data/poptartcore/data_maps/item/ulcer_food.json' ($foods | ConvertTo-Json -Depth 32)
Write-Resource 'data/poptartcore/tags/block/flesh.json' '{"replace":false,"values":["poptartcore:flesh","poptartcore:living_flesh","poptartcore:rotten_flesh","poptartcore:budding_flesh"]}'
Write-Output 'Copied and namespaced Wayfarer flesh assets, loot and food data. Growth eligibility is configured separately.'
