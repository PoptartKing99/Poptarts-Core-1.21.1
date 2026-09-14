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

$expectedBlockTags = @{
    'minecraft\tags\block\mineable\axe.json' = @(
        'poptartcore:crucible',
        'poptartcore:workbench'
    )
    'minecraft\tags\block\mineable\pickaxe.json' = @(
        'poptartcore:portable_engine',
        'poptartcore:millstone',
        'poptartcore:millstone_structural',
        'poptartcore:millstone_rotor',
        'poptartcore:crucible',
        'poptartcore:blast_furnace',
        'poptartcore:bloomery',
        'poptartcore:iron_bloom',
        'poptartcore:clinker_bricks',
        'poptartcore:clinker_brick_slab',
        'poptartcore:clinker_brick_stairs',
        'poptartcore:clinker_brick_wall',
        'poptartcore:clinker_tile',
        'poptartcore:clinker_tile_slab',
        'poptartcore:clinker_tile_stairs',
        'poptartcore:clinker_tile_wall',
        'poptartcore:mosaic_clinker_tile',
        'poptartcore:chiseled_clinker_tile',
        'poptartcore:clinker_pillar',
        'poptartcore:quern',
        'poptartcore:tin_ore',
        'poptartcore:deepslate_tin_ore',
        'poptartcore:tin_block',
        'poptartcore:raw_tin_block',
        'poptartcore:lead_ore',
        'poptartcore:deepslate_lead_ore',
        'poptartcore:lead_block',
        'poptartcore:raw_lead_block',
        'poptartcore:silver_ore',
        'poptartcore:deepslate_silver_ore',
        'poptartcore:silver_block',
        'poptartcore:raw_silver_block',
        'poptartcore:coal_coke_block',
        'poptartcore:bronze_block',
        'poptartcore:steel_block'
    )
    'minecraft\tags\block\needs_iron_tool.json' = @(
        'poptartcore:lead_ore',
        'poptartcore:deepslate_lead_ore',
        'poptartcore:lead_block',
        'poptartcore:raw_lead_block',
        'poptartcore:silver_ore',
        'poptartcore:deepslate_silver_ore',
        'poptartcore:silver_block',
        'poptartcore:raw_silver_block',
        'poptartcore:steel_block'
    )
    'minecraft\tags\block\needs_stone_tool.json' = @(
        'poptartcore:bronze_block'
    )
    'minecraft\tags\block\walls.json' = @(
        'poptartcore:clinker_brick_wall',
        'poptartcore:clinker_tile_wall'
    )
}

foreach ($entry in $expectedBlockTags.GetEnumerator()) {
    $relativePath = Join-Path 'data' $entry.Key
    $generatedPath = Join-Path $generatedRoot $relativePath
    if (-not (Test-Path -LiteralPath $generatedPath)) {
        throw "Missing generated block tag: $relativePath"
    }
    if (Test-Path -LiteralPath (Join-Path $mainRoot $relativePath)) {
        throw "Handwritten block tag still exists: $relativePath"
    }

    $tag = Get-Content -Raw -LiteralPath $generatedPath | ConvertFrom-Json
    if ($tag.replace -eq $true) {
        throw "Generated block tag unexpectedly replaces the shared tag: $relativePath"
    }
    $expectedValues = @($entry.Value | Sort-Object)
    $actualValues = @($tag.values | Sort-Object)
    if (($expectedValues.Count -ne $actualValues.Count) -or
        (Compare-Object -ReferenceObject $expectedValues -DifferenceObject $actualValues)) {
        throw "Generated block tag has the wrong values: $relativePath"
    }
}

$pickaxeTag = Get-Content -Raw -LiteralPath (Join-Path $generatedRoot 'data\minecraft\tags\block\mineable\pickaxe.json') | ConvertFrom-Json
$ironToolTag = Get-Content -Raw -LiteralPath (Join-Path $generatedRoot 'data\minecraft\tags\block\needs_iron_tool.json') | ConvertFrom-Json
$wallTag = Get-Content -Raw -LiteralPath (Join-Path $generatedRoot 'data\minecraft\tags\block\walls.json') | ConvertFrom-Json

Write-Output "Verified $($expectedBlockTags.Count) generated Poptart Catalog block tags."

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

