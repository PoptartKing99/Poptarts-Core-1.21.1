#requires -Version 7.0
$ErrorActionPreference = 'Stop'
$scriptsRoot = Split-Path -Parent $PSScriptRoot
$repo = Split-Path -Parent $scriptsRoot
$entryPoints = @('setup-dev-mods.ps1', 'verify-mod.ps1', 'verify-feature-checks.ps1', 'README.md')
$categories = @('verification', 'assets', 'migrations', 'lib')

foreach ($entry in Get-ChildItem -LiteralPath $scriptsRoot -File) {
    if ($entry.Name -notin $entryPoints) { throw "Put $($entry.Name) in its script category." }
}
foreach ($directory in Get-ChildItem -LiteralPath $scriptsRoot -Directory) {
    # Empty local folders are not tracked by Git and do not own any tools.
    if ($directory.Name -notin $categories -and
            @(Get-ChildItem -LiteralPath $directory.FullName -Recurse -Force -File).Count -gt 0) {
        throw "Unknown script category: $($directory.Name)"
    }
}

$files = @(Get-ChildItem -LiteralPath $scriptsRoot -Recurse -File)
foreach ($file in $files) {
    if ($file.Extension -in @('.ps1', '.py', '.gradle') -and $file.BaseName -cnotmatch '^[a-z0-9]+(?:-[a-z0-9]+)*$') {
        throw "Use lowercase names separated by hyphens: $($file.FullName)"
    }
    if ($file.Extension -eq '.ps1') {
        $tokens = $null
        $parseErrors = $null
        $null = [Management.Automation.Language.Parser]::ParseFile($file.FullName, [ref]$tokens, [ref]$parseErrors)
        if ($parseErrors.Count) { throw "PowerShell syntax error in $($file.Name): $($parseErrors.Message -join '; ')" }
    }
}

$seen = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
$checks = Get-Content -LiteralPath (Join-Path $PSScriptRoot 'feature-checks.json') -Raw | ConvertFrom-Json
foreach ($check in $checks) {
    if (-not $seen.Add($check)) { throw "Duplicate feature check: $check" }
    if ($check -notmatch '^scripts/verification/(?:[a-z0-9-]+/)*verify-[a-z0-9-]+\.ps1$' -or
            -not (Test-Path -LiteralPath (Join-Path $repo $check) -PathType Leaf)) {
        throw "Feature check must point to an existing verification script: $check"
    }
}

# Validate concrete repository script references in code, docs, and script inputs.
$referenceFiles = @($files)
foreach ($folder in @('docs', 'gradle', 'src')) {
    $referenceFiles += Get-ChildItem -LiteralPath (Join-Path $repo $folder) -Recurse -File
}
foreach ($file in $referenceFiles | Where-Object Extension -In @('.ps1', '.py', '.gradle', '.java', '.md', '.json')) {
    $source = Get-Content -LiteralPath $file.FullName -Raw
    foreach ($match in [regex]::Matches($source, '(?<![\w/])scripts[/\\][a-zA-Z0-9_./\\+-]+')) {
        $relative = $match.Value.TrimEnd('.').Replace('\', '/')
        if (-not (Test-Path -LiteralPath (Join-Path $repo $relative))) {
            throw "Broken script reference '$relative' in $($file.FullName)"
        }
    }
}
Write-Output "Script categories, names, PowerShell syntax, $($checks.Count) feature checks, and repository references verified."
