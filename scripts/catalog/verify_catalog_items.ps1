param(
    [string[]]$GeneratedItemIds = @(
        'mining_helmet',
        'raw_hide_helmet',
        'raw_hide_chestplate',
        'raw_hide_leggings',
        'beekeeper_helmet',
        'beekeeper_chestplate',
        'beekeeper_leggings',
        'beekeeper_boots',
        'steel_helmet',
        'steel_chestplate',
        'steel_leggings',
        'steel_boots',
        'steel_ingot',
        'steel_nugget',
        'steel_plate',
        'coal_coke',
        'wax',
        'redstone_circuit',
        'firestarter',
        'unfired_ingot_mould',
        'ingot_mould',
        'unfired_plate_mould',
        'plate_mould',
        'bronze_ingot',
        'bronze_nugget',
        'bronze_plate',
        'tin_ingot',
        'tin_nugget',
        'raw_tin',
        'lead_ingot',
        'lead_nugget',
        'raw_lead',
        'silver_ingot',
        'silver_nugget',
        'raw_silver',
        'raw_titanium',
        'titanium_shard',
        'titanium_ingot',
        'titanium_nugget',
        'titanium_plate'
    ),
    [string[]]$HandheldItemIds = @(
        'bronze_sword',
        'bronze_pickaxe',
        'bronze_axe',
        'bronze_shovel',
        'bronze_knife',
        'steel_sword',
        'steel_pickaxe',
        'steel_axe',
        'steel_shovel',
        'steel_knife',
        'bone_pick',
        'flint_axe',
        'flint_shovel',
        'hammer'
    ),
    [string[]]$CustomModelItemIds = @(
        'bee_smoker',
        'repeating_crossbow'
    ),
    [hashtable]$NameOverrides = @{
        'beekeeper_helmet' = 'Beekeeper Hood'
        'beekeeper_chestplate' = 'Beekeeper Tunic'
        'beekeeper_leggings' = 'Beekeeper Skirt'
    }
)

$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..')).Path
$generatedResourceRoot = Join-Path $projectRoot 'src\generated\resources'
$generatedRoot = Join-Path $projectRoot 'src\generated\resources\assets'
$mainRoot = Join-Path $projectRoot 'src\main\resources\assets\poptartcore'
$generatedLanguagePath = Join-Path $generatedRoot 'poptart_catalog\lang\en_us.json'
$mainLanguagePath = Join-Path $mainRoot 'lang\en_us.json'

if (-not (Test-Path -LiteralPath $generatedLanguagePath)) {
    throw "Missing generated catalog language file: $generatedLanguagePath"
}

$generatedLanguage = Get-Content -Raw -LiteralPath $generatedLanguagePath | ConvertFrom-Json
$mainLanguage = Get-Content -Raw -LiteralPath $mainLanguagePath | ConvertFrom-Json

$catalogItems = @()
$catalogItems += $GeneratedItemIds | ForEach-Object { [PSCustomObject]@{ Id = $_; Parent = 'minecraft:item/generated'; Custom = $false } }
$catalogItems += $HandheldItemIds | ForEach-Object { [PSCustomObject]@{ Id = $_; Parent = 'minecraft:item/handheld'; Custom = $false } }
$catalogItems += $CustomModelItemIds | ForEach-Object { [PSCustomObject]@{ Id = $_; Parent = $null; Custom = $true } }

foreach ($catalogItem in $catalogItems) {
    $itemId = $catalogItem.Id
    $expectedName = if ($NameOverrides.ContainsKey($itemId)) {
        $NameOverrides[$itemId]
    } else {
        (Get-Culture).TextInfo.ToTitleCase($itemId.Replace('_', ' '))
    }
    $translationKey = "item.poptartcore.$itemId"
    $generatedName = $generatedLanguage.PSObject.Properties[$translationKey].Value
    if ($generatedName -ne $expectedName) {
        throw "Wrong generated name for ${itemId}: expected '$expectedName', found '$generatedName'"
    }
    if ($null -ne $mainLanguage.PSObject.Properties[$translationKey]) {
        throw "Handwritten translation still exists for $itemId"
    }

    $handwrittenModelPath = Join-Path $mainRoot "models\item\$itemId.json"
    $generatedModelPath = Join-Path $generatedRoot "poptartcore\models\item\$itemId.json"
    if ($catalogItem.Custom) {
        if (-not (Test-Path -LiteralPath $handwrittenModelPath)) {
            throw "Missing handwritten custom model for $itemId"
        }
        Get-Content -Raw -LiteralPath $handwrittenModelPath | ConvertFrom-Json | Out-Null
        if (Test-Path -LiteralPath $generatedModelPath) {
            throw "Generated model incorrectly exists for custom-model item $itemId"
        }
    } else {
        if (-not (Test-Path -LiteralPath $generatedModelPath)) {
            throw "Missing generated model for $itemId"
        }
        $generatedModel = Get-Content -Raw -LiteralPath $generatedModelPath | ConvertFrom-Json
        if ($generatedModel.parent -ne $catalogItem.Parent) {
            throw "Wrong generated model parent for $itemId"
        }
        $expectedTexture = "poptartcore:item/$itemId"
        if ($generatedModel.textures.layer0 -ne $expectedTexture) {
            throw "Wrong generated texture reference for $itemId"
        }
        if (Test-Path -LiteralPath $handwrittenModelPath) {
            throw "Handwritten model still exists for $itemId"
        }
    }

    if (-not $catalogItem.Custom) {
        $texturePath = Join-Path $mainRoot "textures\item\$itemId.png"
        if (-not (Test-Path -LiteralPath $texturePath)) {
            throw "Missing texture for $itemId"
        }
    }
}

