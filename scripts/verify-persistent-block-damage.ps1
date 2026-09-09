$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$progressSource = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/hammer/BlockBreakProgress.java")
$eventsSource = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/hammer/BlockBreakingEvents.java")
$clientEventsSource = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/client/ClientEvents.java")
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

function Get-GroupResumeFraction([double[]] $Fractions) {
    return ($Fractions | Measure-Object -Minimum).Minimum
}

Assert-Contains $progressSource 'FILE_NAME = "poptartcore_block_break_progress"' "Persistent damage needs its own saved-world data file."
Assert-Contains $progressSource "computeIfAbsent" "Persistent damage is not loaded through SavedData."
Assert-Contains $progressSource 'tag.put("cracks", entries)' "Persistent damage is not saved."
Assert-Contains $progressSource "resumedStart" "Saved damage is not resumed when mining restarts."
Assert-Contains $progressSource "fractionAt" "The server cannot determine when restored damage reaches completion."
Assert-Contains $progressSource "beginAttempt" "Mining attempts do not distinguish fresh blocks from resumed damage."
Assert-Contains $progressSource "isCompletedResumedAttempt" "Fresh blocks cannot be excluded from server-forced completion."
Assert-Contains $progressSource "crack.fraction + amount" "Hammer targets must gain only newly earned damage."
Assert-Contains $progressSource "crack.block != level.getBlockState(pos).getBlock()" "Saved damage can transfer to a different replacement block."
Assert-Contains $progressSource 'tag.putString(BLOCK_KEY, BuiltInRegistries.BLOCK.getKey(block).toString())' "Saved damage does not remember its original block type."
Assert-Contains $progressSource "state.getBlock() == crack.block" "Damage for a replaced block is not discarded during decay."
Assert-Contains $eventsSource "BlockEvent.EntityPlaceEvent" "Replacing a block with the same block type does not clear old damage."
Assert-Contains $progressSource "crack.fraction -= Math.min" "Abandoned damage does not decay."
Assert-Contains $progressSource "crack.hide(level)" "Expired damage does not remove its crack overlay."
Assert-Contains $progressSource "if (!level.isLoaded(crack.pos))" "Persistent damage can access unloaded chunk contents."
Assert-NotContains $progressSource "for (Crack crack : progress.cracks.values()) {`r`n            crack.show(level);" "Loading saved damage must not render cracks in unloaded chunks."
Assert-Contains $eventsSource "LevelTickEvent.Post" "Persistent damage is not ticked by the server."
Assert-Contains $eventsSource "PlayerChangedDimensionEvent" "Dimension changes do not clear active server mining state."
Assert-Contains $eventsSource "LivingDeathEvent" "Player death does not clear active server mining state."
Assert-Contains $eventsSource "server.getLevel(event.getFrom())" "Dimension cleanup must clear the attempt from the previous level."
Assert-Contains $eventsSource "poptartcore`$setDestroyingBlock(false)" "Lifecycle cleanup must cancel active server mining."
Assert-Contains $eventsSource "poptartcore`$setDelayedDestroy(false)" "Lifecycle cleanup must cancel delayed server mining."
Assert-Contains $clientEventsSource "ClientPlayerNetworkEvent.Clone" "Respawning does not clear active client mining state."
Assert-Contains $clientEventsSource "cleanup.poptartcore`$clearActiveMining()" "Client lifecycle cleanup does not clear crack overlays."
Assert-Contains $serverMixin 'method = "handleBlockBreakAction", at = @At("RETURN")' "Saved damage must be resumed once after a mining attempt starts."
Assert-Contains $serverMixin "resumedStart" "Server mining does not resume saved damage."
Assert-NotContains $eventsSource "progress.resumedStart" "Saved damage must not be reapplied every server tick."
Assert-Contains $eventsSource "progress.isCompletedResumedAttempt(player.getUUID(), pos)" "Fresh mining must retain vanilla completion timing."
Assert-Contains $eventsSource "player.gameMode.destroyBlock(pos)" "Completed restored damage does not use vanilla block breaking."
Assert-Contains $serverMixin "savedFraction = Math.min(savedFraction, progress.fractionAt(target))" "A shifted hammer area must resume from its least-damaged block."
Assert-Contains $serverMixin "progress.updateAttempt(player.getUUID(), pos, fraction)" "Area completion must track the current attempt instead of old center damage."
Assert-Contains $serverMixin "progress.record(pos, fraction, rate, areaMining" "The hammer center must use the same decay rate as its surrounding targets."
Assert-Contains $serverMixin "progress.accrue(target, rate, rate, true" "Hammer targets must not inherit the center's old absolute damage."
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

if ([Math]::Abs((Get-GroupResumeFraction @(0.9, 0.0, 0.0)) - 0.0) -gt 0.0001) {
    throw "Moving a damaged edge block into a fresh area must not spread its old damage."
}

if ([Math]::Abs((Get-GroupResumeFraction @(0.9, 0.9, 0.9)) - 0.9) -gt 0.0001) {
    throw "Returning to the same damaged area must resume its shared progress."
}

$chunkGuardIndex = $progressSource.IndexOf("if (!level.isLoaded(crack.pos))")
$chunkBlockReadIndex = $progressSource.IndexOf("BlockState state = level.getBlockState(crack.pos)")
if ($chunkGuardIndex -lt 0 -or $chunkBlockReadIndex -lt 0 -or $chunkGuardIndex -gt $chunkBlockReadIndex) {
    throw "The loaded-chunk guard must run before reading the damaged block state."
}

Write-Host "Persistent block-damage verification passed."
