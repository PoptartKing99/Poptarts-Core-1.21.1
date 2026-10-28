# Repack the original pixels without resizing, interpolation, or palette changes.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$root = (Resolve-Path "$PSScriptRoot/../../..").Path
$source = [System.Drawing.Bitmap]::new("$PSScriptRoot/source.png")
$atlas = [System.Drawing.Bitmap]::new(32, 32)
function Copy-Pixels($sx, $sy, $width, $height, $tx, $ty) {
    for ($y = 0; $y -lt $height; $y++) {
        for ($x = 0; $x -lt $width; $x++) {
            $atlas.SetPixel($tx + $x, $ty + $y, $source.GetPixel($sx + $x, $sy + $y))
        }
    }
}
try {
    Copy-Pixels 0 0 10 14 0 0      # Exterior: 10x14
    Copy-Pixels 20 0 10 10 12 0    # Rim: 10x10, including transparent opening
    Copy-Pixels 1 16 8 12 0 16     # Inner walls: 8x12
    Copy-Pixels 11 11 8 8 12 16    # Interior floor: 8x8
    Copy-Pixels 10 10 10 10 22 16  # Underside: 10x10
    $atlas.Save("$root/src/main/resources/assets/poptartcore/textures/block/wooden_bucket.png", [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
    $atlas.Dispose()
    $source.Dispose()
}
