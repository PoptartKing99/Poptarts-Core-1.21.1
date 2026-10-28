#requires -Version 7.0
param(
    [Parameter(Mandatory)][string] $SourceDirectory,
    [string] $DestinationDirectory,
    [switch] $CheckOnly
)

$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$source = (Resolve-Path -LiteralPath $SourceDirectory).Path
if (-not $DestinationDirectory) { $DestinationDirectory = Join-Path $repo 'run/mods' }
$destination = [IO.Path]::GetFullPath($DestinationDirectory)
$lock = Get-Content -LiteralPath (Join-Path $repo 'gradle/development/mods-lock.json') -Raw | ConvertFrom-Json
if ($lock.schemaVersion -ne 1) { throw 'Unsupported development mod lock format.' }

# Validate every source and existing destination before copying anything.
foreach ($mod in $lock.mods) {
    if ([IO.Path]::GetFileName($mod.file) -ne $mod.file -or $mod.file.Contains('\') -or $mod.file.Contains('/')) {
        throw "Invalid locked mod filename: $($mod.file)"
    }
    $path = Join-Path $source $mod.file
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Missing locked mod: $path" }
    if ((Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant() -ne $mod.sha256) {
        throw "Wrong binary for $($mod.file). Supply the exact locked version, including local patches."
    }
    $existing = Join-Path $destination $mod.file
    if ((Test-Path -LiteralPath $existing) -and
        (Get-FileHash -LiteralPath $existing -Algorithm SHA256).Hash.ToLowerInvariant() -ne $mod.sha256) {
        throw "Destination has a different binary: $existing. Keep that file separately before installing the locked version."
    }
}
if (-not $CheckOnly) {
    New-Item -ItemType Directory -Path $destination -Force | Out-Null
    foreach ($mod in $lock.mods) {
        $target = Join-Path $destination $mod.file
        if (-not (Test-Path -LiteralPath $target)) {
            Copy-Item -LiteralPath (Join-Path $source $mod.file) -Destination $target
        }
    }
}
Write-Host "Verified $($lock.mods.Count) locked development mods. Destination: $destination"
