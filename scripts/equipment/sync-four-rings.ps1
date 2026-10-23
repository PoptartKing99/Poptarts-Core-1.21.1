param([string]$Reference = "$PSScriptRoot/../../../decompiled-review-2026-10-19/wayfarer_core-0.1.0/sources/assets/wayfarer/textures/item/rings")
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path "$PSScriptRoot/../..").Path
$assets = "$repo/src/main/resources/assets/poptartcore"
$rings = [ordered]@{
    bulwark_ring = @('Bulwark Ring', 'Reduces explosion damage by 20% and grants +5% knockback resistance.')
    ring_of_the_rat = @('Ring of the Rat', 'Move 15% faster while crouching; mobs detect you 20% less easily.')
    sulfur_gemstone_ring = @('Sulfur Gemstone Ring', 'Reduces corrosion damage by 30%.')
    ring_of_silence = @('Ring of Silence', 'Turns the wearer intangible when not moving.')
}
foreach ($id in $rings.Keys) {
    Copy-Item -LiteralPath "$Reference/$id.png" -Destination "$assets/textures/item/$id.png"
    $model = @{parent='minecraft:item/generated'; textures=@{layer0="poptartcore:item/$id"}} | ConvertTo-Json -Depth 4
    [IO.File]::WriteAllText("$assets/models/item/$id.json", $model + "`n")
}
$langPath = "$assets/lang/en_us.json"
$lang = Get-Content -LiteralPath $langPath -Raw | ConvertFrom-Json -AsHashtable
foreach ($id in $rings.Keys) {
    $lang["item.poptartcore.$id"] = $rings[$id][0]
    $lang["item.poptartcore.$id.desc"] = $rings[$id][1]
}
[IO.File]::WriteAllText($langPath, ($lang | ConvertTo-Json -Depth 20) + "`n")
$tagPath = "$repo/src/main/resources/data/poptartcore/tags/item/equipment/rings.json"
$tag = Get-Content -LiteralPath $tagPath -Raw | ConvertFrom-Json
$tag.values = @($tag.values) + @($rings.Keys | ForEach-Object { "poptartcore:$_" } | Where-Object { $_ -notin $tag.values })
[IO.File]::WriteAllText($tagPath, ($tag | ConvertTo-Json -Depth 4) + "`n")
# Test resources shadow the production file; retain the synthetic stone fixture plus every real ring.
$testTag = @{replace=$false; values=@('minecraft:stone') + @($tag.values)}
[IO.File]::WriteAllText("$repo/src/gameTest/resources/data/poptartcore/tags/item/equipment/rings.json", ($testTag | ConvertTo-Json -Depth 4) + "`n")
foreach ($id in $rings.Keys) {
    if ((Get-FileHash "$Reference/$id.png").Hash -ne (Get-FileHash "$assets/textures/item/$id.png").Hash) { throw "Texture mismatch: $id" }
    if (Test-Path "$repo/src/main/resources/data/poptartcore/recipe/$id.json") { throw "Unexpected recipe: $id" }
}
Write-Output 'Four Wayfarer ring textures copied exactly; models, names, tooltips and ring-slot tags synchronized. No recipes.'
