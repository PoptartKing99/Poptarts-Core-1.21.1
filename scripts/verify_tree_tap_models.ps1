$ErrorActionPreference = 'Stop'
$assets = Join-Path $PSScriptRoot '../src/main/resources/assets/poptartcore'
$models = Join-Path $assets 'models/block/tree_tap'
$woods = @('maple', 'spruce', 'pine', 'birch', 'acacia', 'jungle')
$parents = @('full') + @(0..4 | ForEach-Object { "level_${_}_dripping" })

foreach ($kind in $parents) {
    $parentName = "tree_tap_$kind"
    $parentPath = Join-Path $models "$parentName.json"
    $parent = Get-Content -LiteralPath $parentPath -Raw | ConvertFrom-Json -AsHashtable
    $expectedCount = if ($kind -eq 'full') { 16 } elseif ($kind -eq 'level_0_dripping') { 18 } else { 19 }
    if ($parent.elements.Count -ne $expectedCount) { throw "Wrong geometry: $parentPath" }

    foreach ($wood in $woods) {
        $childPath = Join-Path $models "tree_tap_${wood}_${kind}.json"
        $child = Get-Content -LiteralPath $childPath -Raw | ConvertFrom-Json -AsHashtable
        if ($child.parent -ne "poptartcore:block/tree_tap/$parentName" -or $child.Contains('elements')) {
            throw "Expected texture-only child of $parentName`: $childPath"
        }
        $expectedPool = "poptartcore:block/tree_tap/tree_tap_${wood}_pool"
        if ($child.textures['3'] -ne $expectedPool) {
            throw "Wrong pool texture: $childPath"
        }
        if ($kind -ne 'full' -and
            $child.textures['4'] -ne "poptartcore:block/tree_tap/tree_tap_${wood}_drip") {
            throw "Wrong drip texture: $childPath"
        }
        $textures = @{}
        foreach ($key in $parent.textures.Keys) { $textures[$key] = $parent.textures[$key] }
        foreach ($key in $child.textures.Keys) { $textures[$key] = $child.textures[$key] }
        foreach ($element in $parent.elements) {
            foreach ($face in $element.faces.Values) {
                $slot = $face.texture.TrimStart('#')
                if (!$textures.ContainsKey($slot)) { throw "Unresolved texture $slot in $childPath" }
            }
        }
    }
}

$blockstate = Get-Content -LiteralPath (Join-Path $assets 'blockstates/tree_tap.json') -Raw | ConvertFrom-Json -AsHashtable
if ($blockstate.variants.Count -ne 1152) { throw 'Expected 1152 tree-tap blockstate variants.' }
foreach ($variant in $blockstate.variants.Values) {
    $modelName = $variant.model.Replace('poptartcore:block/tree_tap/', '')
    if (!(Test-Path -LiteralPath (Join-Path $models "$modelName.json"))) {
        throw "Missing model for blockstate: $($variant.model)"
    }
}

$files = @(Get-ChildItem -LiteralPath $models -Filter '*.json')
if ($files.Count -ne 43) { throw "Expected 43 tree-tap model files, found $($files.Count)." }
$textureDir = Join-Path $assets 'textures/block/tree_tap'
Add-Type -AssemblyName System.Drawing
foreach ($wood in $woods) {
    $metadataPath = Join-Path $textureDir "tree_tap_${wood}_drip.png.mcmeta"
    $animation = (Get-Content -LiteralPath $metadataPath -Raw | ConvertFrom-Json -AsHashtable).animation
    $frames = @($animation.frames)
    if ($animation.frametime -ne 2 -or $animation.interpolate -ne $false -or
        $frames.Count -ne 10 -or $frames[0].index -ne 0 -or $frames[0].time -ne 380) {
        throw "Wrong drip timing: $metadataPath"
    }
    for ($index = 1; $index -le 9; $index++) {
        if ($frames[$index] -ne $index) { throw "Wrong drip frame order: $metadataPath" }
    }
    $spritePath = Join-Path $textureDir "tree_tap_${wood}_drip.png"
    $sprite = [System.Drawing.Bitmap]::new($spritePath)
    try {
        if ($sprite.Width -ne 16 -or $sprite.Height -ne 160) { throw "Wrong drip sprite size: $spritePath" }
        for ($y = 0; $y -lt 16; $y++) {
            for ($x = 0; $x -lt 16; $x++) {
                if ($sprite.GetPixel($x, $y).A -ne 0) { throw "Visible sap in resting frame: $spritePath" }
            }
        }
        foreach ($frame in 1..3) {
            $firstSplashRow = switch ($frame) { 1 { 12 } 2 { 13 } 3 { 14 } }
            for ($y = $firstSplashRow; $y -lt 16; $y++) {
                for ($x = 0; $x -lt 16; $x++) {
                    if ($sprite.GetPixel($x, $frame * 16 + $y).A -ne 0) {
                        throw "Splash appears before the drip reaches the bucket: $spritePath"
                    }
                }
            }
        }
    } finally {
        $sprite.Dispose()
    }
}
$obsoleteFiles = @('oak') | ForEach-Object {
    Get-ChildItem -LiteralPath $models, $textureDir -Filter "tree_tap_${_}*" -File
}
if (@($obsoleteFiles).Count -ne 0) { throw 'Obsolete oak or jungle tree-tap assets remain.' }
if (@($blockstate.variants.Keys | Where-Object { $_ -like '*wood=oak*' }).Count -ne 0) {
    throw 'Obsolete oak tree-tap blockstate variants remain.'
}
Write-Host 'Verified 6 shared shapes, 36 texture-only children, and 1152 resolvable blockstates.'
