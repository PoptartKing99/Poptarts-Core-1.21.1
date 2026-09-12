$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path

function Require-File([string]$relativePath) {
    $path = Join-Path $projectRoot $relativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Missing Portable Engine file: $relativePath"
    }
    return $path
}

function Require-Text([string]$relativePath, [string[]]$patterns) {
    $content = Get-Content -Raw -LiteralPath (Require-File $relativePath)
    foreach ($pattern in $patterns) {
        if ($content -notmatch $pattern) {
            throw "Missing '$pattern' in $relativePath"
        }
    }
}

Require-Text 'src/main/java/dev/poptartking/poptartcore/registry/PoptartCoreBlocks.java' @(
    'DeferredBlock<PortableEngineBlock> PORTABLE_ENGINE', 'strength\(3\.0F, 6\.0F\)', 'SoundType\.COPPER'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/integration/simulated/PortableEngineSetup.java' @(
    'CAPACITIES\.register', '\(DoubleSupplier\) \(\) -> 64\.0', 'new GeneratedRpm\(32, false\)'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/mixin/integration/simulated/PortableEngineFuelMixin.java' @(
    'PORTABLE_ENGINE_ALLOWED_FUEL'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/mixin/integration/simulated/PortableEngineDyeingMixin.java' @(
    'RED_PORTABLE_ENGINE', 'PoptartCoreBlocks\.PORTABLE_ENGINE'
)
Require-Text 'src/main/resources/poptartcore.mixins.json' @(
    'PortableEngineBlockMixin', 'PortableEngineDyeingMixin', 'PortableEngineFuelInventoryMixin', 'PortableEngineFuelMixin'
)
Require-Text 'src/main/templates/META-INF/neoforge.mods.toml' @('modId="aeronautics"', 'versionRange="\[1\.3\.1,\)"')

$recipe = Get-Content -Raw -LiteralPath (Require-File 'src/main/resources/data/poptartcore/recipe/portable_engine.json') | ConvertFrom-Json
if (($recipe.pattern -join '/') -ne 'ISI/IBI/PTP' -or
    $recipe.key.I.item -ne 'minecraft:iron_ingot' -or
    $recipe.key.S.item -ne 'create:iron_sheet' -or
    $recipe.key.B.item -ne 'aeronautics:adjustable_burner' -or
    $recipe.key.P.tag -ne 'minecraft:planks' -or
    $recipe.key.T.item -ne 'poptartcore:steel_ingot' -or
    $recipe.result.id -ne 'poptartcore:portable_engine') {
    throw 'Portable Engine recipe does not match the agreed 3 x 3 layout'
}

foreach ($file in @(
    'src/main/resources/assets/poptartcore/blockstates/portable_engine.json',
    'src/main/resources/assets/poptartcore/models/block/portable_engine.json',
    'src/main/resources/assets/poptartcore/models/item/portable_engine.json',
    'src/main/resources/assets/poptartcore/textures/block/portable_engine.png',
    'src/main/resources/data/poptartcore/loot_table/blocks/portable_engine.json',
    'src/main/resources/data/poptartcore/tags/item/portable_engine_allowed_fuel.json'
)) {
    Require-File $file | Out-Null
}

Write-Output 'Portable Engine registration, integration, recipe, assets, fuel rules, and dependency verified.'
