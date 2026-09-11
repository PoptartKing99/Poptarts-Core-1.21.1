$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$resourceRoot = Join-Path $repoRoot "src\main\resources"
$assetRoot = Join-Path $resourceRoot "assets\poptartcore"
$dataRoot = Join-Path $resourceRoot "data\poptartcore"
$blockSource = Get-Content -Raw (Join-Path $repoRoot "src\main\java\dev\poptartking\poptartcore\registry\PoptartCoreBlocks.java")
$itemSource = Get-Content -Raw (Join-Path $repoRoot "src\main\java\dev\poptartking\poptartcore\registry\PoptartCoreItems.java")
$tabSource = Get-Content -Raw (Join-Path $repoRoot "src\main\java\dev\poptartking\poptartcore\registry\PoptartCoreTabs.java")
$language = Get-Content -Raw (Join-Path $assetRoot "lang\en_us.json") | ConvertFrom-Json -AsHashtable

$blockIds = @(
    "tin_ore", "deepslate_tin_ore", "tin_block", "raw_tin_block",
    "lead_ore", "deepslate_lead_ore", "lead_block", "raw_lead_block",
    "silver_ore", "deepslate_silver_ore", "silver_block", "raw_silver_block"
)

foreach ($id in $blockIds) {
    $constant = $id.ToUpperInvariant()
    if ($blockSource -notmatch "\b$constant\b") { throw "Missing block registration: $constant" }
    if ($itemSource -notmatch "\b$constant\b") { throw "Missing block item registration: $constant" }
    if ($tabSource -notmatch "PoptartCoreItems\.$constant\b") { throw "Missing creative-tab entry: $constant" }
    if (-not $language.ContainsKey("block.poptartcore.$id")) { throw "Missing language entry: block.poptartcore.$id" }

    $paths = @(
        (Join-Path $assetRoot "blockstates\$id.json"),
        (Join-Path $assetRoot "models\block\$id.json"),
        (Join-Path $assetRoot "models\item\$id.json"),
        (Join-Path $assetRoot "textures\block\$id.png"),
        (Join-Path $dataRoot "loot_table\blocks\$id.json")
    )
    foreach ($path in $paths) {
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Missing file: $path" }
    }

    foreach ($jsonPath in $paths | Where-Object { $_.EndsWith(".json") }) {
        $jsonText = Get-Content -Raw -LiteralPath $jsonPath
        $null = $jsonText | ConvertFrom-Json -AsHashtable
        if ($jsonText -match "wayfarer_core") { throw "Wayfarer namespace remains in $jsonPath" }
    }
}

$recipeIds = @(
    "tin_block", "tin_ingots_from_block", "raw_tin_block", "raw_tin_from_block",
    "lead_block", "lead_ingots_from_block", "raw_lead_block", "raw_lead_from_block",
    "silver_block", "silver_ingots_from_block", "raw_silver_block", "raw_silver_from_block"
)
foreach ($id in $recipeIds) {
    $path = Join-Path $dataRoot "recipe\$id.json"
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Missing recipe: $id" }
    $null = Get-Content -Raw -LiteralPath $path | ConvertFrom-Json -AsHashtable
}

$pickaxeTag = Get-Content -Raw (Join-Path $resourceRoot "data\minecraft\tags\block\mineable\pickaxe.json") | ConvertFrom-Json
foreach ($id in $blockIds) {
    if ("poptartcore:$id" -notin $pickaxeTag.values) { throw "Missing pickaxe tag entry: $id" }
}

$ironToolTag = Get-Content -Raw (Join-Path $resourceRoot "data\minecraft\tags\block\needs_iron_tool.json") | ConvertFrom-Json
foreach ($metal in @("lead", "silver")) {
    foreach ($id in @("${metal}_ore", "deepslate_${metal}_ore", "${metal}_block", "raw_${metal}_block")) {
        if ("poptartcore:$id" -notin $ironToolTag.values) { throw "Missing iron-tool tag entry: $id" }
    }
}

Write-Output "Verified 12 metal blocks, 12 block items, 60 resource files, 12 recipes, mining tags, names, and creative-tab entries."
