$ErrorActionPreference = 'Stop'
$assetRoot = Join-Path $PSScriptRoot '../src/main/resources/assets/poptartcore/models/block'
$models = Join-Path $assetRoot 'tree_tap'
$overlayPath = Join-Path $assetRoot 'tree_tap_drip.json'
$source = Get-Content -LiteralPath (Join-Path $models 'tree_tap_level_0_dripping.json') -Raw | ConvertFrom-Json -AsHashtable
$drip = @($source.elements | Where-Object { @($_.faces.Values | Where-Object { $_.texture -eq '#4' }).Count -gt 0 })
if ($drip.Count -gt 0) {
    if ($drip.Count -ne 3) { throw 'Expected three original drip elements.' }
    $overlay = [ordered]@{
        textures = @{ '4' = 'poptartcore:block/tree_tap/tree_tap_maple_drip' }
        elements = $drip
        ambientocclusion = $false
    }
    [IO.File]::WriteAllText($overlayPath, ($overlay | ConvertTo-Json -Depth 100 -Compress) + "`n")
} elseif (!(Test-Path -LiteralPath $overlayPath)) {
    throw 'No original drip geometry available.'
}
foreach ($path in Get-ChildItem -LiteralPath $models -Filter '*.json') {
    $model = Get-Content -LiteralPath $path.FullName -Raw | ConvertFrom-Json -AsHashtable
    if ($model.ContainsKey('elements')) {
        $model.elements = @($model.elements | Where-Object { @($_.faces.Values | Where-Object { $_.texture -eq '#4' }).Count -eq 0 })
        $model.Remove('groups')
    }
    if ($model.ContainsKey('textures')) { $model.textures.Remove('4') }
    [IO.File]::WriteAllText($path.FullName, ($model | ConvertTo-Json -Depth 100 -Compress) + "`n")
}
Write-Host 'Separated the original drip geometry for event-driven per-tap rendering.'
