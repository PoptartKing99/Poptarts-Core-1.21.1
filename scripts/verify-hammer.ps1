param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot)
)

$ErrorActionPreference = "Stop"

$requiredFiles = @(
    "src/main/java/dev/poptartking/poptartcore/item/HammerItem.java",
    "src/main/resources/assets/poptartcore/models/item/hammer.json",
    "src/main/resources/assets/poptartcore/textures/item/hammer.png",
    "src/main/resources/data/c/tags/item/tools/hammers.json",
    "src/main/resources/data/poptartcore/tags/item/hammer_repair_materials.json"
)

foreach ($relativePath in $requiredFiles) {
    if (-not (Test-Path -LiteralPath (Join-Path $ProjectRoot $relativePath) -PathType Leaf)) {
        throw "Missing hammer file: $relativePath"
    }
}

$hammerTag = Get-Content -LiteralPath (Join-Path $ProjectRoot "src/main/resources/data/c/tags/item/tools/hammers.json") -Raw |
    ConvertFrom-Json
if ($hammerTag.replace -ne $false -or $hammerTag.values -notcontains "poptartcore:hammer") {
    throw "The shared c:tools/hammers tag is incorrect."
}

$repairTag = Get-Content -LiteralPath (Join-Path $ProjectRoot "src/main/resources/data/poptartcore/tags/item/hammer_repair_materials.json") -Raw |
    ConvertFrom-Json
if ($repairTag.values -notcontains "supplementaries:slidy_block") {
    throw "The hammer repair tag must contain supplementaries:slidy_block."
}

$itemsSource = Get-Content -LiteralPath (Join-Path $ProjectRoot "src/main/java/dev/poptartking/poptartcore/registry/PoptartCoreItems.java") -Raw
if ($itemsSource -notmatch '"hammer"' -or $itemsSource -notmatch '\.durability\(720\)') {
    throw "The 720-durability hammer registration is missing."
}

$bloomSource = Get-Content -LiteralPath (Join-Path $ProjectRoot "src/main/java/dev/poptartking/poptartcore/bloomery/IronBloomBlock.java") -Raw
if ($bloomSource -notmatch "PoptartCoreTags\.HAMMERS" -or $bloomSource -notmatch "IRON_NUGGET") {
    throw "Iron blooms are not connected to the shared hammer tag."
}

Write-Output "Hammer verification passed: item assets, durability, compatibility tag, repair material, and iron-bloom processing are connected."
