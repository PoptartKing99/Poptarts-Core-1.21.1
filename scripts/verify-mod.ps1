#requires -Version 7.0
param([string] $DevModsDirectory)

$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$arguments = @('-PgameTests', 'build', '--console=plain')
if ($DevModsDirectory) { $arguments += "-PdevModsDir=$([IO.Path]::GetFullPath($DevModsDirectory))" }
Push-Location -LiteralPath $repo
try {
    & (Join-Path $repo 'gradlew.bat') @arguments
    if ($LASTEXITCODE -ne 0) { throw "Full mod verification failed with exit code $LASTEXITCODE." }
    & (Join-Path $repo 'scripts/verify-feature-checks.ps1')
} finally { Pop-Location }
