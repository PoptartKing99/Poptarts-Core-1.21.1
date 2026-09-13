$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path

function Require-Text([string]$relativePath, [string[]]$patterns) {
    $path = Join-Path $projectRoot $relativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Missing Bee Smoker file: $relativePath"
    }

    $content = Get-Content -Raw -LiteralPath $path
    foreach ($pattern in $patterns) {
        if ($content -notmatch $pattern) {
            throw "Missing '$pattern' in $relativePath"
        }
    }
}

Require-Text 'src/main/java/dev/poptartking/poptartcore/beekeeping/BeeSmokerItem.java' @(
    'REACH = 5\.0D', 'SPRAY_RADIUS = 1\.25D', 'BEE_SMOKE_TICKS = 30', 'HIVE_SMOKE_TICKS = 100',
    'elapsed % 20 == 0', 'SmokedHives\.mark', 'CAMPFIRE_COSY_SMOKE',
    'SoundEvents\.EXPERIENCE_ORB_PICKUP', 'ParticleTypes\.HEART', 'ticks == HIVE_SMOKE_TICKS',
    'SmokedBees\.isSmoked', 'SmokedBees\.mark',
    '(?s)ParticleTypes\.HEART,.*?7,'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/beekeeping/SmokedHives.java' @(
    'SMOKED_DURATION = 300', 'level\.getGameTime\(\) > expiry'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/beekeeping/SmokedBees.java' @(
    'SMOKED_DURATION = 300', 'keepCalm', 'setRemainingPersistentAngerTime\(0\)'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/registry/PoptartCoreItems.java' @(
    'DeferredItem<BeeSmokerItem> BEE_SMOKER', 'stacksTo\(1\)\.durability\(360\)'
)
Require-Text 'src/main/resources/poptartcore.mixins.json' @(
    'bee\.BeehiveSmokedMixin', 'bee\.SmokedBeeMixin', 'bee\.BeeSmokerArmPoseMixin',
    'bee\.BeeSmokerReequipMixin'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/mixin/bee/SmokedBeeMixin.java' @(
    'customServerAiStep', '@At\("HEAD"\)', '@At\("TAIL"\)', 'SmokedBees\.keepCalm'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/mixin/bee/BeeSmokerReequipMixin.java' @(
    'shouldCauseReequipAnimation', 'BeeSmokerItem', 'setDamageValue\(0\)', 'ItemStack\.matches'
)

$recipePath = Join-Path $projectRoot 'src/main/resources/data/poptartcore/recipe/bee_smoker.json'
$recipe = Get-Content -Raw -LiteralPath $recipePath | ConvertFrom-Json
if (($recipe.pattern -join '/') -ne ' I /IFC/ I ' -or
    $recipe.key.I.item -ne 'minecraft:iron_ingot' -or
    $recipe.key.F.item -ne 'minecraft:campfire' -or
    $recipe.key.C.item -ne 'farmersdelight:canvas' -or
    $recipe.result.id -ne 'poptartcore:bee_smoker') {
    throw 'Bee Smoker recipe does not match the agreed layout'
}

foreach ($relativePath in @(
    'src/main/resources/assets/poptartcore/models/item/bee_smoker.json',
    'src/main/resources/assets/poptartcore/models/item/bee_smoker_gui.json',
    'src/main/resources/assets/poptartcore/models/item/bee_smoker_3d.json',
    'src/main/resources/assets/poptartcore/textures/item/bee_smoker.png',
    'src/main/resources/assets/poptartcore/textures/item/bee_smoker_model.png'
)) {
    $path = Join-Path $projectRoot $relativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Missing Bee Smoker asset: $relativePath"
    }
}

Write-Output 'Bee Smoker behavior, registration, recipe, mixins, and assets verified.'
