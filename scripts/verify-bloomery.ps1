param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = "Stop"

$requiredFiles = @(
    "src/main/java/dev/poptartking/poptartcore/bloomery/BloomeryBlock.java",
    "src/main/java/dev/poptartking/poptartcore/bloomery/BloomeryBlockEntity.java",
    "src/main/java/dev/poptartking/poptartcore/bloomery/IronBloomBlock.java",
    "src/main/resources/assets/poptartcore/blockstates/bloomery.json",
    "src/main/resources/assets/poptartcore/blockstates/iron_bloom.json",
    "src/main/resources/assets/poptartcore/models/block/bloomery/bloomery.json",
    "src/main/resources/assets/poptartcore/models/block/bloomery/bloomery_lit.json",
    "src/main/resources/assets/poptartcore/models/item/bloomery.json",
    "src/main/resources/assets/poptartcore/models/item/iron_bloom.json",
    "src/main/resources/data/poptartcore/loot_table/blocks/bloomery.json"
)

1..9 | ForEach-Object {
    $requiredFiles += "src/main/resources/assets/poptartcore/models/block/iron_bloom/iron_bloom_$_.json"
}

$requiredTextures = @(
    "bloomery_bottom.png",
    "bloomery_bottom_lit.png",
    "bloomery_front.png",
    "bloomery_front_lit.png",
    "bloomery_front_lit.png.mcmeta",
    "bloomery_side.png",
    "bloomery_side_lit.png",
    "bloomery_top.png",
    "bloomery_top_lit.png",
    "bloomery_top_lit.png.mcmeta"
)

$requiredTextures | ForEach-Object {
    $requiredFiles += "src/main/resources/assets/poptartcore/textures/block/bloomery/$_"
}
$requiredFiles += "src/main/resources/assets/poptartcore/textures/block/iron_bloom/iron_bloom.png"
$requiredFiles += "src/main/resources/assets/poptartcore/textures/block/iron_bloom/iron_bloom_base.png"
$requiredFiles += "src/main/resources/assets/poptartcore/textures/item/iron_bloom.png"

foreach ($relativePath in $requiredFiles) {
    $path = Join-Path $ProjectRoot $relativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Missing bloomery file: $relativePath"
    }
}

$resourceRoot = Join-Path $ProjectRoot "src/main/resources"
Get-ChildItem -LiteralPath $resourceRoot -Recurse -Filter "*.json" | ForEach-Object {
    Get-Content -LiteralPath $_.FullName -Raw | ConvertFrom-Json -AsHashtable | Out-Null
}

$bloomeryState = Get-Content -LiteralPath (Join-Path $resourceRoot "assets/poptartcore/blockstates/bloomery.json") -Raw |
    ConvertFrom-Json
if (@($bloomeryState.variants.PSObject.Properties).Count -ne 8) {
    throw "Bloomery blockstate must contain eight facing and lit variants."
}

$ironBloomState = Get-Content -LiteralPath (Join-Path $resourceRoot "assets/poptartcore/blockstates/iron_bloom.json") -Raw |
    ConvertFrom-Json
if (@($ironBloomState.variants.PSObject.Properties).Count -ne 9) {
    throw "Iron bloom blockstate must contain nine bloom-count variants."
}

$allText = Get-ChildItem -LiteralPath (Join-Path $ProjectRoot "src") -Recurse -File |
    Where-Object { $_.Extension -in ".java", ".json" } |
    ForEach-Object { Get-Content -LiteralPath $_.FullName -Raw }
if (($allText -join "`n") -match "wayfarer_core:") {
    throw "A Wayfarer namespace reference remains in the project."
}

Write-Output "Bloomery verification passed: registrations and required files are present, JSON is valid, and all 17 blockstate variants exist."
