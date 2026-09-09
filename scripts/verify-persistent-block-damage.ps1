$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$progressSource = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/hammer/BlockBreakProgress.java")
$eventsSource = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/hammer/BlockBreakingEvents.java")
$serverMixin = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/mixin/item/ServerBlockBreakingMixin.java")
$clientMixin = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/mixin/item/ClientHammerFaceMixin.java")
$mixinConfig = Get-Content -Raw (Join-Path $projectRoot "src/main/resources/poptartcore.mixins.json")

function Assert-Contains([string] $Text, [string] $Expected, [string] $Message) {
    if (-not $Text.Contains($Expected)) {
        throw $Message
    }
}

function Assert-NotContains([string] $Text, [string] $Unexpected, [string] $Message) {
    if ($Text.Contains($Unexpected)) {
        throw $Message
    }
}

function Get-NextFraction([double] $SavedFraction, [double] $Rate) {
    $gameTicks = 100
    $earnedTicks = [Math]::Round([Math]::Min($SavedFraction, 1.0) / $Rate)
    $startTick = $gameTicks - [Math]::Max(0, $earnedTicks - 1)
    return $Rate * (($gameTicks + 1) - $startTick + 1)
}

Assert-Contains $progressSource 'FILE_NAME = "poptartcore_block_break_progress"' "Persistent damage needs its own saved-world data file."
Assert-Contains $progressSource "computeIfAbsent" "Persistent damage is not loaded through SavedData."
Assert-Contains $progressSource 'tag.put("cracks", entries)' "Persistent damage is not saved."
Assert-Contains $progressSource "resumedStart" "Saved damage is not resumed when mining restarts."
Assert-Contains $progressSource "fractionAt" "The server cannot determine when restored damage reaches completion."
Assert-Contains $progressSource "beginAttempt" "Mining attempts do not distinguish fresh blocks from resumed damage."
Assert-Contains $progressSource "isResumedAttempt" "Fresh blocks cannot be excluded from server-forced completion."
Assert-Contains $progressSource "crack.fraction -= Math.min" "Abandoned damage does not decay."
Assert-Contains $progressSource "crack.hide(level)" "Expired damage does not remove its crack overlay."
Assert-Contains $eventsSource "LevelTickEvent.Post" "Persistent damage is not ticked by the server."
Assert-Contains $serverMixin 'method = "handleBlockBreakAction", at = @At("RETURN")' "Saved damage must be resumed once after a mining attempt starts."
Assert-Contains $serverMixin "resumedStart" "Server mining does not resume saved damage."
Assert-NotContains $eventsSource "progress.resumedStart" "Saved damage must not be reapplied every server tick."
Assert-Contains $eventsSource "progress.isResumedAttempt(player.getUUID(), pos)" "Fresh mining must retain vanilla completion timing."
Assert-Contains $eventsSource "progress.fractionAt(pos) >= 1.0F" "Restored mining progress cannot finish the block server-side."
Assert-Contains $eventsSource "player.gameMode.destroyBlock(pos)" "Completed restored damage does not use vanilla block breaking."
Assert-Contains $serverMixin "progress.record(pos, fraction, rate, areaMining" "The hammer center must use the same decay rate as its surrounding targets."
Assert-Contains $serverMixin "progress.record(target, fraction, rate, true" "Hammer target damage is not recorded."
Assert-Contains $serverMixin "progress.clear(pos)" "Destroyed blocks do not clear saved damage."
Assert-Contains $clientMixin "poptartcore`$breakerId, poptartcore`$hammerCenter, -1" "Logout does not clear the stale vanilla player overlay."
Assert-Contains $mixinConfig '"item.GameModeDestroyAccessor"' "The mining progress accessor is not registered."
Assert-Contains $mixinConfig '"item.ServerBlockBreakingMixin"' "The persistent mining mixin is not registered."

if ([Math]::Abs((Get-NextFraction 0.1 0.1) - 0.2) -gt 0.0001) {
    throw "Continuous mining must advance by exactly one normal tick."
}

if ([Math]::Abs((Get-NextFraction 0.5 0.1) - 0.6) -gt 0.0001) {
    throw "Resumed mining must add exactly one tick to its saved progress."
}

Write-Host "Persistent block-damage verification passed."