$ironBloomName = $generatedLanguage.PSObject.Properties['block.poptartcore.iron_bloom'].Value
if ($ironBloomName -ne 'Iron Bloom') {
    throw 'Wrong generated name for iron_bloom'
}
if ($null -ne $mainLanguage.PSObject.Properties['block.poptartcore.iron_bloom']) {
    throw 'Handwritten Iron Bloom translation still exists'
}
foreach ($relativePath in @(
    'assets\poptartcore\blockstates\iron_bloom.json',
    'assets\poptartcore\models\item\iron_bloom.json')) {
    if (-not (Test-Path -LiteralPath (Join-Path $mainRoot $relativePath))) {
        throw "Missing handwritten Iron Bloom resource: $relativePath"
    }
    if (Test-Path -LiteralPath (Join-Path $generatedRoot $relativePath)) {
        throw "Catalog unexpectedly generated custom Iron Bloom resource: $relativePath"
    }
}
$ironBloomLoot = 'data\poptartcore\loot_table\blocks\iron_bloom.json'
if ((Test-Path -LiteralPath (Join-Path $mainRoot $ironBloomLoot)) -or
    (Test-Path -LiteralPath (Join-Path $generatedRoot $ironBloomLoot))) {
    throw 'Iron Bloom must not have a generated loot table'
}
if ($pickaxeTag.values -notcontains 'poptartcore:iron_bloom') {
    throw 'Pickaxe mining tag is missing iron_bloom'
}

Write-Output 'Verified the Poptart Catalog Iron Bloom registration.'

$workbenchName = $generatedLanguage.PSObject.Properties['block.poptartcore.workbench'].Value
if ($workbenchName -ne 'Workbench') {
    throw 'Wrong generated name for workbench'
}
if ($null -ne $mainLanguage.PSObject.Properties['block.poptartcore.workbench']) {
    throw 'Handwritten Workbench block translation still exists'
}
foreach ($relativePath in @(
    'assets\poptartcore\blockstates\workbench.json',
    'assets\poptartcore\models\item\workbench.json')) {
    if (-not (Test-Path -LiteralPath (Join-Path $mainRoot $relativePath))) {
        throw "Missing handwritten Workbench resource: $relativePath"
    }
    if (Test-Path -LiteralPath (Join-Path $generatedRoot $relativePath)) {
        throw "Catalog unexpectedly generated custom Workbench resource: $relativePath"
    }
}
$workbenchLootPath = 'data\poptartcore\loot_table\blocks\workbench.json'
$generatedWorkbenchLootPath = Join-Path $generatedRoot $workbenchLootPath
if (-not (Test-Path -LiteralPath $generatedWorkbenchLootPath)) {
    throw 'Missing generated Workbench self-drop loot table'
}
if (Test-Path -LiteralPath (Join-Path $mainRoot $workbenchLootPath)) {
    throw 'Handwritten Workbench loot table still exists'
}
$workbenchLoot = Get-Content -Raw -LiteralPath $generatedWorkbenchLootPath | ConvertFrom-Json
if ($workbenchLoot.pools[0].entries[0].name -ne 'poptartcore:workbench') {
    throw 'Generated Workbench loot table does not drop the Workbench'
}

Write-Output 'Verified the Poptart Catalog Workbench registration.'

$quernName = $generatedLanguage.PSObject.Properties['block.poptartcore.quern'].Value
if ($quernName -ne 'Quern') {
    throw 'Wrong generated name for quern'
}
if ($null -ne $mainLanguage.PSObject.Properties['block.poptartcore.quern']) {
    throw 'Handwritten Quern block translation still exists'
}
foreach ($relativePath in @(
    'assets\poptartcore\blockstates\quern.json',
    'assets\poptartcore\models\item\quern.json')) {
    if (-not (Test-Path -LiteralPath (Join-Path $mainRoot $relativePath))) {
        throw "Missing handwritten Quern resource: $relativePath"
    }
    if (Test-Path -LiteralPath (Join-Path $generatedRoot $relativePath)) {
        throw "Catalog unexpectedly generated custom Quern resource: $relativePath"
    }
}
$quernLootPath = 'data\poptartcore\loot_table\blocks\quern.json'
$generatedQuernLootPath = Join-Path $generatedRoot $quernLootPath
if (-not (Test-Path -LiteralPath $generatedQuernLootPath)) {
    throw 'Missing generated Quern self-drop loot table'
}
if (Test-Path -LiteralPath (Join-Path $mainRoot $quernLootPath)) {
    throw 'Handwritten Quern loot table still exists'
}
$quernLoot = Get-Content -Raw -LiteralPath $generatedQuernLootPath | ConvertFrom-Json
if ($quernLoot.pools[0].entries[0].name -ne 'poptartcore:quern') {
    throw 'Generated Quern loot table does not drop the Quern'
}

Write-Output 'Verified the Poptart Catalog Quern registration.'

