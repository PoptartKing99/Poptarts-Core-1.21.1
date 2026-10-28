param([switch]$RunClient)
$ErrorActionPreference = 'Stop'
$project = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
Push-Location $project
try {
    & .\gradlew.bat build --offline --no-configuration-cache --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Build failed' }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $artifact = Get-ChildItem build/libs/*.jar | Where-Object Name -NotMatch 'sources|javadoc' | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    $archive = [IO.Compression.ZipFile]::OpenRead($artifact.FullName)
    try {
        if ($archive.Entries.FullName -match 'NavigationControlsSmoke') { throw 'Development client test was packaged' }
        if ($archive.Entries.FullName -notcontains 'dev/poptartking/poptartcore/mixin/navigation/NavigationCavernsCompassMixin.class') { throw 'Compass compatibility fix missing' }
    } finally { $archive.Dispose() }
    if ($RunClient) {
        $run = Join-Path $project 'build/navigation-test-run'
        $world = Join-Path $run 'saves/navigation-smoke'
        if (-not (Test-Path -LiteralPath 'build/game-test-run/world/level.dat')) { throw 'Run the server GameTests first to create the disposable world' }
        New-Item -ItemType Directory -Force "$run/mods", "$run/saves" | Out-Null
        if (-not (Test-Path -LiteralPath "$world/level.dat")) { Copy-Item -LiteralPath 'build/game-test-run/world' -Destination $world -Recurse }
        Copy-Item -Path 'run/mods/*.jar' -Destination "$run/mods"
        Set-Content -LiteralPath "$run/options.txt" -Value 'pauseOnLostFocus:false'
        try {
            & .\gradlew.bat runClient -PnavigationTests --offline --no-configuration-cache --console=plain *> "$run/gradle-client.log"
            if ($LASTEXITCODE -ne 0) { throw "Client failed. See $run/gradle-client.log" }
        } finally {
            & .\gradlew.bat prepareClientRun --offline --no-configuration-cache --console=plain
            if ($LASTEXITCODE -ne 0) { throw 'Could not restore normal client launch arguments' }
        }
        $log = Get-Content -LiteralPath "$run/logs/latest.log" -Raw
        if ($log -notmatch 'NAVIGATION_CONTROLS_PASS' -or $log -match 'NAVIGATION_CONTROLS_FAIL') { throw "Controls test failed. See $run/logs/latest.log" }
        Select-String -LiteralPath "$run/logs/latest.log" -Pattern 'NAVIGATION_CONTROL_MODE_PASS|NAVIGATION_CONTROLS_PASS'
    }
} finally { Pop-Location }
