param([string]$Reference="$PSScriptRoot/../../../../decompiled-review-2026-10-24/wayward-neoforge-1.0.0+1.21.1/assets/wayward")
$ErrorActionPreference='Stop'
$root=(Resolve-Path "$PSScriptRoot/../../..").Path
$assets=Join-Path $root 'src/main/resources/assets/poptartcore'
foreach($kind in @('models','textures')){
    New-Item -ItemType Directory -Force "$assets/$kind/item/backpack_attachments"|Out-Null
}
foreach($name in @('compass','clock_day','clock_night','recovery_compass','heavy_core','bundle','rucksack_banner')){
    $model=(Get-Content "$Reference/models/item/backpack_attachments/$name.json" -Raw).Replace('wayward:','poptartcore:')
    [IO.File]::WriteAllText("$assets/models/item/backpack_attachments/$name.json",$model,[Text.UTF8Encoding]::new($false))
    Copy-Item -LiteralPath "$Reference/textures/item/backpack_attachments/$name.png" -Destination "$assets/textures/item/backpack_attachments/$name.png"
}
foreach($id in @('backpack','rucksack')){
    Copy-Item -LiteralPath "$Reference/textures/gui/container/$id.png" -Destination "$assets/textures/gui/backpack/${id}_attachments_panel.png"
}
Write-Output 'Imported attachment assets for existing items; bedroll and depth-meter assets excluded.'
