$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$miningSource = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/hammer/HammerMining.java")
$eventsSource = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/hammer/BlockBreakingEvents.java")
$serverMixin = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/mixin/hammer/ServerBlockBreakingMixin.java")
$clientMixin = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/mixin/hammer/ClientHammerFaceMixin.java")
$clientEvents = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/client/ClientEvents.java")
$mixinConfig = Get-Content -Raw (Join-Path $projectRoot "src/main/resources/poptartcore.mixins.json")
$noSpreadTag = Get-Content -Raw (Join-Path $projectRoot "src/main/resources/data/poptartcore/tags/block/hammer_no_spread.json")

function Assert-Contains([string] $Text, [string] $Expected, [string] $Message) {
    if (-not $Text.Contains($Expected)) {
        throw $Message
    }
}

function Get-ExpectedAreaMiningTicks([double[]] $ProgressRates) {
    $miningTicks = ($ProgressRates | ForEach-Object {
        if ($_ -gt 0.0 -and $_ -lt 1.0) { [Math]::Ceiling(1.0 / $_) } else { 0 }
    } | Measure-Object -Sum).Sum
    return $miningTicks + 5 * ($ProgressRates.Count - 1)
}

Assert-Contains $miningSource "firstOffset = -1; firstOffset <= 1" "The first 3x3 axis is missing."
Assert-Contains $miningSource "secondOffset = -1; secondOffset <= 1" "The second 3x3 axis is missing."
Assert-Contains $miningSource "new MiningSession(face, !player.isShiftKeyDown())" "The mining mode must be locked when mining begins."
Assert-Contains $miningSource "CLIENT_MINING_SESSIONS" "Client and integrated-server mining sessions must not share one map."
Assert-Contains $miningSource "SERVER_MINING_SESSIONS" "Client and integrated-server mining sessions must not share one map."
Assert-Contains $miningSource "player.level().isClientSide" "Mining sessions must be selected by logical side."
Assert-Contains $miningSource "session.areaMining()" "Area mining must use the locked mode instead of the live Shift state."
Assert-Contains $miningSource "totalTicks += Math.ceil(1.0 / progress)" "Each block's mining time must be rounded to whole ticks."
Assert-Contains $miningSource "DESTROY_DELAY_TICKS * (targets.size() - 1)" "The normal delay between separately mined blocks is missing."
Assert-Contains $miningSource "state.getFluidState().isEmpty()" "Fluid blocks must be excluded."
Assert-Contains $miningSource "state.canHarvestBlock" "Blocks the hammer cannot harvest must be excluded."
Assert-Contains $miningSource "level.getBlockState(center).is(PoptartCoreTags.HAMMER_NO_SPREAD)" "A no-spread center block must disable area mining."
Assert-Contains $miningSource "!state.is(PoptartCoreTags.HAMMER_NO_SPREAD)" "No-spread blocks must be excluded from surrounding targets."
Assert-Contains $noSpreadTag '"poptartcore:iron_bloom"' "Iron blooms must not trigger or receive hammer area mining."
# Persistent overlay IDs are checked by verify-persistent-block-damage.ps1 and CrackRenderIdsTest.
Assert-Contains $eventsSource "PlayerLoggedOutEvent" "Hammer mining sessions must be cleaned up when players log out."
Assert-Contains $eventsSource "clearActiveMining(player, player.serverLevel())" "Logout must use the shared mining cleanup path."
Assert-Contains $eventsSource "HammerMining.endMining(player)" "Lifecycle cleanup must remove the player's mining session."
Assert-Contains $eventsSource "poptartcore`$setDestroyingBlock(false)" "Logout must cancel active server mining before removing the hammer session."
Assert-Contains $eventsSource "poptartcore`$setDelayedDestroy(false)" "Logout must cancel delayed server mining before removing the hammer session."
Assert-Contains $serverMixin "progress.accrue(target, rate, rate, true" "Surrounding persistent damage is not synchronized."
Assert-Contains $serverMixin "gameMode.destroyBlock(target)" "Surrounding blocks are not using vanilla breaking."
Assert-Contains $clientMixin "HammerMining.beginMining" "The client is not recording the selected mining plane and mode."
Assert-Contains $clientMixin 'method = "stopDestroyBlock"' "Stopping client-side mining must clear hammer state."
Assert-Contains $clientMixin "poptartcore`$breakerId, poptartcore`$hammerCenter, -1" "Stopping client-side mining must clear the vanilla center-block crack overlay."
Assert-Contains $clientMixin "HammerMining.endMining(minecraft.player)" "Client cleanup must remove its mining session."
Assert-Contains $clientEvents "ClientPlayerNetworkEvent.LoggingOut" "Client logout must clear hammer crack overlays before the level renderer is discarded."
Assert-Contains $clientEvents "cleanup.poptartcore`$clearActiveMining()" "Client logout must clear only the active vanilla mining overlay."
Assert-Contains $mixinConfig '"hammer.HammerDestroyProgressMixin"' "The speed mixin is not registered."
Assert-Contains $mixinConfig '"hammer.ServerBlockBreakingMixin"' "The server mining mixin is not registered."
Assert-Contains $mixinConfig '"hammer.ClientHammerFaceMixin"' "The client face mixin is not registered."

if ((Get-ExpectedAreaMiningTicks (1..9 | ForEach-Object { 0.1 })) -ne 130) {
    throw "Nine ten-tick blocks must take 90 mining ticks plus 40 delay ticks."
}

if ((Get-ExpectedAreaMiningTicks @(0.1, 0.2)) -ne 20) {
    throw "Mixed block times must be rounded separately before adding the normal delay."
}

Write-Host "Hammer area-mining verification passed."
