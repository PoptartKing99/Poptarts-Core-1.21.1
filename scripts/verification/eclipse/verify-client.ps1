#requires -Version 7.0
param([switch] $RunClient, [switch] $WithClientRenderMods, [switch] $WithBetterDays)
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '../../..')).Path
$init = 'scripts/verification/eclipse/client-check.gradle'
Push-Location -LiteralPath $repo
try {
    & ./gradlew.bat compileNavigationTestJava -PnavigationTests -I $init --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Eclipse client test compilation failed.' }
    if ($RunClient) {
        $run = Join-Path $repo 'build/eclipse-client-run'
        $world = Join-Path $run 'saves/navigation-smoke'
        $sourceWorld = Join-Path $repo 'build/game-test-run/world'
        if (-not (Test-Path -LiteralPath (Join-Path $sourceWorld 'level.dat'))) {
            throw 'Run scripts/verify-mod.ps1 first to prepare the isolated server test world.'
        }
        New-Item -ItemType Directory -Force -Path (Join-Path $run 'mods'), (Join-Path $run 'saves') | Out-Null
        $lock = Get-Content -Raw -LiteralPath (Join-Path $repo 'gradle/development/mods-lock.json') | ConvertFrom-Json
        $optionalClientMods = @('sodium-neoforge-0.8.13+mc1.21.1.jar', 'polytone-1.21-4.2.0-neoforge.jar')
        $betterDaysJar = 'betterdays-1.21.1-3.3.6.3-NEOFORGE.jar'
        $clientMods = @()
        if ($WithClientRenderMods) {
            $clientMods = $optionalClientMods
        }
        if ($WithBetterDays) { $clientMods += $betterDaysJar }
        foreach ($name in $clientMods) {
            if (-not (Test-Path -LiteralPath (Join-Path $repo "run/mods/$name"))) { throw "Missing local runtime mod: $name" }
        }
        # These are the only optional files this runner stages; clear them when switching to baseline rendering.
        foreach ($name in @($optionalClientMods) + @($betterDaysJar)) {
            $staged = Join-Path $run "mods/$name"
            if ($name -notin $clientMods -and (Test-Path -LiteralPath $staged)) {
                Remove-Item -LiteralPath $staged
            }
        }
        foreach ($entry in $lock.mods) {
            $source = Join-Path $repo "build/game-test-run/mods/$($entry.file)"
            if (-not (Test-Path -LiteralPath $source) -or (Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash -ne $entry.sha256) {
                throw "Missing or mismatched isolated runtime mod: $($entry.file). Run full verification first."
            }
        }
        foreach ($existing in Get-ChildItem -LiteralPath (Join-Path $run 'mods') -File -Filter '*.jar') {
            if ($existing.Name -notin $lock.mods.file -and $existing.Name -notin $clientMods) {
                throw "Unexpected isolated runtime mod: $($existing.Name)"
            }
        }
        foreach ($entry in $lock.mods) {
            Copy-Item -LiteralPath (Join-Path $repo "build/game-test-run/mods/$($entry.file)") -Destination (Join-Path $run 'mods')
        }
        foreach ($name in $clientMods) {
            $source = Join-Path $repo "run/mods/$name"
            $destination = Join-Path $run "mods/$name"
            Copy-Item -LiteralPath $source -Destination $destination
            if ((Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash -ne (Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash) {
                throw "Client mod copy mismatch: $name"
            }
        }
        if ($WithBetterDays) {
            $timeConfig = Join-Path $repo 'run/config/betterdays-common.toml'
            if (-not (Test-Path -LiteralPath $timeConfig)) { throw "Missing development time config: $timeConfig" }
            New-Item -ItemType Directory -Force -Path (Join-Path $run 'config') | Out-Null
            Copy-Item -LiteralPath $timeConfig -Destination (Join-Path $run 'config/betterdays-common.toml')
        }
        if (-not (Test-Path -LiteralPath (Join-Path $world 'level.dat'))) {
            Copy-Item -LiteralPath $sourceWorld -Destination $world -Recurse
        }
        Set-Content -LiteralPath (Join-Path $run 'options.txt') -Value @('pauseOnLostFocus:false', 'renderDistance:8', 'simulationDistance:5', 'maxFps:60')
        try {
            & ./gradlew.bat runClient -PnavigationTests -I $init --console=plain *> (Join-Path $run 'gradle-client.log')
            if ($LASTEXITCODE -ne 0) { throw "Eclipse client failed. See $run/gradle-client.log" }
        } finally {
            & ./gradlew.bat prepareClientRun --console=plain
            if ($LASTEXITCODE -ne 0) { throw 'Could not restore normal client launch arguments.' }
        }
        $log = Get-Content -Raw -LiteralPath (Join-Path $run 'logs/latest.log')
        if ($log -notmatch 'ECLIPSE_CLIENT_PASS' -or $log -match 'ECLIPSE_CLIENT_FAIL') {
            throw "Eclipse client assertions failed. See $run/logs/latest.log"
        }
        if ($WithBetterDays -and $log -notmatch 'ECLIPSE_BETTER_DAYS_PASS') {
            throw "Better Days timing and bed checks did not complete. See $run/logs/latest.log"
        }
        Write-Output 'Eclipse commands, one-day natural phases, sun dog and moon suppression/restoration, saved-wait scheduling, lightmap, sun frames, night, dimensions and timed expiry passed.'
    }
} finally { Pop-Location }
