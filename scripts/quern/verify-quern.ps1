$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)

$required = @(
    "src\main\java\dev\poptartking\poptartcore\quern\QuernBlock.java",
    "src\main\java\dev\poptartking\poptartcore\quern\QuernBlockEntity.java",
    "src\main\java\dev\poptartking\poptartcore\quern\client\QuernRenderer.java",
    "src\main\java\dev\poptartking\poptartcore\quern\client\QuernSoundInstance.java",
    "src\main\java\dev\poptartking\poptartcore\quern\client\QuernHighlightRenderer.java",
    "src\main\java\dev\poptartking\poptartcore\quern\recipe\GrindingRecipe.java",
    "src\main\java\dev\poptartking\poptartcore\registry\PoptartCoreSounds.java",
    "src\main\resources\assets\poptartcore\blockstates\quern.json",
    "src\main\resources\assets\poptartcore\models\block\quern\quern_stone_base.json",
    "src\main\resources\assets\poptartcore\models\block\quern\quern_stone.json",
    "src\main\resources\assets\poptartcore\models\block\quern\quern_flour.json",
    "src\main\resources\assets\poptartcore\models\block\quern\quern_inventory.json",
    "src\main\resources\assets\poptartcore\models\item\quern.json",
    "src\main\resources\assets\poptartcore\textures\block\quern\flour.png",
    "src\main\resources\assets\poptartcore\sounds\quern\quern.ogg",
    "src\main\resources\assets\poptartcore\sounds.json",
    "src\main\resources\data\poptartcore\recipe\grinding\flour_from_wheat.json",
    "src\main\resources\data\poptartcore\loot_table\blocks\quern.json"
)

foreach ($relative in $required) {
    $path = Join-Path $projectRoot $relative
    if (-not (Test-Path -LiteralPath $path)) {
        throw "Missing Quern file: $relative"
    }
}

$jsonFiles = Get-ChildItem -Recurse -File (Join-Path $projectRoot "src\main\resources") -Filter *.json
foreach ($json in $jsonFiles) {
    Get-Content -Raw -LiteralPath $json.FullName | ConvertFrom-Json -AsHashtable | Out-Null
}

$base = Get-Content -Raw -LiteralPath (Join-Path $projectRoot "src\main\resources\assets\poptartcore\models\block\quern\quern_stone_base.json") | ConvertFrom-Json
$rotor = Get-Content -Raw -LiteralPath (Join-Path $projectRoot "src\main\resources\assets\poptartcore\models\block\quern\quern_stone.json") | ConvertFrom-Json
$flour = Get-Content -Raw -LiteralPath (Join-Path $projectRoot "src\main\resources\assets\poptartcore\models\block\quern\quern_flour.json") | ConvertFrom-Json
if (@($base.elements | Where-Object name -eq "Powder").Count -ne 0) { throw "Powder must not be baked into the stationary base" }
if (@($rotor.elements | Where-Object name -eq "quern_stone").Count -eq 0) { throw "Rotor has no quern stone geometry" }
foreach ($model in @($base, $rotor)) {
    foreach ($texture in $model.textures.PSObject.Properties.Value) {
        if ($texture -notmatch "^([a-z0-9_.-]+:)?[a-z0-9_./-]+$") {
            throw "Quern model has an invalid Minecraft texture location: $texture"
        }
    }
}

$quernModelDirectory = Join-Path $projectRoot "src\main\resources\assets\poptartcore\models\block\quern"
$quernTextureDirectory = Join-Path $projectRoot "src\main\resources\assets\poptartcore\textures"
foreach ($modelPath in Get-ChildItem -LiteralPath $quernModelDirectory -Filter *.json) {
    $model = Get-Content -Raw -LiteralPath $modelPath.FullName | ConvertFrom-Json
    $textureKeys = @($model.textures.PSObject.Properties.Name)

    foreach ($texture in $model.textures.PSObject.Properties.Value) {
        if ($texture -like "wayfarer:*") {
            throw "$($modelPath.Name) still references a Wayfarer texture: $texture"
        }
        if ($texture -match "^poptartcore:(.+)$") {
            $texturePath = Join-Path $quernTextureDirectory ($Matches[1] + ".png")
            if (-not (Test-Path -LiteralPath $texturePath)) {
                throw "$($modelPath.Name) references a missing texture: $texture"
            }
        }
    }

    foreach ($element in $model.elements) {
        foreach ($face in $element.faces.PSObject.Properties.Value) {
            if ($face.texture -notlike "#*") {
                throw "$($modelPath.Name) has a face texture without a # reference: $($face.texture)"
            }
            $textureKey = $face.texture.Substring(1)
            if ($textureKeys -notcontains $textureKey) {
                throw "$($modelPath.Name) references undefined texture key: $($face.texture)"
            }
        }
    }
}

