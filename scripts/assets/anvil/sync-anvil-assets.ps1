param([string]$Reference = "$PSScriptRoot/../../../../decompiled-review-2026-10-19/wayfarer_core-0.1.0/sources")
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path "$PSScriptRoot/../../..").Path
$resources = "$repo/src/main/resources"
$names = @('anvil','chipped_anvil','damaged_anvil','primitive_anvil','chipped_primitive_anvil','damaged_primitive_anvil')
function Write-AnvilJson($relative, $value) {
    $path = Join-Path $resources $relative
    [IO.Directory]::CreateDirectory((Split-Path $path)) | Out-Null
    [IO.File]::WriteAllText($path, ($value | ConvertTo-Json -Depth 80) + "`n")
}
function Copy-AnvilJson($source, $target) {
    $text = Get-Content -LiteralPath "$Reference/$source" -Raw
    foreach ($name in $names) { $text = $text.Replace(('"wayfarer:block/' + $name + '"'), ('"poptartcore:block/anvils/' + $name + '"')) }
    $text = $text.Replace('wayfarer:block/mortar/mortar_particle','poptartcore:block/anvils/mortar_particle').Replace('wayfarer:block/anvils/','poptartcore:block/anvils/').Replace('wayfarer_core:', 'poptartcore:')
    Write-AnvilJson $target ($text | ConvertFrom-Json -AsHashtable)
}
foreach ($name in $names) {
    Copy-AnvilJson "assets/wayfarer_core/blockstates/$name.json" "assets/poptartcore/blockstates/$name.json"
    Copy-AnvilJson "assets/wayfarer_core/models/item/$name.json" "assets/poptartcore/models/item/$name.json"
    Copy-AnvilJson "data/wayfarer_core/loot_table/blocks/$name.json" "data/poptartcore/loot_table/blocks/$name.json"
    if (Test-Path "$Reference/assets/wayfarer/models/block/$name.json") {
        Copy-AnvilJson "assets/wayfarer/models/block/$name.json" "assets/poptartcore/models/block/anvils/$name.json"
    }
}
[IO.Directory]::CreateDirectory("$resources/assets/poptartcore/textures/block/anvils") | Out-Null
Copy-Item "$Reference/assets/wayfarer/textures/block/anvils/*.png" "$resources/assets/poptartcore/textures/block/anvils/"
Copy-Item "$Reference/assets/wayfarer/textures/block/mortar/mortar_particle.png" "$resources/assets/poptartcore/textures/block/anvils/mortar_particle.png"
foreach ($relative in @('textures/gui/anvil.png','textures/gui/sprites/anvil_textbox.png')) {
    [IO.Directory]::CreateDirectory((Split-Path "$resources/assets/poptartcore/$relative")) | Out-Null
    Copy-Item -LiteralPath "$Reference/assets/wayfarer/$relative" -Destination "$resources/assets/poptartcore/$relative"
}
$lang = Get-Content "$resources/assets/poptartcore/lang/en_us.json" -Raw | ConvertFrom-Json -AsHashtable
$referenceLang = Get-Content "$Reference/assets/wayfarer_core/lang/en_us.json" -Raw | ConvertFrom-Json -AsHashtable
$duplicateItemNames = @($names | ForEach-Object { "item.wayfarer_core.$_" })
foreach ($key in $referenceLang.Keys) {
    if ($key -in $duplicateItemNames) { continue }
    if ($key -match 'anvil|menu.message.wayfarer_core.(needs_hammer|upgrade_cost|not_upgradeable|upgraded)|modification.wayfarer_core|message.wayfarer_core.sticky') {
        $lang[$key.Replace('wayfarer_core','poptartcore')] = $referenceLang[$key]
    }
}
$lang['item.poptartcore.worn_out'] = 'Worn Out - repair to restore function'
Write-AnvilJson 'assets/poptartcore/lang/en_us.json' $lang
$pickaxePath = "$repo/src/generated/resources/data/minecraft/tags/block/mineable/pickaxe.json"
$pickaxe = Get-Content $pickaxePath -Raw | ConvertFrom-Json -AsHashtable
foreach ($name in $names) { if ("poptartcore:$name" -notin $pickaxe.values) { $pickaxe.values += "poptartcore:$name" } }
# Targeted catalog output, matching CatalogBlockTagProvider, without unrelated third-party datagen.
[IO.File]::WriteAllText($pickaxePath, ($pickaxe | ConvertTo-Json -Depth 8) + "`n")
# Tags are intentionally separate files so later eligibility edits survive asset synchronization.
Write-Output 'Copied exact Wayfarer anvil models, textures, GUI, loot and language. No recipes or eligibility tags changed.'
