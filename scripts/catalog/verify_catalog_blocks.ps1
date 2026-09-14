param(
    [hashtable]$StorageBlocks = @{
        'tin_block' = @{ Name = 'Block of Tin'; Ingredient = 'tin_ingot'; Unpacking = 'tin_ingots_from_block' }
        'raw_tin_block' = @{ Name = 'Block of Raw Tin'; Ingredient = 'raw_tin'; Unpacking = 'raw_tin_from_block' }
        'lead_block' = @{ Name = 'Block of Lead'; Ingredient = 'lead_ingot'; Unpacking = 'lead_ingots_from_block' }
        'raw_lead_block' = @{ Name = 'Block of Raw Lead'; Ingredient = 'raw_lead'; Unpacking = 'raw_lead_from_block' }
        'silver_block' = @{ Name = 'Block of Silver'; Ingredient = 'silver_ingot'; Unpacking = 'silver_ingots_from_block' }
        'raw_silver_block' = @{ Name = 'Block of Raw Silver'; Ingredient = 'raw_silver'; Unpacking = 'raw_silver_from_block' }
        'wax_block' = @{ Name = 'Wax Block'; Ingredient = 'wax'; Unpacking = 'wax_from_block' }
        'coal_coke_block' = @{ Name = 'Coal Coke Block'; Ingredient = 'coal_coke'; Unpacking = 'coal_coke_from_block' }
        'bronze_block' = @{ Name = 'Block of Bronze'; Ingredient = 'bronze_ingot'; Unpacking = 'bronze_ingot_from_block' }
        'steel_block' = @{ Name = 'Block of Steel'; Ingredient = 'steel_ingot'; Unpacking = 'steel_ingot_from_block' }
    },
    [hashtable]$OreBlocks = @{
        'tin_ore' = @{ Name = 'Tin Ore'; Drop = 'raw_tin' }
        'deepslate_tin_ore' = @{ Name = 'Deepslate Tin Ore'; Drop = 'raw_tin' }
        'lead_ore' = @{ Name = 'Lead Ore'; Drop = 'raw_lead' }
        'deepslate_lead_ore' = @{ Name = 'Deepslate Lead Ore'; Drop = 'raw_lead' }
        'silver_ore' = @{ Name = 'Silver Ore'; Drop = 'raw_silver' }
        'deepslate_silver_ore' = @{ Name = 'Deepslate Silver Ore'; Drop = 'raw_silver' }
    },
    [hashtable]$ClinkerBlocks = @{
        'clinker_bricks' = @{ Name = 'Clinker Bricks'; Type = 'random'; Models = 12 }
        'clinker_brick_slab' = @{ Name = 'Clinker Brick Slab'; Type = 'slab' }
        'clinker_brick_stairs' = @{ Name = 'Clinker Brick Stairs'; Type = 'stairs' }
        'clinker_brick_wall' = @{ Name = 'Clinker Brick Wall'; Type = 'wall' }
        'clinker_tile' = @{ Name = 'Clinker Tile'; Type = 'simple' }
        'clinker_tile_slab' = @{ Name = 'Clinker Tile Slab'; Type = 'slab' }
        'clinker_tile_stairs' = @{ Name = 'Clinker Tile Stairs'; Type = 'stairs' }
        'clinker_tile_wall' = @{ Name = 'Clinker Tile Wall'; Type = 'wall' }
        'mosaic_clinker_tile' = @{ Name = 'Mosaic Clinker Tile'; Type = 'random'; Models = 4 }
        'chiseled_clinker_tile' = @{ Name = 'Chiseled Clinker Tile'; Type = 'simple' }
        'clinker_pillar' = @{ Name = 'Clinker Pillar'; Type = 'external' }
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

foreach ($entry in $OreBlocks.GetEnumerator()) {
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
        "data\poptartcore\loot_table\blocks\$id.json"
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
    $loot = Get-Content -Raw -LiteralPath (Join-Path $generatedRoot "data\poptartcore\loot_table\blocks\$id.json") | ConvertFrom-Json
    $silkTouchDrop = $loot.pools[0].entries[0].children[0]
    $rawOreDrop = $loot.pools[0].entries[0].children[1]
    if ($silkTouchDrop.name -ne "poptartcore:$id" -or
        $silkTouchDrop.conditions[0].condition -ne 'minecraft:match_tool' -or
        $rawOreDrop.name -ne "poptartcore:$($details.Drop)" -or
        $rawOreDrop.functions[0].function -ne 'minecraft:apply_bonus' -or
        $rawOreDrop.functions[0].enchantment -ne 'minecraft:fortune' -or
        $rawOreDrop.functions[0].formula -ne 'minecraft:ore_drops') {
        throw "Wrong generated Silk Touch or raw ore drop for $id"
    }
}

$pickaxeTag = Get-Content -Raw -LiteralPath (Join-Path $mainRoot 'data\minecraft\tags\block\mineable\pickaxe.json') | ConvertFrom-Json
$ironToolTag = Get-Content -Raw -LiteralPath (Join-Path $mainRoot 'data\minecraft\tags\block\needs_iron_tool.json') | ConvertFrom-Json
foreach ($id in $OreBlocks.Keys) {
    if ($pickaxeTag.values -notcontains "poptartcore:$id") {
        throw "Pickaxe mining tag is missing $id"
    }
    if ($id -notlike '*tin_ore' -and $ironToolTag.values -notcontains "poptartcore:$id") {
        throw "Iron-tool tag is missing $id"
    }
}

Write-Output "Verified $($OreBlocks.Count) Poptart Catalog ore blocks."

foreach ($entry in $ClinkerBlocks.GetEnumerator()) {
    $id = $entry.Key
    $details = $entry.Value
    $translationKey = "block.poptartcore.$id"
    if ($generatedLanguage.PSObject.Properties[$translationKey].Value -ne $details.Name) {
        throw "Wrong generated name for $id"
    }
    if ($null -ne $mainLanguage.PSObject.Properties[$translationKey]) {
        throw "Handwritten translation still exists for $id"
    }

    $generatedLoot = Join-Path $generatedRoot "data\poptartcore\loot_table\blocks\$id.json"
    if (-not (Test-Path -LiteralPath $generatedLoot)) {
        throw "Missing generated clinker loot table: $id"
    }
    if (Test-Path -LiteralPath (Join-Path $mainRoot "data\poptartcore\loot_table\blocks\$id.json")) {
        throw "Handwritten clinker loot table still exists: $id"
    }

    if ($id -eq 'clinker_pillar') {
        foreach ($relativePath in @(
            'assets\poptartcore\blockstates\clinker_pillar.json',
            'assets\poptartcore\models\item\clinker_pillar.json')) {
            if (-not (Test-Path -LiteralPath (Join-Path $mainRoot $relativePath))) {
                throw "Missing handwritten pillar resource: $relativePath"
            }
            if (Test-Path -LiteralPath (Join-Path $generatedRoot $relativePath)) {
                throw "Catalog unexpectedly generated custom pillar resource: $relativePath"
            }
        }
        continue
    }

    foreach ($relativePath in @(
        "assets\poptartcore\blockstates\$id.json",
        "assets\poptartcore\models\item\$id.json")) {
        if (-not (Test-Path -LiteralPath (Join-Path $generatedRoot $relativePath))) {
            throw "Missing generated clinker resource: $relativePath"
        }
        if (Test-Path -LiteralPath (Join-Path $mainRoot $relativePath)) {
            throw "Handwritten clinker resource still exists: $relativePath"
        }
    }

    $expectedModels = switch ($details.Type) {
        'simple' { @($id) }
        'slab' { @($id, "${id}_top", "${id}_double") }
        'stairs' { @($id, "${id}_inner", "${id}_outer") }
        'wall' { @("${id}_post", "${id}_side", "${id}_side_tall", "${id}_inventory") }
        'random' {
            $names = @($id)
            for ($index = 1; $index -lt $details.Models; $index++) {
                $names += "${id}_$index"
            }
            $names
        }
        default { throw "Unknown clinker model type: $($details.Type)" }
    }
    foreach ($modelName in $expectedModels) {
        $modelPath = Join-Path $generatedRoot "assets\poptartcore\models\block\clinker\$modelName.json"
        if (-not (Test-Path -LiteralPath $modelPath)) {
            throw "Missing generated clinker model: $modelName"
        }
    }
}

foreach ($id in $ClinkerBlocks.Keys) {
    if ($pickaxeTag.values -notcontains "poptartcore:$id") {
        throw "Pickaxe mining tag is missing $id"
    }
}

$wallTag = Get-Content -Raw -LiteralPath (Join-Path $mainRoot 'data\minecraft\tags\block\walls.json') | ConvertFrom-Json
foreach ($id in @('clinker_brick_wall', 'clinker_tile_wall')) {
    if ($wallTag.values -notcontains "poptartcore:$id") {
        throw "Wall tag is missing $id"
    }
}

$brickSlabLoot = Get-Content -Raw -LiteralPath (Join-Path $generatedRoot 'data\poptartcore\loot_table\blocks\clinker_brick_slab.json') | ConvertFrom-Json
$tileSlabLoot = Get-Content -Raw -LiteralPath (Join-Path $generatedRoot 'data\poptartcore\loot_table\blocks\clinker_tile_slab.json') | ConvertFrom-Json
foreach ($loot in @($brickSlabLoot, $tileSlabLoot)) {
    $setCount = $loot.pools[0].entries[0].functions[0]
    if ($setCount.count -ne 2 -or $setCount.conditions[0].properties.type -ne 'double') {
        throw 'Generated clinker slab loot does not return two slabs from a double slab'
    }
}

Write-Output "Verified $($ClinkerBlocks.Count) Poptart Catalog clinker blocks."
