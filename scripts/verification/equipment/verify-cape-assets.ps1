param([string]$JarPath)
$ErrorActionPreference = 'Stop'
& "$PSScriptRoot/../../assets/cape/build-cape-colors.ps1" -VerifyOnly -JarPath $JarPath
