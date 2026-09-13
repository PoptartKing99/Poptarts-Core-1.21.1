$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path

function Require-Text([string]$relativePath, [string[]]$patterns) {
    $path = Join-Path $projectRoot $relativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Missing beekeeper armor file: $relativePath"
    }

    $content = Get-Content -Raw -LiteralPath $path
    foreach ($pattern in $patterns) {
        if ($content -notmatch $pattern) {
            throw "Missing '$pattern' in $relativePath"
        }
    }
}

Require-Text 'src/main/java/dev/poptartking/poptartcore/registry/PoptartCoreArmorMaterials.java' @(
    'BEEKEEPER_ARMOR_MATERIAL', 'protection\(1, 3, 2, 1\)', 'ARMOR_EQUIP_LEATHER'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/registry/PoptartCoreItems.java' @(
    'BEEKEEPER_HELMET', 'durability\(200\)', 'BEEKEEPER_CHESTPLATE', 'durability\(300\)',
    'BEEKEEPER_LEGGINGS', 'durability\(260\)', 'BEEKEEPER_BOOTS'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/beekeeping/BeekeeperEvents.java' @(
    '0\.05F \* pieces', '0\.15F \* pieces', 'SENSITIVE_TO_BANE_OF_ARTHROPODS'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/mixin/bee/BeeStingMixin.java' @(
    '0\.25F \* pieces', 'hurtAndBreak', 'setReturnValue\(false\)'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/armor/client/BeekeeperArmorModel.java' @(
    'LayerDefinition\.create\(mesh, 64, 64\)', 'hood', 'jacket', 'skirt', 'right_sleeve', 'right_boot'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/mixin/armor/FirstPersonBeekeeperSleeveMixin.java' @(
    'renderRightHand', 'renderLeftHand', 'textures/armor/beekeeper_armor\.png'
)
Require-Text 'src/main/resources/poptartcore.mixins.json' @(
    'bee\.BeeStingMixin', 'effect\.MobEffectInstanceAccessor', 'armor\.FirstPersonBeekeeperSleeveMixin'
)

foreach ($relativePath in @(
    'src/main/resources/assets/poptartcore/textures/armor/beekeeper_armor.png',
    'src/main/resources/assets/poptartcore/textures/item/beekeeper_helmet.png',
    'src/main/resources/assets/poptartcore/textures/item/beekeeper_chestplate.png',
    'src/main/resources/assets/poptartcore/textures/item/beekeeper_leggings.png',
    'src/main/resources/assets/poptartcore/textures/item/beekeeper_boots.png',
    'src/main/resources/assets/poptartcore/models/item/beekeeper_helmet.json',
    'src/main/resources/assets/poptartcore/models/item/beekeeper_chestplate.json',
    'src/main/resources/assets/poptartcore/models/item/beekeeper_leggings.json',
    'src/main/resources/assets/poptartcore/models/item/beekeeper_boots.json'
)) {
    if (-not (Test-Path -LiteralPath (Join-Path $projectRoot $relativePath) -PathType Leaf)) {
        throw "Missing beekeeper armor asset: $relativePath"
    }
}

$recipes = Get-ChildItem -Recurse -File -LiteralPath (Join-Path $projectRoot 'src\main\resources\data') |
    Where-Object { $_.Name -match '^beekeeper_(helmet|chestplate|leggings|boots)\.json$' -and $_.FullName -match '[\\/]recipe[s]?[\\/]' }
if ($recipes) {
    throw 'Beekeeper armor recipes were added despite the agreed no-recipe scope'
}

Write-Output 'Beekeeper armor items, effects, model, textures, tags, and no-recipe scope verified.'
