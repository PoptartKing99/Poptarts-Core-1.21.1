param(
    [Parameter(Mandatory = $true)]
    [string]$SourcePackRoot
)

$ErrorActionPreference = 'Stop'
$sourceAssets = Join-Path $SourcePackRoot 'assets/nogs_menagerie'
$sourceModels = Join-Path $sourceAssets 'models/crops/utilities'
$sourceTextures = Join-Path $sourceAssets 'textures/crops/utilities'
$resourceRoot = Join-Path $PSScriptRoot '../src/main/resources/assets/poptartcore'
$targetModels = Join-Path $resourceRoot 'models/block/tree_tap'
$targetTextures = Join-Path $resourceRoot 'textures/block/tree_tap'

$woods = @('maple', 'spruce', 'birch', 'acacia', 'jungle')
$modelNames = @('nm_tree_tap', 'nm_tree_tap_bucket')
foreach ($wood in $woods) {
    $sourceWood = if ($wood -eq 'maple') { 'oak' } else { $wood }
    $modelNames += "nm_tree_tap_$sourceWood"
    $modelNames += "nm_tree_tap_${sourceWood}_full"
}
$textureNames = @('nm_tree_tap', 'nm_tree_tap_bucket')
foreach ($wood in $woods) {
    if ($wood -eq 'jungle') { continue } # Preserve the supplied custom latex textures.
    $sourceWood = if ($wood -eq 'maple') { 'oak' } else { $wood }
    $textureNames += "nm_tree_tap_${sourceWood}_pool"
    $textureNames += "nm_tree_tap_${sourceWood}_drip"
}

New-Item -ItemType Directory -Force -Path $targetModels, $targetTextures | Out-Null
foreach ($name in $modelNames) {
    $source = Join-Path $sourceModels "$name.json"
    if (!(Test-Path -LiteralPath $source)) { throw "Missing model: $source" }
    $model = Get-Content -LiteralPath $source -Raw | ConvertFrom-Json -AsHashtable
    $model.render_type = 'minecraft:cutout'
    foreach ($texture in @($model.textures.Keys)) {
        $model.textures[$texture] = $model.textures[$texture].Replace(
            'nogs_menagerie:crops/utilities/nm_tree_tap',
            'poptartcore:block/tree_tap/tree_tap').Replace(
            'tree_tap_oak', 'tree_tap_maple')
    }
    foreach ($element in $model.elements) {
        # The source model ends at z=13, leaving a visible gap to the log at z=16.
        $element.from[2] = [double]$element.from[2] + 3
        $element.to[2] = [double]$element.to[2] + 3
        if ($element.Contains('rotation')) {
            $element.rotation.origin[2] = [double]$element.rotation.origin[2] + 3
        }
        # The drip extends below the block into the bucket. Disable the extra
        # directional and ambient shading only on its faces, without making
        # the sap glow or changing the original texture colors.
        if (@($element.faces.Values | Where-Object { $_.texture -eq '#4' }).Count -gt 0) {
            $element.shade = $false
            $element.neoforge_data = [ordered]@{ ambient_occlusion = $false }
        }
    }
    $targetName = $name.Replace('nm_tree_tap', 'tree_tap').Replace('tree_tap_oak', 'tree_tap_maple')
    $json = $model | ConvertTo-Json -Depth 100 -Compress
    [System.IO.File]::WriteAllText((Join-Path $targetModels "$targetName.json"), $json + "`n")
}
foreach ($name in $textureNames) {
    $source = Join-Path $sourceTextures "$name.png"
    if (!(Test-Path -LiteralPath $source)) { throw "Missing texture: $source" }
    $targetName = $name.Replace('nm_tree_tap', 'tree_tap').Replace('tree_tap_oak', 'tree_tap_maple')
    Copy-Item -LiteralPath $source -Destination (Join-Path $targetTextures "$targetName.png") -Force
    $metadata = "$source.mcmeta"
    if (Test-Path -LiteralPath $metadata) {
        $targetMetadata = Join-Path $targetTextures "$targetName.png.mcmeta"
        if ($name -like '*_drip') {
            # Interpolation mixes visible sap with black RGB stored in fully
            # transparent pixels, making the stream darken between frames.
            $animation = Get-Content -LiteralPath $metadata -Raw | ConvertFrom-Json -AsHashtable
            $animation.animation.interpolate = $false
            # Keep the original drip frames at two ticks each, but pause on
            # frame zero (no connecting stream) between short drip bursts.
            $animation.animation.frames = @(@{ index = 0; time = 380 }) + @(1..9)
            [System.IO.File]::WriteAllText(
                $targetMetadata,
                (($animation | ConvertTo-Json -Depth 20 -Compress) + "`n"))
        } else {
            Copy-Item -LiteralPath $metadata -Destination $targetMetadata -Force
        }
    }
}
foreach ($name in 'tree_tap_jungle_pool.png', 'tree_tap_jungle_drip.png', 'tree_tap_jungle_drip.png.mcmeta') {
    if (!(Test-Path -LiteralPath (Join-Path $targetTextures $name))) {
        throw "Missing supplied jungle latex texture: $name"
    }
}
& (Join-Path $PSScriptRoot 'clear_tree_tap_pause_frame.ps1')