$bloomeryName = $generatedLanguage.PSObject.Properties['block.poptartcore.bloomery'].Value
if ($bloomeryName -ne 'Bloomery') {
    throw 'Wrong generated name for bloomery'
}
if ($null -ne $mainLanguage.PSObject.Properties['block.poptartcore.bloomery']) {
    throw 'Handwritten Bloomery block translation still exists'
}
foreach ($relativePath in @(
    'assets\poptartcore\blockstates\bloomery.json',
    'assets\poptartcore\models\item\bloomery.json')) {
    if (-not (Test-Path -LiteralPath (Join-Path $mainRoot $relativePath))) {
        throw "Missing handwritten Bloomery resource: $relativePath"
    }
    if (Test-Path -LiteralPath (Join-Path $generatedRoot $relativePath)) {
        throw "Catalog unexpectedly generated custom Bloomery resource: $relativePath"
    }
}
$bloomeryLootPath = 'data\poptartcore\loot_table\blocks\bloomery.json'
$generatedBloomeryLootPath = Join-Path $generatedRoot $bloomeryLootPath
if (-not (Test-Path -LiteralPath $generatedBloomeryLootPath)) {
    throw 'Missing generated Bloomery self-drop loot table'
}
if (Test-Path -LiteralPath (Join-Path $mainRoot $bloomeryLootPath)) {
    throw 'Handwritten Bloomery loot table still exists'
}
$bloomeryLoot = Get-Content -Raw -LiteralPath $generatedBloomeryLootPath | ConvertFrom-Json
if ($bloomeryLoot.pools[0].entries[0].name -ne 'poptartcore:bloomery') {
    throw 'Generated Bloomery loot table does not drop the Bloomery'
}

Write-Output 'Verified the Poptart Catalog Bloomery registration.'

$portableEngineName = $generatedLanguage.PSObject.Properties['block.poptartcore.portable_engine'].Value
if ($portableEngineName -ne 'Portable Engine') {
    throw 'Wrong generated name for portable_engine'
}
if ($null -ne $mainLanguage.PSObject.Properties['block.poptartcore.portable_engine']) {
    throw 'Handwritten Portable Engine block translation still exists'
}
foreach ($relativePath in @(
    'assets\poptartcore\blockstates\portable_engine.json',
    'assets\poptartcore\models\item\portable_engine.json')) {
    if (-not (Test-Path -LiteralPath (Join-Path $mainRoot $relativePath))) {
        throw "Missing handwritten Portable Engine resource: $relativePath"
    }
    if (Test-Path -LiteralPath (Join-Path $generatedRoot $relativePath)) {
        throw "Catalog unexpectedly generated custom Portable Engine resource: $relativePath"
    }
}
$portableEngineLootPath = 'data\poptartcore\loot_table\blocks\portable_engine.json'
$generatedPortableEngineLootPath = Join-Path $generatedRoot $portableEngineLootPath
if (-not (Test-Path -LiteralPath $generatedPortableEngineLootPath)) {
    throw 'Missing generated Portable Engine self-drop loot table'
}
if (Test-Path -LiteralPath (Join-Path $mainRoot $portableEngineLootPath)) {
    throw 'Handwritten Portable Engine loot table still exists'
}
$portableEngineLoot = Get-Content -Raw -LiteralPath $generatedPortableEngineLootPath | ConvertFrom-Json
if ($portableEngineLoot.pools[0].entries[0].name -ne 'poptartcore:portable_engine') {
    throw 'Generated Portable Engine loot table does not drop the Portable Engine'
}

Write-Output 'Verified the Poptart Catalog Portable Engine registration.'

$crucibleName = $generatedLanguage.PSObject.Properties['block.poptartcore.crucible'].Value
if ($crucibleName -ne 'Crucible') {
    throw 'Wrong generated name for crucible'
}
if ($null -ne $mainLanguage.PSObject.Properties['block.poptartcore.crucible']) {
    throw 'Handwritten Crucible block translation still exists'
}
if ($mainLanguage.PSObject.Properties['container.poptartcore.crucible'].Value -ne 'Crucible') {
    throw 'Crucible menu translation must remain handwritten'
}
foreach ($relativePath in @(
    'assets\poptartcore\blockstates\crucible.json',
    'assets\poptartcore\models\item\crucible.json')) {
    if (-not (Test-Path -LiteralPath (Join-Path $mainRoot $relativePath))) {
        throw "Missing handwritten Crucible resource: $relativePath"
    }
    if (Test-Path -LiteralPath (Join-Path $generatedRoot $relativePath)) {
        throw "Catalog unexpectedly generated custom Crucible resource: $relativePath"
    }
}
$crucibleLootPath = 'data\poptartcore\loot_table\blocks\crucible.json'
$generatedCrucibleLootPath = Join-Path $generatedRoot $crucibleLootPath
if (-not (Test-Path -LiteralPath $generatedCrucibleLootPath)) {
    throw 'Missing generated Crucible self-drop loot table'
}
if (Test-Path -LiteralPath (Join-Path $mainRoot $crucibleLootPath)) {
    throw 'Handwritten Crucible loot table still exists'
}
$crucibleLoot = Get-Content -Raw -LiteralPath $generatedCrucibleLootPath | ConvertFrom-Json
if ($crucibleLoot.pools[0].entries[0].name -ne 'poptartcore:crucible') {
    throw 'Generated Crucible loot table does not drop the Crucible'
}

