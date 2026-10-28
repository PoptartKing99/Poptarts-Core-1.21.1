param(
    [Parameter(Mandatory = $true)][string]$Source,
    [Parameter(Mandatory = $true)][string]$Destination
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

# The reference artwork uses the same four-island layout at an enlarged scale.
# Sampling each island separately keeps the game atlas pixel-perfect even when
# the reference drawing's margins differ from the 32x32 UV coordinates.
$regions = @(
    @{ Crop = @(60, 58, 334, 783); Destination = @(1, 1, 8, 16) },
    @{ Crop = @(450, 58, 730, 783); Destination = @(11, 1, 8, 16) },
    @{ Crop = @(60, 959, 690, 1138); Destination = @(1, 20, 16, 4) },
    @{ Crop = @(822, 959, 1152, 1138); Destination = @(20, 20, 8, 4) }
)

$inputImage = [System.Drawing.Bitmap]::new($Source)
try {
    if ($inputImage.Width -lt 1152 -or $inputImage.Height -lt 1138) {
        throw 'Source art is smaller than the expected enlarged UV layout'
    }
    $atlas = [System.Drawing.Bitmap]::new(32, 32, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    try {
        $base = $inputImage.GetPixel(180, 220)
        for ($y = 0; $y -lt 32; $y++) {
            for ($x = 0; $x -lt 32; $x++) { $atlas.SetPixel($x, $y, $base) }
        }
        foreach ($region in $regions) {
            $crop = $region.Crop
            $target = $region.Destination
            for ($v = 0; $v -lt $target[3]; $v++) {
                for ($u = 0; $u -lt $target[2]; $u++) {
                    $sampleX = [int][math]::Round($crop[0] + ($u + .5) * ($crop[2] - $crop[0]) / $target[2])
                    $sampleY = [int][math]::Round($crop[1] + ($v + .5) * ($crop[3] - $crop[1]) / $target[3])
                    $atlas.SetPixel($target[0] + $u, $target[1] + $v, $inputImage.GetPixel($sampleX, $sampleY))
                }
            }
            # Duplicate the edge pixels into the one-pixel gutter for mipmaps.
            for ($v = -1; $v -le $target[3]; $v++) {
                for ($u = -1; $u -le $target[2]; $u++) {
                    if ($u -ge 0 -and $u -lt $target[2] -and $v -ge 0 -and $v -lt $target[3]) { continue }
                    $clampedU = [math]::Max(0, [math]::Min($target[2] - 1, $u))
                    $clampedV = [math]::Max(0, [math]::Min($target[3] - 1, $v))
                    $x = $target[0] + $u
                    $y = $target[1] + $v
                    if ($x -ge 0 -and $x -lt 32 -and $y -ge 0 -and $y -lt 32) {
                        $atlas.SetPixel($x, $y, $atlas.GetPixel($target[0] + $clampedU, $target[1] + $clampedV))
                    }
                }
            }
        }
        $atlas.Save($Destination, [System.Drawing.Imaging.ImageFormat]::Png)
    }
    finally { $atlas.Dispose() }
}
finally { $inputImage.Dispose() }
