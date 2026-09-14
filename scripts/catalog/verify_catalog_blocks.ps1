param(
    [hashtable]$StorageBlocks = @{
        'tin_block' = @{ Name = 'Block of Tin'; Ingredient = 'tin_ingot'; Unpacking = 'tin_ingots_from_block' }
        'lead_block' = @{ Name = 'Block of Lead'; Ingredient = 'lead_ingot'; Unpacking = 'lead_ingots_from_block' }
        'silver_block' = @{ Name = 'Block of Silver'; Ingredient = 'silver_ingot'; Unpacking = 'silver_ingots_from_block' }
        'wax_block' = @{ Name = 'Wax Block'; Ingredient = 'wax'; Unpacking = 'wax_from_block' }
        'coal_coke_block' = @{ Name = 'Coal Coke Block'; Ingredient = 'coal_coke'; Unpacking = 'coal_coke_from_block' }
        'bronze_block' = @{ Name = 'Block of Bronze'; Ingredient = 'bronze_ingot'; Unpacking = 'bronze_ingot_from_block' }
        'steel_block' = @{ Name = 'Block of Steel'; Ingredient = 'steel_ingot'; Unpacking = 'steel_ingot_from_block' }
    }
)

$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..')).Path
$generatedRoot = Join-Path $projectRoot 'src\generated\resources'
$mainRoot = Join-Path $projectRoot 'src\main\resources'
$generatedLanguage = Get-Content -Raw -LiteralPath (Join-Path $generatedRoot 'assets\poptart_catalog\lang\en_us.json') | ConvertFrom-Json
$mainLanguage = Get-Content -Raw -LiteralPath (Join-Path $mainRoot 'assets\poptartcore\lang\en_us.json') | ConvertFrom-Json

foreach ($entry in $StorageBlocks.GetEnumerator()) {
    $id = $entry.Key
    $details = $entry.Value
    $translationKey = "block.poptartcore.$id"
    if ($generatedLanguage.PSObject.Properties[$translationKey].Value -ne $details.Name) {
        throw "Wrong generated name for $id"
    }
    if ($null -ne $mainLanguage.PSObject.Properties[$translationKey]) {
        throw "Handwritten translation still exists for $id"
    }

    $generatedFiles = @(
        "assets\poptartcore\blockstates\$id.json",
        "assets\poptartcore\models\block\$id.json",
        "assets\poptartcore\models\item\$id.json",
        "data\poptartcore\loot_table\blocks\$id.json",
        "data\poptartcore\recipe\$id.json",
        "data\poptartcore\recipe\$($details.Unpacking).json"
    )
    foreach ($relativePath in $generatedFiles) {
        $generatedPath = Join-Path $generatedRoot $relativePath
        if (-not (Test-Path -LiteralPath $generatedPath)) {
            throw "Missing generated resource: $relativePath"
        }
        $jsonDocument = [System.Text.Json.JsonDocument]::Parse((Get-Content -Raw -LiteralPath $generatedPath))
        $jsonDocument.Dispose()
        if (Test-Path -LiteralPath (Join-Path $mainRoot $relativePath)) {
            throw "Handwritten resource still exists: $relativePath"
        }
    }

    $blockModel = Get-Content -Raw -LiteralPath (Join-Path $generatedRoot "assets\poptartcore\models\block\$id.json") | ConvertFrom-Json
    if ($blockModel.parent -ne 'minecraft:block/cube_all' -or $blockModel.textures.all -ne "poptartcore:block/$id") {
        throw "Wrong generated cube model for $id"
    }
    $itemModel = Get-Content -Raw -LiteralPath (Join-Path $generatedRoot "assets\poptartcore\models\item\$id.json") | ConvertFrom-Json
    if ($itemModel.parent -ne "poptartcore:block/$id") {
        throw "Wrong generated block-item model for $id"
    }

    $packing = Get-Content -Raw -LiteralPath (Join-Path $generatedRoot "data\poptartcore\recipe\$id.json") | ConvertFrom-Json
    if ($packing.result.id -ne "poptartcore:$id" -or $packing.key.'#'.item -ne "poptartcore:$($details.Ingredient)") {
        throw "Wrong generated packing recipe for $id"
    }
    $unpacking = Get-Content -Raw -LiteralPath (Join-Path $generatedRoot "data\poptartcore\recipe\$($details.Unpacking).json") | ConvertFrom-Json
    if ($unpacking.result.id -ne "poptartcore:$($details.Ingredient)" -or $unpacking.result.count -ne 9) {
        throw "Wrong generated unpacking recipe for $id"
    }
}

Write-Output "Verified $($StorageBlocks.Count) Poptart Catalog storage blocks."