Write-Output "Verified $($catalogItems.Count) Poptart Catalog item names, models, and textures."

$expectedTags = @{
    'minecraft\tags\item\axes.json' = @('poptartcore:flint_axe', 'poptartcore:bronze_axe', 'poptartcore:steel_axe')
    'minecraft\tags\item\chest_armor.json' = @('poptartcore:raw_hide_chestplate', 'poptartcore:steel_chestplate', 'poptartcore:beekeeper_chestplate')
    'minecraft\tags\item\foot_armor.json' = @('poptartcore:steel_boots', 'poptartcore:beekeeper_boots')
    'minecraft\tags\item\head_armor.json' = @('poptartcore:mining_helmet', 'poptartcore:raw_hide_helmet', 'poptartcore:steel_helmet', 'poptartcore:beekeeper_helmet')
    'minecraft\tags\item\hoes.json' = @('poptartcore:bone_pick')
    'minecraft\tags\item\leg_armor.json' = @('poptartcore:raw_hide_leggings', 'poptartcore:steel_leggings', 'poptartcore:beekeeper_leggings')
    'minecraft\tags\item\pickaxes.json' = @('poptartcore:bone_pick', 'poptartcore:bronze_pickaxe', 'poptartcore:steel_pickaxe')
    'minecraft\tags\item\shovels.json' = @('poptartcore:flint_shovel', 'poptartcore:bronze_shovel', 'poptartcore:steel_shovel')
    'minecraft\tags\item\swords.json' = @('poptartcore:bronze_sword', 'poptartcore:steel_sword')
    'farmersdelight\tags\item\tools\knives.json' = @('poptartcore:bronze_knife', 'poptartcore:steel_knife')
}

foreach ($entry in $expectedTags.GetEnumerator()) {
    $generatedTagPath = Join-Path $generatedResourceRoot "data\$($entry.Key)"
    if (-not (Test-Path -LiteralPath $generatedTagPath)) {
        throw "Missing generated Catalog item tag: $($entry.Key)"
    }
    $generatedTag = Get-Content -Raw -LiteralPath $generatedTagPath | ConvertFrom-Json
    $expectedValues = @($entry.Value | Sort-Object)
    $actualValues = @($generatedTag.values | Sort-Object)
    if (($expectedValues -join "`n") -ne ($actualValues -join "`n")) {
        throw "Wrong generated values for Catalog item tag: $($entry.Key)"
    }
    if ($generatedTag.replace -eq $true) {
        throw "Catalog item tag replaces values from other mods: $($entry.Key)"
    }
    $handwrittenTagPath = Join-Path $projectRoot "src\main\resources\data\$($entry.Key)"
    if (Test-Path -LiteralPath $handwrittenTagPath) {
        throw "Handwritten item tag still exists: $($entry.Key)"
    }
}

Write-Output "Verified $($expectedTags.Count) generated Poptart Catalog item tags."

$fuelDataMapRelativePath = 'data\neoforge\data_maps\item\furnace_fuels.json'
$generatedFuelDataMapPath = Join-Path $generatedResourceRoot $fuelDataMapRelativePath
if (-not (Test-Path -LiteralPath $generatedFuelDataMapPath)) {
    throw "Missing generated Catalog furnace-fuel data map: $fuelDataMapRelativePath"
}
if (Test-Path -LiteralPath (Join-Path $projectRoot "src\main\resources\$fuelDataMapRelativePath")) {
    throw "Handwritten furnace-fuel data map still exists: $fuelDataMapRelativePath"
}

$fuelDataMap = Get-Content -Raw -LiteralPath $generatedFuelDataMapPath | ConvertFrom-Json
$expectedFuels = @{
    'poptartcore:coal_coke' = 3200
    'poptartcore:coal_coke_block' = 28800
}
$actualFuelIds = @($fuelDataMap.values.PSObject.Properties.Name | Sort-Object)
$expectedFuelIds = @($expectedFuels.Keys | Sort-Object)
if (($expectedFuelIds -join "`n") -ne ($actualFuelIds -join "`n")) {
    throw 'Generated Catalog furnace-fuel data map contains the wrong items'
}
foreach ($entry in $expectedFuels.GetEnumerator()) {
    if ($fuelDataMap.values.PSObject.Properties[$entry.Key].Value.burn_time -ne $entry.Value) {
        throw "Wrong generated burn time for $($entry.Key)"
    }
}

Write-Output "Verified $($expectedFuels.Count) generated Poptart Catalog furnace fuels."
