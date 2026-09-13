param([switch]$RunServer)

$ErrorActionPreference = "Stop"
$projectRoot = (Resolve-Path "$PSScriptRoot\..\..").Path
$javaRoot = Join-Path $projectRoot "src\main\java\dev\poptartking\poptartcore"
$golemRoot = Join-Path $javaRoot "waxgolem"
$resourceRoot = Join-Path $projectRoot "src\main\resources"

$requiredClasses = @(
    "CollectDropsGoal.java",
    "ContainerAccess.java",
    "DepositResultGoal.java",
    "DepositSites.java",
    "FerryHoneyGoal.java",
    "HarvestHiveGoal.java",
    "HiveMemory.java",
    "HoneyCauldrons.java",
    "WaxGolem.java",
    "WaxGolemConstruction.java",
    "WaxGolemGoals.java",
    "WaxGolemState.java",
    "client\WaxGolemHeldItemLayer.java",
    "client\WaxGolemModel.java",
    "client\WaxGolemRenderer.java"
)

foreach ($relativePath in $requiredClasses) {
    $path = Join-Path $golemRoot $relativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Missing Wax Golem class: $relativePath"
    }
}

foreach ($stage in 1..4) {
    $texture = Join-Path $resourceRoot "assets\poptartcore\textures\entity\wax_golem\wax_golem_$stage.png"
    if (-not (Test-Path -LiteralPath $texture -PathType Leaf)) {
        throw "Missing Wax Golem stage texture: $texture"
    }
}

$java = Get-ChildItem -LiteralPath $golemRoot -Recurse -Filter "*.java" | Get-Content -Raw
if ($java -match "dev\.tazer\.wayfarer") {
    throw "Wax Golem code still contains a Wayfarer package dependency."
}

$build = Get-Content -Raw -LiteralPath (Join-Path $projectRoot "build.gradle")
if ($build -notmatch "nomansland-2\.0\.0\.jar") {
    throw "The compile-time No Man's Land 2.0.0 dependency is missing."
}

$metadata = Get-Content -Raw -LiteralPath (Join-Path $projectRoot "src\main\templates\META-INF\neoforge.mods.toml")
if ($metadata -notmatch 'modId="nomansland"' -or $metadata -notmatch 'versionRange="\[2\.0\.0,3\.0\.0\)"') {
    throw "The required No Man's Land 2.0.0 runtime dependency is missing."
}

Write-Output "Wax Golem structure and No Man's Land 2.0.0 dependency checks passed."

if ($RunServer) {
    $testMods = Join-Path $projectRoot "build\millstone-test-run\mods"
    New-Item -ItemType Directory -Force -Path $testMods | Out-Null
    $requiredPatterns = @(
        "create-1.21.1-6.0.10.jar",
        "create-aeronautics-bundled-1.21.1-1.3.1.jar",
        "FarmersDelight-1.21.1-1.3.4.jar",
        "moonlight-*.jar",
        "nomansland-2.0.0.jar",
        "sable-neoforge-1.21.1-*.jar",
        "supplementaries-*.jar"
    )
    foreach ($pattern in $requiredPatterns) {
        $matches = @(Get-ChildItem -LiteralPath (Join-Path $projectRoot "run\mods") -Filter $pattern)
        if ($matches.Count -ne 1) {
            throw "Expected one development dependency matching $pattern, found $($matches.Count)."
        }
        Copy-Item -LiteralPath $matches[0].FullName -Destination (Join-Path $testMods $matches[0].Name) -Force
    }

    Push-Location $projectRoot
    try {
        & .\gradlew.bat runGameTestServer -PmillstoneTests --console=plain
        if ($LASTEXITCODE -ne 0) {
            throw "Wax Golem GameTests failed."
        }
        $log = Join-Path $projectRoot "build\millstone-test-run\logs\latest.log"
        if (Select-String -LiteralPath $log -Pattern "Mod loading failures|WaxGolemGameTests.*failed|test failed" -Quiet) {
            throw "Wax Golem GameTest log contains a failure."
        }
    } finally {
        Pop-Location
    }
}
