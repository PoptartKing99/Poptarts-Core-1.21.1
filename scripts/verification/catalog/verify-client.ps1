#requires -Version 7.0
param([switch] $RunClient)
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '../../..')).Path
$init = 'scripts/verification/catalog/client-check.gradle'
Push-Location -LiteralPath $repo
try {
    & ./gradlew.bat compileNavigationTestJava -PnavigationTests -I $init --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Catalog client test compilation failed.' }
    if ($RunClient) {
        $run = Join-Path $repo 'build/catalog-client-run'
        $world = Join-Path $run 'saves/navigation-smoke'
        $sourceWorld = Join-Path $repo 'build/game-test-run/world'
        if (-not (Test-Path -LiteralPath (Join-Path $sourceWorld 'level.dat'))) {
            throw 'Run scripts/verify-mod.ps1 first to prepare the isolated server test world.'
        }
        New-Item -ItemType Directory -Force -Path (Join-Path $run 'mods'), (Join-Path $run 'saves') | Out-Null
        $lock = Get-Content -Raw -LiteralPath (Join-Path $repo 'gradle/development/mods-lock.json') | ConvertFrom-Json
        foreach ($entry in $lock.mods) {
            $source = Join-Path $repo "build/game-test-run/mods/$($entry.file)"
            if (-not (Test-Path -LiteralPath $source) -or (Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash -ne $entry.sha256) {
                throw "Missing or mismatched isolated runtime mod: $($entry.file). Run full verification first."
            }
        }
        foreach ($existing in Get-ChildItem -LiteralPath (Join-Path $run 'mods') -File -Filter '*.jar') {
            if ($existing.Name -notin $lock.mods.file) { throw "Unexpected mod in the isolated client runtime: $($existing.Name)" }
        }
        foreach ($entry in $lock.mods) {
            Copy-Item -LiteralPath (Join-Path $repo "build/game-test-run/mods/$($entry.file)") -Destination (Join-Path $run 'mods')
        }
        if (-not (Test-Path -LiteralPath (Join-Path $world 'level.dat'))) {
            Copy-Item -LiteralPath $sourceWorld -Destination $world -Recurse
        }
        Set-Content -LiteralPath (Join-Path $run 'options.txt') -Value 'pauseOnLostFocus:false'
        try {
            & ./gradlew.bat runClient -PnavigationTests -I $init --console=plain *> (Join-Path $run 'gradle-client.log')
            if ($LASTEXITCODE -ne 0) { throw "Catalog client failed. See $run/gradle-client.log" }
        } finally {
            & ./gradlew.bat prepareClientRun --console=plain
            if ($LASTEXITCODE -ne 0) { throw 'Could not restore normal client launch arguments.' }
        }
        $log = Get-Content -Raw -LiteralPath (Join-Path $run 'logs/latest.log')
        if ($log -notmatch 'CATALOG_CLIENT_PASS' -or $log -match 'CATALOG_CLIENT_FAIL') {
            throw "Catalog client assertions failed. See $run/logs/latest.log"
        }
        Write-Output 'Catalog client crossbow models, beekeeper geometry and first-person sleeves, and workbench screen registration passed.'
    }
} finally { Pop-Location }
