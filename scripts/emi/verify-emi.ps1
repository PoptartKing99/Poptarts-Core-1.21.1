param([switch]$RunClient)
$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
Push-Location $project
try {
    & .\gradlew.bat build --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Build failed' }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $properties = Get-Content -LiteralPath 'gradle.properties' -Raw | ConvertFrom-StringData
    $jar = [IO.Compression.ZipFile]::OpenRead((Join-Path $project "build/libs/$($properties.mod_id)-$($properties.mod_version).jar"))
    try {
        $entries = @($jar.Entries.FullName)
        if (-not ($entries -contains 'dev/poptartking/poptartcore/integration/emi/PoptartCoreEmiPlugin.class')) {
            throw 'EMI plugin missing from JAR'
        }
        if ($entries -match '^dev/emi/' -or $entries -match 'EmiSmokeTest|MillstoneGameTests') {
            throw 'Dependency or development tests bundled in mod JAR'
        }
        foreach ($asset in @('emi/grinding.png', 'emi/grinding_progress.png', 'crucible/crucible.png', 'blast_furnace/blast_furnace.png')) {
            if (-not ($entries -contains "assets/poptartcore/textures/gui/$asset")) { throw "Missing EMI background: $asset" }
        }
        $reader = [IO.StreamReader]::new($jar.GetEntry('assets/poptartcore/lang/en_us.json').Open())
        try { $language = $reader.ReadToEnd() | ConvertFrom-Json -AsHashtable } finally { $reader.Dispose() }
        foreach ($category in @('crucible_melting', 'blast_furnace_melting', 'crucible_alloying',
                'blast_furnace_alloying', 'casting', 'grinding', 'milling')) {
            if (-not $language.ContainsKey("emi.category.poptartcore.$category")) { throw "Missing category name: $category" }
        }
    } finally { $jar.Dispose() }
    Write-Host 'EMI plugin, category names and clean JAR packaging verified.'

    if ($RunClient) {
        $testRun = Join-Path $project 'build/emi-test-run'
        $testWorld = Join-Path $testRun 'saves/emi-smoke'
        if (-not (Test-Path -LiteralPath "$project/build/millstone-test-run/world/level.dat")) {
            throw 'Run scripts/millstone/verify-millstone.ps1 -RunServer first to generate the disposable test world.'
        }
        New-Item -ItemType Directory -Force -Path "$testRun/mods", "$testRun/saves" | Out-Null
        if (-not (Test-Path -LiteralPath $testWorld)) {
            Copy-Item -LiteralPath "$project/build/millstone-test-run/world" -Destination $testWorld -Recurse
        }
        foreach ($mod in Get-ChildItem -LiteralPath "$project/build/millstone-test-run/mods" -Filter '*.jar') {
            Copy-Item -LiteralPath $mod.FullName -Destination "$testRun/mods/$($mod.Name)"
        }
        $emiName = "emi-$($properties.emi_version)+neoforge.jar"
        Copy-Item -LiteralPath "$project/run/mods/$emiName" -Destination "$testRun/mods/$emiName"
        foreach ($pattern in @('lambdynamiclights-*.jar', 'sable-*.jar')) {
            $matches = @(Get-ChildItem -LiteralPath "$project/run/mods" -Filter $pattern)
            if ($matches.Count -ne 1) { throw "Expected one client dependency matching $pattern" }
            Copy-Item -LiteralPath $matches[0].FullName -Destination "$testRun/mods/$($matches[0].Name)"
        }
        try {
            & .\gradlew.bat runClient -PemiTests --console=plain *> "$testRun/gradle-client.log"
            if ($LASTEXITCODE -ne 0) { throw "Test client failed. See $testRun/gradle-client.log" }
        } finally {
            # Restore the argument files used by the normal IntelliJ launch configuration.
            & .\gradlew.bat prepareClientRun --console=plain
            if ($LASTEXITCODE -ne 0) { throw 'Could not restore normal client launch arguments' }
        }
        $log = Get-Content -LiteralPath "$testRun/logs/latest.log" -Raw
        if ($log -notmatch 'EMI_SMOKE_PASS' -or $log -match 'EMI_SMOKE_FAIL') {
            throw "EMI runtime checks failed. See $testRun/logs/latest.log"
        }
        Select-String -LiteralPath "$testRun/logs/latest.log" -Pattern 'EMI_SMOKE_PASS'
    }
} finally { Pop-Location }
