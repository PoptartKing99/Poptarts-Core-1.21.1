$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path

function Require-Text([string]$relativePath, [string[]]$patterns) {
    $path = Join-Path $projectRoot $relativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Missing bee behavior file: $relativePath"
    }

    $content = Get-Content -Raw -LiteralPath $path
    foreach ($pattern in $patterns) {
        if ($content -notmatch $pattern) {
            throw "Missing '$pattern' in $relativePath"
        }
    }
}

Require-Text 'src/main/java/dev/poptartking/poptartcore/mixin/bee/BeeFlightMixin.java' @(
    'setNoGravity\(true\)', 'hasSkyLight\(\)', 'isNight\(\)', 'isRaining\(\)'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/mixin/bee/BeehiveSkylightMixin.java' @(
    'BeehiveBlockEntity', 'hasSkyLight\(\)', 'isNight\(\)', 'isRaining\(\)'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/mixin/bee/TurtleEggBeeMixin.java' @(
    'original && !\(entity instanceof Bee\)'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/mixin/bee/BeeFlipMixin.java' @(
    'LivingEntityRenderer', 'entity instanceof Bee \? 180\.0F : original'
)
Require-Text 'src/main/resources/poptartcore.mixins.json' @(
    'bee\.BeeFlightMixin', 'bee\.BeehiveSkylightMixin', 'bee\.TurtleEggBeeMixin', 'bee\.BeeFlipMixin'
)

Write-Output 'Bee flight, hive weather, turtle egg, and death rotation behavior verified.'
