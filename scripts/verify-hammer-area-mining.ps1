$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$miningSource = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/hammer/HammerMining.java")
$serverMixin = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/mixin/item/ServerHammerMiningMixin.java")
$clientMixin = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/mixin/item/ClientHammerFaceMixin.java")
$mixinConfig = Get-Content -Raw (Join-Path $projectRoot "src/main/resources/poptartcore.mixins.json")

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
Assert-Contains $miningSource "session.areaMining()" "Area mining must use the locked mode instead of the live Shift state."
Assert-Contains $miningSource "totalTicks += Math.ceil(1.0 / progress)" "Each block's mining time must be rounded to whole ticks."
Assert-Contains $miningSource "DESTROY_DELAY_TICKS * (targets.size() - 1)" "The normal delay between separately mined blocks is missing."
Assert-Contains $miningSource "state.getFluidState().isEmpty()" "Fluid blocks must be excluded."
Assert-Contains $miningSource "state.canHarvestBlock" "Blocks the hammer cannot harvest must be excluded."
Assert-Contains $miningSource "pos.hashCode() ^ HAMMER_CRACK_ID_SALT" "Each surrounding block needs its own crack-render ID."
Assert-Contains $serverMixin "level.destroyBlockProgress(HammerMining.crackId(target), target, stage)" "Surrounding break progress is not synchronized."
Assert-Contains $serverMixin "level.destroyBlockProgress(HammerMining.crackId(target), target, -1)" "Surrounding break progress is not cleared by its matching ID."
Assert-Contains $serverMixin "gameMode.destroyBlock(target)" "Surrounding blocks are not using vanilla breaking."
Assert-Contains $clientMixin "HammerMining.beginMining" "The client is not recording the selected mining plane and mode."
Assert-Contains $mixinConfig '"item.HammerDestroyProgressMixin"' "The speed mixin is not registered."
Assert-Contains $mixinConfig '"item.ServerHammerMiningMixin"' "The server mining mixin is not registered."
Assert-Contains $mixinConfig '"item.ClientHammerFaceMixin"' "The client face mixin is not registered."

if ((Get-ExpectedAreaMiningTicks (1..9 | ForEach-Object { 0.1 })) -ne 130) {
    throw "Nine ten-tick blocks must take 90 mining ticks plus 40 delay ticks."
}

if ((Get-ExpectedAreaMiningTicks @(0.1, 0.2)) -ne 20) {
    throw "Mixed block times must be rounded separately before adding the normal delay."
}

Write-Host "Hammer area-mining verification passed."
