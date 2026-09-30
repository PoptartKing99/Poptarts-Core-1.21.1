$ErrorActionPreference = 'Stop'

$assets = Join-Path $PSScriptRoot '../src/main/resources/assets/poptartcore'
$blockModels = Join-Path $assets 'models/block'
$beehiveModels = Join-Path $blockModels 'beehive'
$names = @(
    'beehive_black_roof', 'beehive_blue_roof', 'beehive_brown_roof',
    'beehive_cyan_roof', 'beehive_gray_roof', 'beehive_green_roof',
    'beehive_light_blue_roof', 'beehive_light_gray_roof', 'beehive_lime_roof',
    'beehive_magenta_roof', 'beehive_orange_roof', 'beehive_pink_roof',
    'beehive_purple_roof', 'beehive_red_roof', 'beehive_white_roof',
    'beehive_yellow_roof', 'beehive_roof_template',
    'beehive_support', 'beehive_support_angled'
)

New-Item -ItemType Directory -Path $beehiveModels -Force | Out-Null
foreach ($name in $names) {
    $old = Join-Path $blockModels "$name.json"
    $new = Join-Path $beehiveModels "$name.json"
    if ((Test-Path -LiteralPath $old) -and (Test-Path -LiteralPath $new)) {
        throw "Both old and new beehive models exist: $name"
    }
    if (Test-Path -LiteralPath $old) { Move-Item -LiteralPath $old -Destination $new }
    if (!(Test-Path -LiteralPath $new)) { throw "Missing beehive model: $name" }
}

$updated = 0
foreach ($file in Get-ChildItem -LiteralPath $assets -Recurse -File -Filter '*.json') {
    $original = [System.IO.File]::ReadAllText($file.FullName)
    $changed = $original
    foreach ($name in $names) {
        # Only model references move. Texture paths remain wherever their PNGs live.
        $pattern = '("(?:parent|model)"\s*:\s*")poptartcore:block/' + [regex]::Escape($name) + '"'
        $replacement = '${1}poptartcore:block/beehive/' + $name + '"'
        $changed = [regex]::Replace($changed, $pattern, $replacement)
    }
    if ($changed -ne $original) {
        [System.IO.File]::WriteAllText($file.FullName, $changed)
        $updated++
    }
}
foreach ($file in Get-ChildItem -LiteralPath $beehiveModels -File -Filter '*.json') {
    $model = Get-Content -LiteralPath $file.FullName -Raw | ConvertFrom-Json -AsHashtable
    foreach ($texture in $model.textures.Values) {
        if (!$texture.StartsWith('poptartcore:block/')) { continue }
        $relative = $texture.Substring('poptartcore:block/'.Length)
        $png = Join-Path $assets "textures/block/$relative.png"
        if (!(Test-Path -LiteralPath $png)) { throw "Missing texture $texture in $($file.Name)" }
    }
}
Write-Host "Organized $($names.Count) beehive block models and updated $updated JSON files."