if (@($rotor.elements | Where-Object { $null -ne $_.PSObject.Properties["shade"] }).Count -ne 0) {
    throw "Rotor must use Minecraft's normal world-aware shading"
}
if ($flour.elements[0].from[1] -ne 9.99) { throw "Flour layer must begin at Y=9.99 in the updated Quern model" }
if (Test-Path -LiteralPath (Join-Path $projectRoot "src\main\resources\assets\poptartcore\models\item\flour.json")) {
    throw "Flour must remain a visual texture, not a registered item"
}
$recipe = Get-Content -Raw -LiteralPath (Join-Path $projectRoot "src\main\resources\data\poptartcore\recipe\grinding\flour_from_wheat.json") | ConvertFrom-Json
if ($recipe.ingredient.item -ne "minecraft:wheat" -or $recipe.result.id -ne "create:wheat_flour") {
    throw "The Quern flour recipe must convert minecraft:wheat to create:wheat_flour"
}
if ($recipe.powder_color -lt 0 -or $recipe.powder_color -gt 16777215) {
    throw "The Quern recipe powder_color must be an RGB color between 0 and 16777215"
}

$redDyeRecipePath = Join-Path $projectRoot "src/main/resources/data/poptartcore/recipe/grinding/red_dye_from_poppy.json"
$redDyeRecipe = Get-Content -LiteralPath $redDyeRecipePath -Raw | ConvertFrom-Json
if ($redDyeRecipe.ingredient.item -ne "minecraft:poppy" -or $redDyeRecipe.result.id -ne "minecraft:red_dye") {
    throw "The Quern red dye recipe must convert minecraft:poppy to minecraft:red_dye"
}
if ($redDyeRecipe.powder_color -lt 0 -or $redDyeRecipe.powder_color -gt 16777215) {
    throw "The Quern red dye powder_color must be an RGB color between 0 and 16777215"
}

$flowerRecipes = @{
    "red_dye_from_poppy.json" = @("minecraft:poppy", "minecraft:red_dye", 1)
    "yellow_dye_from_dandelion.json" = @("minecraft:dandelion", "minecraft:yellow_dye", 1)
    "yellow_dye_from_sunflower.json" = @("minecraft:sunflower", "minecraft:yellow_dye", 2)
    "red_dye_from_red_tulip.json" = @("minecraft:red_tulip", "minecraft:red_dye", 1)
    "red_dye_from_rose_bush.json" = @("minecraft:rose_bush", "minecraft:red_dye", 2)
    "light_blue_dye_from_blue_orchid.json" = @("minecraft:blue_orchid", "minecraft:light_blue_dye", 1)
    "magenta_dye_from_allium.json" = @("minecraft:allium", "minecraft:magenta_dye", 1)
    "magenta_dye_from_lilac.json" = @("minecraft:lilac", "minecraft:magenta_dye", 2)
    "light_gray_dye_from_azure_bluet.json" = @("minecraft:azure_bluet", "minecraft:light_gray_dye", 1)
    "light_gray_dye_from_oxeye_daisy.json" = @("minecraft:oxeye_daisy", "minecraft:light_gray_dye", 1)
    "light_gray_dye_from_white_tulip.json" = @("minecraft:white_tulip", "minecraft:light_gray_dye", 1)
    "orange_dye_from_orange_tulip.json" = @("minecraft:orange_tulip", "minecraft:orange_dye", 1)
    "orange_dye_from_torchflower.json" = @("minecraft:torchflower", "minecraft:orange_dye", 1)
    "pink_dye_from_pink_tulip.json" = @("minecraft:pink_tulip", "minecraft:pink_dye", 1)
    "pink_dye_from_peony.json" = @("minecraft:peony", "minecraft:pink_dye", 2)
    "blue_dye_from_cornflower.json" = @("minecraft:cornflower", "minecraft:blue_dye", 1)
    "white_dye_from_lily_of_the_valley.json" = @("minecraft:lily_of_the_valley", "minecraft:white_dye", 1)
    "black_dye_from_wither_rose.json" = @("minecraft:wither_rose", "minecraft:black_dye", 1)
    "cyan_dye_from_pitcher_plant.json" = @("minecraft:pitcher_plant", "minecraft:cyan_dye", 2)
}

$grindingRecipeDirectory = Join-Path $projectRoot "src\main\resources\data\poptartcore\recipe\grinding"
foreach ($entry in $flowerRecipes.GetEnumerator()) {
    $flowerRecipePath = Join-Path $grindingRecipeDirectory $entry.Key
    if (-not (Test-Path -LiteralPath $flowerRecipePath)) {
        throw "Missing flower grinding recipe: $($entry.Key)"
    }

    $flowerRecipe = Get-Content -Raw -LiteralPath $flowerRecipePath | ConvertFrom-Json
    $expectedInput, $expectedOutput, $expectedCount = $entry.Value
    if ($flowerRecipe.ingredient.item -ne $expectedInput -or
        $flowerRecipe.result.id -ne $expectedOutput -or
        $flowerRecipe.result.count -ne $expectedCount) {
        throw "Incorrect flower grinding conversion in $($entry.Key)"
    }
    if ($flowerRecipe.cranks -ne 4) {
        throw "Flower grinding recipe $($entry.Key) must require exactly 4 cranks"
    }
    if ($flowerRecipe.powder_color -lt 0 -or $flowerRecipe.powder_color -gt 16777215) {
        throw "Flower grinding recipe $($entry.Key) has an invalid powder_color"
    }
}

Write-Host "Quern files and JSON structure verified."
