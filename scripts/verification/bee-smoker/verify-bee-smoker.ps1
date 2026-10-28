$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/../../lib/resources.ps1"
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path

function Require-Text([string]$relativePath, [string[]]$patterns) {
    $path = Resolve-ModFile (Join-Path $projectRoot $relativePath)
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
    'PUMP_CYCLE_TICKS = 40', 'PARTICLE_DURATION_TICKS = 30', 'RETRACT_DELAY_TICKS = 15',
    'PoptartCoreSounds\.BEE_SMOKER_BLOW', 'PoptartCoreSounds\.BEE_SMOKER_RETRACT',
    'pumpTick < PARTICLE_DURATION_TICKS',
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




Require-Text 'src/main/java/dev/poptartking/poptartcore/mixin/bee/SmokedBeeMixin.java' @(
    'customServerAiStep', '@At\("HEAD"\)', '@At\("TAIL"\)', 'SmokedBees\.keepCalm'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/mixin/bee/BeeSmokerReequipMixin.java' @(
    'shouldCauseReequipAnimation', 'BeeSmokerItem', 'setDamageValue\(0\)', 'ItemStack\.matches'
)

Write-Output 'Bee Smoker range, timing and client-use source audits passed. Stats, crafting, assets, sounds and mixin packaging use shared verification.'
