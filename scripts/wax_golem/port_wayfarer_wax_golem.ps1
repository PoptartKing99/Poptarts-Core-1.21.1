param(
    [string]$WayfarerSources = "C:\Users\badas\IdeaProjects\jadx_wayfarer_core\sources",
    [string]$WayfarerAssets = "C:\Users\badas\IdeaProjects\intellij_wayfarer\decompiled",
    [string]$ProjectRoot = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [switch]$Overwrite
)

$ErrorActionPreference = "Stop"

$sourcePackage = Join-Path $WayfarerSources "dev\tazer\wayfarer\waxgolem"
$destinationPackage = Join-Path $ProjectRoot "src\main\java\dev\poptartking\poptartcore\waxgolem"
$sourceClient = Join-Path $WayfarerSources "dev\tazer\wayfarer\client\waxgolem"
$destinationClient = Join-Path $destinationPackage "client"
$sourceTextures = Join-Path $WayfarerAssets "assets\wayfarer_core\textures\entity\wax_golem"
$destinationTextures = Join-Path $ProjectRoot "src\main\resources\assets\poptartcore\textures\entity\wax_golem"

$serverFiles = @(
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
    "WaxGolemState.java"
)
$clientFiles = @("WaxGolemHeldItemLayer.java", "WaxGolemModel.java", "WaxGolemRenderer.java")
$textureFiles = 1..4 | ForEach-Object { "wax_golem_$_.png" }

foreach ($path in @($sourcePackage, $sourceClient, $sourceTextures)) {
    if (-not (Test-Path -LiteralPath $path -PathType Container)) {
        throw "Missing Wayfarer source directory: $path"
    }
}

$destinationFiles = @(
    $serverFiles | ForEach-Object { Join-Path $destinationPackage $_ }
) + @(
    $clientFiles | ForEach-Object { Join-Path $destinationClient $_ }
) + @(
    $textureFiles | ForEach-Object { Join-Path $destinationTextures $_ }
)
$existingFiles = @($destinationFiles | Where-Object { Test-Path -LiteralPath $_ -PathType Leaf })
if ($existingFiles.Count -gt 0 -and -not $Overwrite) {
    throw "Refusing to overwrite $($existingFiles.Count) existing Wax Golem files. Pass -Overwrite only when intentionally rebuilding the port."
}

New-Item -ItemType Directory -Force -Path $destinationPackage, $destinationClient, $destinationTextures | Out-Null

foreach ($name in $serverFiles) {
    $source = Join-Path $sourcePackage $name
    $destination = Join-Path $destinationPackage $name
    $content = Get-Content -Raw -LiteralPath $source
    $content = $content.Replace("package dev.tazer.wayfarer.waxgolem;", "package dev.poptartking.poptartcore.waxgolem;")
    $content = $content.Replace("import dev.tazer.wayfarer.registry.WayfarerItems;", "import dev.poptartking.poptartcore.registry.PoptartCoreItems;")
    $content = $content.Replace("WayfarerItems.WAX", "PoptartCoreItems.WAX")
    $content = $content.Replace("import dev.tazer.wayfarer.integration.ragdoll.CorpseSlotLayout;", "")
    $content = $content.Replace("CorpseSlotLayout.COLUMN_X", "120")
    $content = $content.Replace("import dev.tazer.wayfarer.fluidlogistics.FluidRouting;", "")
    $content = $content.Replace("FluidRouting.PUMP_RATE", "200")
    Set-Content -LiteralPath $destination -Value $content -NoNewline
}

foreach ($name in $clientFiles) {
    $source = Join-Path $sourceClient $name
    $destination = Join-Path $destinationClient $name
    $content = Get-Content -Raw -LiteralPath $source
    $content = $content.Replace("package dev.tazer.wayfarer.client.waxgolem;", "package dev.poptartking.poptartcore.waxgolem.client;")
    $content = $content.Replace("import dev.tazer.wayfarer.Wayfarer;", "import dev.poptartking.poptartcore.PoptartCore;")
    $content = $content.Replace("import dev.tazer.wayfarer.waxgolem.WaxGolem;", "import dev.poptartking.poptartcore.waxgolem.WaxGolem;")
    $content = $content.Replace("Wayfarer.location", "PoptartCore.location")
    Set-Content -LiteralPath $destination -Value $content -NoNewline
}

foreach ($name in $textureFiles) {
    Copy-Item -LiteralPath (Join-Path $sourceTextures $name) -Destination (Join-Path $destinationTextures $name) -Force
}

Write-Output "Ported $($serverFiles.Count) server classes, $($clientFiles.Count) client classes, and $($textureFiles.Count) textures."
