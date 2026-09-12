$ErrorActionPreference = 'Stop'

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$itemsFile = Join-Path $projectRoot 'src\main\java\dev\poptartking\poptartcore\registry\PoptartCoreItems.java'
$tiersFile = Join-Path $projectRoot 'src\main\java\dev\poptartking\poptartcore\registry\PoptartCoreTiers.java'
$tabsFile = Join-Path $projectRoot 'src\main\java\dev\poptartking\poptartcore\registry\PoptartCoreTabs.java'
$langFile = Join-Path $projectRoot 'src\main\resources\assets\poptartcore\lang\en_us.json'
$tools = @('sword', 'pickaxe', 'axe', 'shovel', 'knife')

$items = Get-Content -Raw -LiteralPath $itemsFile
$tiers = Get-Content -Raw -LiteralPath $tiersFile
$tabs = Get-Content -Raw -LiteralPath $tabsFile
$lang = Get-Content -Raw -LiteralPath $langFile | ConvertFrom-Json

foreach ($expected in @('INCORRECT_FOR_DIAMOND_TOOL', '1250', '8.0F', '3.0F', '15', 'STEEL_INGOT')) {
    if ($tiers -notmatch $expected) {
        throw "Steel tier is missing expected value: $expected"
    }
}

foreach ($tool in $tools) {
    $upperName = "STEEL_$($tool.ToUpperInvariant())"
    $itemId = "steel_$tool"
    if ($items -notmatch "\b$upperName\b" -or $items -notmatch ('"' + $itemId + '"')) {
        throw "Missing item registration for $itemId"
    }
    if ($tabs -notmatch "PoptartCoreItems\.$upperName") {
        throw "Missing creative-tab entry for $itemId"
    }
    if (-not $lang.PSObject.Properties["item.poptartcore.$itemId"]) {
        throw "Missing language entry for $itemId"
    }

    foreach ($relativePath in @(
        "src\main\resources\assets\poptartcore\models\item\$itemId.json",
        "src\main\resources\assets\poptartcore\textures\item\$itemId.png",
        "src\main\resources\data\poptartcore\recipe\$itemId.json"
    )) {
        if (-not (Test-Path -LiteralPath (Join-Path $projectRoot $relativePath))) {
            throw "Missing $relativePath"
        }
    }
}

$tagFiles = @(
    'src\main\resources\data\minecraft\tags\item\swords.json',
    'src\main\resources\data\minecraft\tags\item\pickaxes.json',
    'src\main\resources\data\minecraft\tags\item\axes.json',
    'src\main\resources\data\minecraft\tags\item\shovels.json',
    'src\main\resources\data\farmersdelight\tags\item\tools\knives.json'
)
foreach ($relativePath in $tagFiles) {
    $contents = Get-Content -Raw -LiteralPath (Join-Path $projectRoot $relativePath)
    if ($contents -notmatch 'poptartcore:steel_') {
        throw "Steel tool is missing from $relativePath"
    }
}

Write-Host 'Steel tool tier, registrations, resources, recipes, and tags are complete.'
