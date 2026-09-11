$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$sourceRoot = Join-Path $projectRoot 'src/main/java/dev/poptartking/poptartcore'

function Assert-SourceContains($relativePath, $pattern, $message) {
    $source = Get-Content -Raw -LiteralPath (Join-Path $sourceRoot $relativePath)
    if ($source -notmatch $pattern) { throw $message }
}

Assert-SourceContains 'PoptartCore.java' 'PoptartCoreCapabilities\.register\(modEventBus\)' 'Missing capability event registration'
foreach ($block in @('CRUCIBLE', 'QUERN', 'WORKBENCH')) {
    Assert-SourceContains 'registry/PoptartCoreCapabilities.java' "PoptartCoreBlockEntities\.$block\.get\(\)" "Missing $block inventory provider"
}
Assert-SourceContains 'registry/PoptartCoreCapabilities.java' 'side == null \? null : new SidedInvWrapper\(crucible, side\)' 'Crucible must retain sided routing'
Assert-SourceContains 'registry/PoptartCoreCapabilities.java' 'new QuernItemHandler\(quern\)' 'Quern must use its output-restricted adapter'
Assert-SourceContains 'quern/QuernItemHandler.java' 'slot != QuernBlockEntity\.OUTPUT_SLOT' 'Quern adapter must block input extraction'
Assert-SourceContains 'quern/QuernItemHandler.java' 'super\.extractItem\(slot, amount, simulate\)' 'Quern extraction must preserve simulation'
Assert-SourceContains 'registry/PoptartCoreCapabilities.java' 'new InvWrapper\(workbench\)' 'Workbench must expose its real inventory rather than its computed result'
Write-Host 'Automation registration and adapter source checks passed. These checks do not replace in-game transfer tests.'
