param([switch]$RunServer)
$ErrorActionPreference = 'Stop'
$utilityRepo = (Resolve-Path "$PSScriptRoot/../..").Path
Push-Location $utilityRepo
try {
    & ./gradlew.bat spotlessJavaApply --offline --console=plain
    if ($LASTEXITCODE) { throw 'Formatting failed' }
    & ./gradlew.bat build compileGameTestJava -PgameTests -I scripts/equipment/gametest.gradle --no-configuration-cache --offline --console=plain
    if ($LASTEXITCODE) { throw 'Build or test compilation failed' }
    $mixins = Get-Content src/main/resources/poptartcore.mixins.json -Raw | ConvertFrom-Json
    if ('equipment.UtilityWieldHandMixin' -notin $mixins.mixins -or 'equipment.UtilityReleaseMixin' -notin $mixins.client) { throw 'Missing wield mixin registration' }
    if ($mixins.client -match 'UtilitySpyglass') { throw 'Obsolete custom zoom mixins remain' }
    $utility = Get-Content src/main/java/dev/poptartking/poptartcore/equipment/UtilityActions.java -Raw
    if ($utility -match 'CHANNELS|finishUsingItem|SPYGLASS|CONSUMABLE') { throw 'Obsolete item-specific dispatch remains' }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $utilityJar = [IO.Compression.ZipFile]::OpenRead((Resolve-Path build/libs/poptartcore-0.0.1.jar))
    try {
        foreach ($entry in @('dev/poptartking/poptartcore/equipment/UtilityWield.class', 'dev/poptartking/poptartcore/mixin/equipment/UtilityWieldHandMixin.class', 'dev/poptartking/poptartcore/mixin/equipment/UtilityReleaseMixin.class')) {
            if (!$utilityJar.GetEntry($entry)) { throw "Missing packaged class: $entry" }
        }
        if ($utilityJar.Entries.FullName -match 'UtilitySpyglass') { throw 'Obsolete spyglass classes packaged' }
    } finally { $utilityJar.Dispose() }
    if ($RunServer) {
        & ./gradlew.bat runGameTestServer -PgameTests -I scripts/equipment/gametest.gradle --no-configuration-cache --offline --console=plain
        if ($LASTEXITCODE) { throw 'Utility/equipment GameTests failed' }
    }
    'Utility wielding build, packaged mixins and removal of legacy dispatch verified.'
} finally { Pop-Location }
