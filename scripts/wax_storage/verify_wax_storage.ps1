$ErrorActionPreference = 'Stop'

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$resourceRoot = Join-Path $projectRoot 'src\main\resources'
$blocksSource = Get-Content -Raw -LiteralPath (Join-Path $projectRoot 'src\main\java\dev\poptartking\poptartcore\registry\PoptartCoreBlocks.java')
$itemsSource = Get-Content -Raw -LiteralPath (Join-Path $projectRoot 'src\main\java\dev\poptartking\poptartcore\registry\PoptartCoreItems.java')
$tabsSource = Get-Content -Raw -LiteralPath (Join-Path $projectRoot 'src\main\java\dev\poptartking\poptartcore\registry\PoptartCoreTabs.java')
$mixins = Get-Content -Raw -LiteralPath (Join-Path $resourceRoot 'poptartcore.mixins.json') | ConvertFrom-Json
$language = Get-Content -Raw -LiteralPath (Join-Path $resourceRoot 'assets\poptartcore\lang\en_us.json') | ConvertFrom-Json
$blockIds = @('wax_block', 'coal_coke_block', 'bronze_block', 'steel_block')

if ($itemsSource -notmatch '\bWAX\b' -or -not $language.PSObject.Properties['item.poptartcore.wax']) {
    throw 'Wax item registration or language entry is missing.'
}
foreach ($relativePath in @(
    'assets\poptartcore\models\item\wax.json',
    'assets\poptartcore\textures\item\wax.png'
)) {
    if (-not (Test-Path -LiteralPath (Join-Path $resourceRoot $relativePath))) {
        throw "Missing Wax resource: $relativePath"
    }
}

foreach ($blockId in $blockIds) {
    $constant = $blockId.ToUpperInvariant()
    if ($blocksSource -notmatch "\b$constant\b" -or $itemsSource -notmatch "\b$constant\b") {
        throw "Missing block or block-item registration: $blockId"
    }
    if ($tabsSource -notmatch "PoptartCoreItems\.$constant") {
        throw "Missing creative-tab entry: $blockId"
    }
    if (-not $language.PSObject.Properties["block.poptartcore.$blockId"]) {
        throw "Missing language entry: $blockId"
    }
    foreach ($relativePath in @(
        "assets\poptartcore\blockstates\$blockId.json",
        "assets\poptartcore\models\block\$blockId.json",
        "assets\poptartcore\models\item\$blockId.json",
        "assets\poptartcore\textures\block\$blockId.png",
        "data\poptartcore\loot_table\blocks\$blockId.json",
        "data\poptartcore\recipe\$blockId.json"
    )) {
        if (-not (Test-Path -LiteralPath (Join-Path $resourceRoot $relativePath))) {
            throw "Missing block resource: $relativePath"
        }
    }
}

foreach ($mixin in @('wax.HoneycombWaxingMixin', 'wax.IWaxableMoonlightMixin')) {
    if ($mixin -notin $mixins.mixins) {
        throw "Missing mixin registration: $mixin"
    }
}

$fuelMap = Get-Content -Raw -LiteralPath (Join-Path $resourceRoot 'data\neoforge\data_maps\item\furnace_fuels.json')
if ($fuelMap -notmatch 'poptartcore:coal_coke_block' -or $fuelMap -notmatch '28800') {
    throw 'Coal Coke Block fuel value is missing.'
}

Write-Host 'Wax behavior and four storage blocks are registered with complete resources and recipes.'
