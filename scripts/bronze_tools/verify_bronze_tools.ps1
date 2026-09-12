$ErrorActionPreference = 'Stop'

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$itemsFile = Join-Path $projectRoot 'src\main\java\dev\poptartking\poptartcore\registry\PoptartCoreItems.java'
$tabsFile = Join-Path $projectRoot 'src\main\java\dev\poptartking\poptartcore\registry\PoptartCoreTabs.java'
$langFile = Join-Path $projectRoot 'src\main\resources\assets\poptartcore\lang\en_us.json'
$tools = @('sword', 'pickaxe', 'axe', 'shovel', 'knife')

$items = Get-Content -Raw -LiteralPath $itemsFile
$tabs = Get-Content -Raw -LiteralPath $tabsFile
$lang = Get-Content -Raw -LiteralPath $langFile | ConvertFrom-Json

foreach ($tool in $tools) {
    $upperName = "BRONZE_$($tool.ToUpperInvariant())"
    $itemId = "bronze_$tool"
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

$requiredTags = @(
    'src\main\resources\data\minecraft\tags\item\swords.json',
    'src\main\resources\data\minecraft\tags\item\pickaxes.json',
    'src\main\resources\data\minecraft\tags\item\axes.json',
    'src\main\resources\data\minecraft\tags\item\shovels.json',
    'src\main\resources\data\farmersdelight\tags\item\tools\knives.json'
)
foreach ($relativePath in $requiredTags) {
    if (-not (Test-Path -LiteralPath (Join-Path $projectRoot $relativePath))) {
        throw "Missing $relativePath"
    }
}

Write-Host 'Bronze tool registrations, resources, recipes, and tags are complete.'