# Each wood uses identical geometry. Keep one parent for each fill shape and
# let small child models select only the wood-specific pool and drip textures.
$mapleBody = Get-Content -LiteralPath (Join-Path $targetModels 'tree_tap_maple.json') -Raw | ConvertFrom-Json -AsHashtable
$mapleFull = Get-Content -LiteralPath (Join-Path $targetModels 'tree_tap_maple_full.json') -Raw | ConvertFrom-Json -AsHashtable
foreach ($wood in $woods | Where-Object { $_ -ne 'maple' }) {
    foreach ($kind in @('', '_full')) {
        $reference = if ($kind -eq '') { $mapleBody } else { $mapleFull }
        $candidate = Get-Content -LiteralPath (Join-Path $targetModels "tree_tap_${wood}${kind}.json") -Raw | ConvertFrom-Json -AsHashtable
        $referenceShape = ($reference | ConvertTo-Json -Depth 100 -Compress) | ConvertFrom-Json -AsHashtable
        $candidate.Remove('textures')
        $referenceShape.Remove('textures')
        if (($candidate | ConvertTo-Json -Depth 100 -Compress) -ne
            ($referenceShape | ConvertTo-Json -Depth 100 -Compress)) {
            throw "Wood-specific geometry differs for $wood$kind; cannot share a parent model."
        }
    }
}

# Level zero is an empty bucket. Reuse the original pool at four heights for
# partial levels, then the supplied full model as level five.
$poolHeights = @(-6.25, -5.25, -4.25, -3.25)
if ($mapleBody.elements.Count -ne 19 -or
    [double]$mapleBody.elements[15].from[1] -ne -4.25) {
    throw 'Unexpected filling model geometry in tree_tap_maple.json.'
}
for ($level = 0; $level -le $poolHeights.Count; $level++) {
    $model = ($mapleBody | ConvertTo-Json -Depth 100 -Compress) | ConvertFrom-Json -AsHashtable
    if ($level -eq 0) {
        $model.elements = @($model.elements[0..14] + $model.elements[16..18])
    } else {
        $model.elements[15].from[1] = $poolHeights[$level - 1]
        $model.elements[15].to[1] = $poolHeights[$level - 1]
    }
    $model.textures.Remove('3')
    $model.textures.Remove('4')
    $target = Join-Path $targetModels "tree_tap_level_${level}_dripping.json"
    [System.IO.File]::WriteAllText($target, (($model | ConvertTo-Json -Depth 100 -Compress) + "`n"))
}
$mapleFull.textures.Remove('3')
[System.IO.File]::WriteAllText(
    (Join-Path $targetModels 'tree_tap_full.json'),
    (($mapleFull | ConvertTo-Json -Depth 100 -Compress) + "`n"))

foreach ($wood in $woods) {
    $pool = if ($wood -eq 'jungle') { 'poptartcore:block/fluid/latex_still' }
            else { "poptartcore:block/tree_tap/tree_tap_${wood}_pool" }
    $drip = "poptartcore:block/tree_tap/tree_tap_${wood}_drip"
    for ($level = 0; $level -le $poolHeights.Count; $level++) {
        $child = [ordered]@{
            parent = "poptartcore:block/tree_tap/tree_tap_level_${level}_dripping"
            textures = [ordered]@{ '3' = $pool; '4' = $drip }
        }
        $target = Join-Path $targetModels "tree_tap_${wood}_level_${level}_dripping.json"
        [System.IO.File]::WriteAllText($target, (($child | ConvertTo-Json -Depth 8 -Compress) + "`n"))
    }
    $fullChild = [ordered]@{
        parent = 'poptartcore:block/tree_tap/tree_tap_full'
        textures = [ordered]@{ '3' = $pool }
    }
    [System.IO.File]::WriteAllText(
        (Join-Path $targetModels "tree_tap_${wood}_full.json"),
        (($fullChild | ConvertTo-Json -Depth 8 -Compress) + "`n"))
}

# These source-derived models are no longer referenced by blockstates or items.
foreach ($obsolete in @('tree_tap_bucket.json') + @($woods | ForEach-Object { "tree_tap_$_.json" })) {
    $path = Join-Path $targetModels $obsolete
    if (Test-Path -LiteralPath $path) { Remove-Item -LiteralPath $path }
}
foreach ($wood in $woods) {
    foreach ($level in 0..4) {
        $oldBodyModel = Join-Path $targetModels "tree_tap_${wood}_level_$level.json"
        if (Test-Path -LiteralPath $oldBodyModel) { Remove-Item -LiteralPath $oldBodyModel }
    }
    $oldDripModel = Join-Path $targetModels "tree_tap_${wood}_drip.json"
    if (Test-Path -LiteralPath $oldDripModel) { Remove-Item -LiteralPath $oldDripModel }
}

# Oak has been repurposed as No Man's Land maple.
# Clean only old generated files; never touch the supplied source pack.
foreach ($removedWood in @('oak')) {
    $obsoleteModels = @("tree_tap_${removedWood}.json", "tree_tap_${removedWood}_full.json",
                        "tree_tap_${removedWood}_drip.json")
    foreach ($level in 0..4) {
        $obsoleteModels += "tree_tap_${removedWood}_level_${level}.json"
        $obsoleteModels += "tree_tap_${removedWood}_level_${level}_dripping.json"
    }
    foreach ($name in $obsoleteModels) {
        $path = Join-Path $targetModels $name
        if (Test-Path -LiteralPath $path) { Remove-Item -LiteralPath $path }
    }
    foreach ($name in @("tree_tap_${removedWood}_pool.png", "tree_tap_${removedWood}_pool.png.mcmeta",
                         "tree_tap_${removedWood}_drip.png", "tree_tap_${removedWood}_drip.png.mcmeta")) {
        $path = Join-Path $targetTextures $name
        if (Test-Path -LiteralPath $path) { Remove-Item -LiteralPath $path }
    }
}

& (Join-Path $PSScriptRoot 'generate_tree_tap_blockstates.ps1')
Write-Host "Generated 6 shared fill shapes, $($woods.Count * 6) texture selectors, and $($woods.Count * 192) blockstate variants."
