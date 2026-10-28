#requires -Version 7.0
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$serverLog = Join-Path $repo 'build/game-test-run/logs/latest.log'
if (-not (Test-Path -LiteralPath $serverLog) -or (Get-Content -Raw -LiteralPath $serverLog) -notmatch 'All \d+ required tests passed') {
    throw 'Required server GameTests have not passed. Run scripts/verify-mod.ps1 first.'
}
$checks = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'verification/feature-checks.json') -Raw | ConvertFrom-Json
$shellPath = (Get-Process -Id $PID).Path
$results = [Collections.Generic.List[object]]::new()
Push-Location -LiteralPath $repo
try {
    foreach ($check in $checks) {
        # Each check has its own scope and exit code. None launch clients or change mod files.
        $output = & $shellPath -NoLogo -NoProfile -File (Join-Path $repo $check) 2>&1
        $exitCode = $LASTEXITCODE
        $results.Add([PSCustomObject]@{ path = $check; exit = $exitCode; output = ($output -join "`n") })
    }
    $reportDirectory = Join-Path $repo 'build/verification'
    New-Item -ItemType Directory -Force -Path $reportDirectory | Out-Null
    $results | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $reportDirectory 'feature-checks.json')
    $failed = @($results | Where-Object exit -ne 0)
    if ($failed.Count) { throw (($failed | ForEach-Object { "$($_.path):`n$($_.output)" }) -join "`n`n") }
    Write-Output "All $($results.Count) read-only feature checks passed."
} finally { Pop-Location }
