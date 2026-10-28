param(
    [string]$TextureDirectory = "C:/Users/badas/IdeaProjects/Poptarts-Core-1.21.1/run/resourcepacks/Poptart's Resource Pack/assets/overflowingbars/textures/gui",
    [Parameter(Mandatory)][string]$BackupDirectory,
    [switch]$VerifyOnly
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$names = @('full', 'full_blinking', 'half', 'half_blinking', 'hardcore_full', 'hardcore_full_blinking', 'hardcore_half', 'hardcore_half_blinking')
# Reference icons.png uses E9801C, 9F5813, 58300B, and FFE1B8.
# Intermediate shades keep the source shading; blinking shades remain lighter.
$palette = @{
    'FF2222' = 'E9801C'; 'D10929' = '9F5813'; 'A00011' = '78420F';
    '680000' = '58300B'; 'FF9EB7' = 'FFE1B8';
    'FF5959' = 'F1A454'; 'FF4060' = 'D59757'; 'D73748' = 'BF8951';
    '9F3737' = '9F784E'; 'FFD5EE' = 'FFF2E1'
}
function Get-RecoloredPixel([Drawing.Color]$pixel) {
    if ($pixel.A -eq 0 -or ($pixel.R -eq $pixel.G -and $pixel.G -eq $pixel.B)) { return $pixel }
    $key = '{0:X2}{1:X2}{2:X2}' -f $pixel.R, $pixel.G, $pixel.B
    if (!$palette.ContainsKey($key)) { throw "Unexpected source color $key" }
    $rgb = [Convert]::ToInt32($palette[$key], 16)
    return [Drawing.Color]::FromArgb([int]$pixel.A, ($rgb -shr 16) -band 255, ($rgb -shr 8) -band 255, $rgb -band 255)
}
if (!$VerifyOnly) { New-Item -ItemType Directory -Force -Path $BackupDirectory | Out-Null }
foreach ($name in $names) {
    $target = Join-Path $TextureDirectory "$name.png"
    $backup = Join-Path $BackupDirectory "$name.png"
    if (!(Test-Path -LiteralPath $backup)) {
        if ($VerifyOnly) { throw "Missing backup $backup" }
        Copy-Item -LiteralPath $target -Destination $backup
    }
    $source = [Drawing.Bitmap]::new($backup)
    try {
        if (!$VerifyOnly) {
            $output = [Drawing.Bitmap]::new($source.Width, $source.Height, [Drawing.Imaging.PixelFormat]::Format32bppArgb)
            try {
                for ($y = 0; $y -lt $source.Height; $y++) {
                    for ($x = 0; $x -lt $source.Width; $x++) {
                        $output.SetPixel($x, $y, (Get-RecoloredPixel $source.GetPixel($x, $y)))
                    }
                }
                $output.Save($target, [Drawing.Imaging.ImageFormat]::Png)
            } finally { $output.Dispose() }
        }
        $actual = [Drawing.Bitmap]::new($target)
        try {
            if ($actual.Width -ne $source.Width -or $actual.Height -ne $source.Height) { throw "Dimensions changed: $name" }
            $changed = 0
            for ($y = 0; $y -lt $source.Height; $y++) {
                for ($x = 0; $x -lt $source.Width; $x++) {
                    $original = $source.GetPixel($x, $y)
                    $pixel = $actual.GetPixel($x, $y)
                    $expected = Get-RecoloredPixel $original
                    if ($pixel.ToArgb() -ne $expected.ToArgb()) { throw "Pixel mismatch: $name at $x,$y" }
                    if ($pixel.A -ne $original.A) { throw "Alpha changed: $name" }
                    if ($pixel.ToArgb() -ne $original.ToArgb()) { $changed++ }
                }
            }
            Write-Output "$name verified: $($actual.Width)x$($actual.Height), $changed recolored pixels; other pixels and alpha unchanged."
        } finally { $actual.Dispose() }
    } finally { $source.Dispose() }
}
