param([string]$ReferenceTextures = "C:/Users/badas/AppData/Roaming/PrismLauncher/instances/Final Pack 1.21.1 v0.5/minecraft/resourcepacks/Aether_Excalibur_Support/assets/aether/textures")
$ErrorActionPreference = 'Stop'
$capeRepo = (Resolve-Path "$PSScriptRoot/../..").Path
foreach ($capeColor in @('white', 'yellow', 'blue', 'red')) {
    foreach ($capeAsset in @(
        @{ Source = "item/accessories/${capeColor}_cape.png"; Target = "textures/item/${capeColor}_cape.png" },
        @{ Source = "models/accessory/capes/${capeColor}_cape_accessory.png"; Target = "textures/entity/${capeColor}_cape.png" }
    )) {
        $capeSource = Join-Path $ReferenceTextures $capeAsset.Source
        $capeTarget = Join-Path $capeRepo "src/main/resources/assets/poptartcore/$($capeAsset.Target)"
        if ((Get-FileHash -LiteralPath $capeSource).Hash -ne (Get-FileHash -LiteralPath $capeTarget).Hash) {
            throw "Cape texture differs from supplied asset: $capeTarget"
        }
    }
    $capeModel = Get-Content -Raw "$capeRepo/src/main/resources/assets/poptartcore/models/item/${capeColor}_cape.json" | ConvertFrom-Json
    if ($capeModel.textures.layer0 -ne "poptartcore:item/${capeColor}_cape") { throw "Wrong item texture for $capeColor" }
}
Write-Output 'All eight cape textures match the supplied assets; four item models reference the correct textures.'
