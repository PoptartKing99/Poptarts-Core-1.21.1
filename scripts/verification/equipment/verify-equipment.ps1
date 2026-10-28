param([switch]$RunServer)
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
Push-Location $project
try {
    & ./gradlew.bat spotlessJavaApply build compileGameTestJava -PgameTests -I scripts/verification/equipment/gametest.gradle --offline --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Equipment build or test compilation failed' }
    if ($RunServer) {
        $mods = Join-Path $project 'build/game-test-run/mods'
        New-Item -ItemType Directory -Force -Path $mods | Out-Null
        $patterns = @('abundant_atmosphere-*.jar', 'BlockBox-*.jar', 'blueprint-*.jar',
            'caverns_and_chasms-*.jar', 'create-1.21.1-*.jar', 'create-aeronautics-bundled-*.jar',
            'FarmersDelight-*.jar', 'lambdynamiclights-*.jar', 'moonlight-*.jar', 'newworld-*.jar',
            'nomansland-*.jar', 'ragdoll-*.jar', 'sable-*.jar', 'supplementaries-*.jar', 'reliable_backpacks-*.jar')
        foreach ($pattern in $patterns) {
            $matches = @(Get-ChildItem -LiteralPath (Join-Path $project 'run/mods') -Filter $pattern)
            if ($matches.Count -ne 1) { throw "Expected one runtime dependency matching $pattern" }
            Copy-Item -LiteralPath $matches[0].FullName -Destination $mods
        }
        & ./gradlew.bat runGameTestServer -PgameTests -I scripts/verification/equipment/gametest.gradle --offline --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'Server GameTests failed' }
        $log = Get-Content -LiteralPath (Join-Path $project 'build/game-test-run/logs/latest.log') -Raw
        if ($log -notmatch 'All.*tests passed|All.*required tests passed') { throw 'Server did not report successful GameTests; inspect build/game-test-run/logs/latest.log' }
    }
} finally { Pop-Location }