Write-Output 'Verified the Poptart Catalog Crucible registration and custom BlockItem.'

$millstoneName = $generatedLanguage.PSObject.Properties['block.poptartcore.millstone'].Value
if ($millstoneName -ne 'Millstone') {
    throw 'Wrong generated name for millstone'
}
if ($null -ne $mainLanguage.PSObject.Properties['block.poptartcore.millstone']) {
    throw 'Handwritten Millstone block translation still exists'
}
foreach ($helperName in @('millstone_structural', 'millstone_rotor')) {
    if ($mainLanguage.PSObject.Properties["block.poptartcore.$helperName"].Value -ne 'Millstone') {
        throw "Internal Millstone helper translation is missing: $helperName"
    }
}
foreach ($relativePath in @(
    'assets\poptartcore\blockstates\millstone.json',
    'assets\poptartcore\models\item\millstone.json')) {
    if (-not (Test-Path -LiteralPath (Join-Path $mainRoot $relativePath))) {
        throw "Missing handwritten Millstone resource: $relativePath"
    }
    if (Test-Path -LiteralPath (Join-Path $generatedRoot $relativePath)) {
        throw "Catalog unexpectedly generated custom Millstone resource: $relativePath"
    }
}
$millstoneLootPath = 'data\poptartcore\loot_table\blocks\millstone.json'
$generatedMillstoneLootPath = Join-Path $generatedRoot $millstoneLootPath
if (-not (Test-Path -LiteralPath $generatedMillstoneLootPath)) {
    throw 'Missing generated Millstone self-drop loot table'
}
if (Test-Path -LiteralPath (Join-Path $mainRoot $millstoneLootPath)) {
    throw 'Handwritten Millstone loot table still exists'
}
$millstoneLoot = Get-Content -Raw -LiteralPath $generatedMillstoneLootPath | ConvertFrom-Json
if ($millstoneLoot.pools[0].entries[0].name -ne 'poptartcore:millstone') {
    throw 'Generated Millstone loot table does not drop the Millstone'
}
foreach ($helperName in @('millstone_structural', 'millstone_rotor')) {
    $helperLootPath = "data\poptartcore\loot_table\blocks\$helperName.json"
    if ((Test-Path -LiteralPath (Join-Path $mainRoot $helperLootPath)) -or
        (Test-Path -LiteralPath (Join-Path $generatedRoot $helperLootPath))) {
        throw "Internal Millstone helper must not have a loot table: $helperName"
    }
}

Write-Output 'Verified the Poptart Catalog Millstone and its internal helper blocks.'

$blastFurnaceName = $generatedLanguage.PSObject.Properties['block.poptartcore.blast_furnace'].Value
if ($blastFurnaceName -ne 'Blast Furnace') {
    throw 'Wrong generated name for blast_furnace'
}
if ($null -ne $mainLanguage.PSObject.Properties['block.poptartcore.blast_furnace']) {
    throw 'Handwritten Blast Furnace block translation still exists'
}
if ($mainLanguage.PSObject.Properties['container.poptartcore.blast_furnace'].Value -ne 'Blast Furnace') {
    throw 'Blast Furnace menu translation must remain handwritten'
}
foreach ($relativePath in @(
    'assets\poptartcore\blockstates\blast_furnace.json',
    'assets\poptartcore\models\item\blast_furnace.json')) {
    if (-not (Test-Path -LiteralPath (Join-Path $mainRoot $relativePath))) {
        throw "Missing handwritten Blast Furnace resource: $relativePath"
    }
    if (Test-Path -LiteralPath (Join-Path $generatedRoot $relativePath)) {
        throw "Catalog unexpectedly generated custom Blast Furnace resource: $relativePath"
    }
}
$blastFurnaceLootPath = 'data\poptartcore\loot_table\blocks\blast_furnace.json'
$generatedBlastFurnaceLootPath = Join-Path $generatedRoot $blastFurnaceLootPath
if (-not (Test-Path -LiteralPath $generatedBlastFurnaceLootPath)) {
    throw 'Missing generated Blast Furnace self-drop loot table'
}
if (Test-Path -LiteralPath (Join-Path $mainRoot $blastFurnaceLootPath)) {
    throw 'Handwritten Blast Furnace loot table still exists'
}
$blastFurnaceLoot = Get-Content -Raw -LiteralPath $generatedBlastFurnaceLootPath | ConvertFrom-Json
if ($blastFurnaceLoot.pools[0].entries[0].name -ne 'poptartcore:blast_furnace') {
    throw 'Generated Blast Furnace loot table does not drop the Blast Furnace'
}

Write-Output 'Verified the Poptart Catalog Blast Furnace registration.'
