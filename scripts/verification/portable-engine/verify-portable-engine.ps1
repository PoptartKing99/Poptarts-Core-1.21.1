$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/../../lib/resources.ps1"
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path

function Require-File([string]$relativePath) {
    $path = Resolve-ModFile (Join-Path $projectRoot $relativePath)
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

Require-Text 'src/main/java/dev/poptartking/poptartcore/registry/PoptartCreateBlocks.java' @(
    'CatalogBlockDefinition<PortableEngineBlock> PORTABLE_ENGINE', 'strength\(3\.0F, 6\.0F\)', 'SoundType\.COPPER',
    'PortableEngineBlock\.isLitState\(state\) \? 6 : 0'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/integration/simulated/PortableEngineSetup.java' @(
    'CAPACITIES\.register', '\(DoubleSupplier\) \(\) -> 64\.0', 'new GeneratedRpm\(32, false\)'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/mixin/integration/simulated/PortableEngineFuelMixin.java' @(
    'PORTABLE_ENGINE_ALLOWED_FUEL'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/mixin/integration/simulated/PortableEngineDyeingMixin.java' @(
    'RED_PORTABLE_ENGINE', 'PoptartCreateBlocks\.PORTABLE_ENGINE'
)



Write-Output 'Portable Engine integration, lighting, fuel and dye source audits passed. Recipe, assets, mixins and dependency metadata use shared verification.'
