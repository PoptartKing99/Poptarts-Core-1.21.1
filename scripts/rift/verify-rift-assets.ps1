#requires -Version 7
$ErrorActionPreference = 'Stop'

$project = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$assets = Join-Path $project 'src/main/resources/assets/poptartcore'
$ids = @(
    'rift_torch', 'sconce_rift_torch', 'rift_lantern', 'rift_campfire',
    'rift_brazier', 'rift_stone_brazier', 'sconce_torch_lever', 'sconce_rift_torch_lever',
    'cupric_brazier', 'cupric_stone_brazier', 'sconce_cupric_torch',
    'sconce_cupric_torch_lever', 'sconce_soul_torch_lever'
)

$errors = [System.Collections.Generic.List[string]]::new()
foreach ($id in $ids) {
    foreach ($relative in @("blockstates/$id.json", "models/item/$id.json")) {
        if (-not (Test-Path (Join-Path $assets $relative))) {
            $errors.Add("Missing $relative")
        }
    }
}

$files = Get-ChildItem (Join-Path $assets 'models') -Recurse -Filter '*.json' -File |
    Where-Object { $_.BaseName -match 'rift|cupric|sconce_soul|sconce_torch_lever' }
$files += Get-ChildItem (Join-Path $assets 'blockstates') -Filter '*.json' -File |
    Where-Object { $_.BaseName -match 'rift|cupric|sconce_soul|sconce_torch_lever' }

foreach ($file in $files) {
    $content = [System.IO.File]::ReadAllText($file.FullName)
    try {
        $null = $content | ConvertFrom-Json -AsHashtable
    } catch {
        throw "JSON check failed for $($file.FullName): $_"
    }
    foreach ($match in [regex]::Matches($content, 'poptartcore:(block|item)/([a-z0-9_/-]+)')) {
        $kind = $match.Groups[1].Value
        $name = $match.Groups[2].Value
        $model = Join-Path $assets "models/$kind/$name.json"
        $texture = Join-Path $assets "textures/$kind/$name.png"
        if (-not (Test-Path $model) -and -not (Test-Path $texture)) {
            $errors.Add("$($file.Name) refers to missing $($match.Value)")
        }
    }
}

if ($errors.Count -gt 0) {
    $errors | ForEach-Object { Write-Error $_ }
    exit 1
}
Write-Output "Verified $($ids.Count) item/blockstate pairs and $($files.Count) Rift/Cupric models."
