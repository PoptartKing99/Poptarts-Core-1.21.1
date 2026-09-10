$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$javaRoot = Join-Path $projectRoot "src\main\java"

foreach ($javaFile in Get-ChildItem -Recurse -File -LiteralPath $javaRoot -Filter *.java) {
    $source = Get-Content -Raw -LiteralPath $javaFile.FullName
    if ($source -notmatch "(?m)^package\s+([^;]+);") {
        throw "Java file has no package declaration: $($javaFile.FullName)"
    }

    $relativeDirectory = [IO.Path]::GetRelativePath($javaRoot, $javaFile.DirectoryName)
    $expectedPackage = $relativeDirectory.Replace([IO.Path]::DirectorySeparatorChar, ".")
    if ($Matches[1] -ne $expectedPackage) {
        throw "Package does not match folder for $($javaFile.Name): expected $expectedPackage, found $($Matches[1])"
    }
}

$requiredDirectories = @(
    "src\main\java\dev\poptartking\poptartcore\armor\client",
    "src\main\java\dev\poptartking\poptartcore\blastfurnace\client",
    "src\main\java\dev\poptartking\poptartcore\crucible\client",
    "src\main\java\dev\poptartking\poptartcore\hammer\client",
    "src\main\java\dev\poptartking\poptartcore\quern\client",
    "src\main\java\dev\poptartking\poptartcore\quern\recipe",
    "src\main\java\dev\poptartking\poptartcore\workbench\client",
    "src\main\resources\assets\poptartcore\models\block\quern",
    "src\main\resources\assets\poptartcore\models\block\workbench",
    "src\main\resources\assets\poptartcore\textures\block\workbench",
    "src\main\resources\assets\poptartcore\textures\gui\sprites\blast_furnace",
    "src\main\resources\assets\poptartcore\textures\gui\sprites\crucible",
    "scripts\bloomery",
    "scripts\hammer",
    "scripts\quern",
    "scripts\workbench"
)
foreach ($relativePath in $requiredDirectories) {
    if (-not (Test-Path -LiteralPath (Join-Path $projectRoot $relativePath) -PathType Container)) {
        throw "Missing organized project directory: $relativePath"
    }
}

$oldPaths = @(
    "src\main\java\dev\poptartking\poptartcore\item\HammerItem.java",
    "src\main\java\dev\poptartking\poptartcore\client\QuernHighlightRenderer.java",
    "src\main\resources\assets\poptartcore\models\block\quern_stone.json",
    "src\main\resources\assets\poptartcore\models\block\workbench.json",
    "src\main\resources\assets\poptartcore\sounds\quern.ogg"
)
foreach ($relativePath in $oldPaths) {
    if (Test-Path -LiteralPath (Join-Path $projectRoot $relativePath)) {
        throw "Old unorganized path still exists: $relativePath"
    }
}

Write-Host "Project package and feature-folder layout verified."
