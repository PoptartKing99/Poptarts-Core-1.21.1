$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$textureDir = Join-Path $PSScriptRoot '../src/main/resources/assets/poptartcore/textures/block/tree_tap'
foreach ($wood in @('maple', 'spruce', 'birch', 'acacia', 'jungle')) {
    $path = Join-Path $textureDir "tree_tap_${wood}_drip.png"
    $image = [System.Drawing.Bitmap]::new($path)
    try {
        if ($image.Width -ne 16 -or $image.Height -ne 160 -or
            $image.PixelFormat -ne [System.Drawing.Imaging.PixelFormat]::Format32bppArgb) {
            throw "Unexpected drip sprite format: $path"
        }
        $hasVisiblePixels = $false
        for ($y = 0; $y -lt 16; $y++) {
            for ($x = 0; $x -lt 16; $x++) {
                if ($image.GetPixel($x, $y).A -ne 0) { $hasVisiblePixels = $true }
            }
        }
        # The original continuous loop overlaps the previous splash with the
        # start of the next drip. A paused loop must clear that old splash.
        foreach ($frame in 1..3) {
            $firstSplashRow = switch ($frame) { 1 { 12 } 2 { 13 } 3 { 14 } }
            for ($y = $firstSplashRow; $y -lt 16; $y++) {
                for ($x = 0; $x -lt 16; $x++) {
                    if ($image.GetPixel($x, $frame * 16 + $y).A -ne 0) { $hasVisiblePixels = $true }
                }
            }
        }
        if (!$hasVisiblePixels) { continue }
        for ($y = 0; $y -lt 16; $y++) {
            for ($x = 0; $x -lt 16; $x++) {
                $image.SetPixel($x, $y, [System.Drawing.Color]::Transparent)
            }
        }
        foreach ($frame in 1..3) {
            $firstSplashRow = switch ($frame) { 1 { 12 } 2 { 13 } 3 { 14 } }
            for ($y = $firstSplashRow; $y -lt 16; $y++) {
                for ($x = 0; $x -lt 16; $x++) {
                    $image.SetPixel($x, $frame * 16 + $y, [System.Drawing.Color]::Transparent)
                }
            }
        }
        $temporary = Join-Path $textureDir "tree_tap_${wood}_drip.pause.tmp.png"
        try {
            $image.Save($temporary, [System.Drawing.Imaging.ImageFormat]::Png)
        } finally {
            $image.Dispose()
        }
        Move-Item -LiteralPath $temporary -Destination $path -Force
    } finally {
        $image.Dispose()
    }
}
