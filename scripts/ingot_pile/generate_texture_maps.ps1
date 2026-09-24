param([switch]$Force)

# Starter-map reset only. -Force overwrites the finished ingot-pile artwork.

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression

$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..')).Path
$sourceRoot = Join-Path $projectRoot 'src\main\resources\assets\poptartcore\textures\item'
$targetRoot = Join-Path $projectRoot 'src\main\resources\assets\poptartcore\textures\block\ingot_pile'
$minecraftJar = 'C:\Users\badas\.gradle\caches\neoformruntime\artifacts\minecraft_1.21.1_client.jar'
$metals = @('iron', 'gold', 'copper', 'tin', 'lead', 'silver', 'bronze', 'steel', 'titanium')

if (-not (Test-Path -LiteralPath $minecraftJar)) {
    throw "Minecraft 1.21.1 client JAR not found: $minecraftJar"
}

function Get-SourceBitmap([string]$metal, $zip) {
    if ($metal -in @('iron', 'gold', 'copper')) {
        $entry = $zip.GetEntry("assets/minecraft/textures/item/$($metal)_ingot.png")
        if ($null -eq $entry) { throw "Missing vanilla $metal ingot texture" }
        $stream = $entry.Open()
        try {
            $image = [System.Drawing.Bitmap]::FromStream($stream)
            try { return [System.Drawing.Bitmap]::new($image) }
            finally { $image.Dispose() }
        }
        finally { $stream.Dispose() }
    }
    return [System.Drawing.Bitmap]::new((Join-Path $sourceRoot "$($metal)_ingot.png"))
}

function Get-Palette($bitmap) {
    $pixels = @(
        for ($y = 0; $y -lt $bitmap.Height; $y++) {
            for ($x = 0; $x -lt $bitmap.Width; $x++) {
                $color = $bitmap.GetPixel($x, $y)
                if ($color.A -ge 128) {
                    [pscustomobject]@{
                        X = $x; Y = $y; Color = $color
                        Lightness = (0.2126 * $color.R + 0.7152 * $color.G + 0.0722 * $color.B)
                    }
                }
            }
        }
    ) | Sort-Object Lightness
    if ($pixels.Count -eq 0) { throw 'The source ingot texture has no opaque pixels' }
    return $pixels
}

function Get-Sample($bitmap, $pixels, [int]$x, [int]$y) {
    $color = $bitmap.GetPixel($x, $y)
    if ($color.A -ge 128) { return $color }
    $nearest = $pixels | Sort-Object { [math]::Abs($_.X - $x) + [math]::Abs($_.Y - $y) } | Select-Object -First 1
    return $nearest.Color
}

$zip = [System.IO.Compression.ZipFile]::OpenRead($minecraftJar)
try {
    foreach ($metal in $metals) {
        $destination = Join-Path $targetRoot "$metal.png"
        if ((Test-Path -LiteralPath $destination) -and -not $Force) {
            throw "Texture exists: $destination. Use -Force to regenerate all starter maps."
        }
        $source = Get-SourceBitmap $metal $zip
        try {
            $pixels = Get-Palette $source
            $dark = $pixels[[math]::Floor(($pixels.Count - 1) * .18)].Color
            $base = $pixels[[math]::Floor(($pixels.Count - 1) * .52)].Color
            $light = $pixels[[math]::Floor(($pixels.Count - 1) * .84)].Color
            $minX = ($pixels | Measure-Object X -Minimum).Minimum
            $maxX = ($pixels | Measure-Object X -Maximum).Maximum
            $minY = ($pixels | Measure-Object Y -Minimum).Minimum
            $maxY = ($pixels | Measure-Object Y -Maximum).Maximum

            $map = [System.Drawing.Bitmap]::new(32, 32, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
            try {
                for ($y = 0; $y -lt 32; $y++) {
                    for ($x = 0; $x -lt 32; $x++) { $map.SetPixel($x, $y, $base) }
                }

                # Top: 8 x 16 pixels. Bottom: the same shape with darker shading.
                for ($y = 0; $y -lt 16; $y++) {
                    for ($x = 0; $x -lt 8; $x++) {
                        $sx = [int][math]::Min($maxX, $minX + [math]::Floor(($x + .5) * ($maxX - $minX + 1) / 8))
                        $sy = [int][math]::Min($maxY, $minY + [math]::Floor(($y + .5) * ($maxY - $minY + 1) / 16))
                        $color = Get-Sample $source $pixels $sx $sy
                        if ($y -eq 0) { $color = $light }
                        if ($x -eq 0 -or $x -eq 7 -or $y -eq 15) { $color = $dark }
                        $map.SetPixel(1 + $x, 1 + $y, $color)
                        $map.SetPixel(11 + $x, 1 + $y, $dark)
                    }
                }

                # Long side: 16 x 4 pixels. End: 8 x 4 pixels.
                for ($y = 0; $y -lt 4; $y++) {
                    for ($x = 0; $x -lt 16; $x++) {
                        $sx = [int][math]::Min($maxX, $minX + [math]::Floor(($x + .5) * ($maxX - $minX + 1) / 16))
                        $sy = [int][math]::Min($maxY, $minY + [math]::Floor(($y + .5) * ($maxY - $minY + 1) / 4))
                        $sample = Get-Sample $source $pixels $sx $sy
                        $color = if ($y -eq 0) { $light } elseif ($y -eq 3) { $dark } else { $sample }
                        $map.SetPixel(1 + $x, 20 + $y, $color)
                    }
                    for ($x = 0; $x -lt 8; $x++) {
                        $sx = [int][math]::Min($maxX, $minX + [math]::Floor(($x + .5) * ($maxX - $minX + 1) / 8))
                        $sy = [int][math]::Min($maxY, $minY + [math]::Floor(($y + .5) * ($maxY - $minY + 1) / 4))
                        $sample = Get-Sample $source $pixels $sx $sy
                        $color = if ($y -eq 0) { $light } elseif ($y -eq 3) { $dark } else { $sample }
                        $map.SetPixel(20 + $x, 20 + $y, $color)
                    }
                }
                $map.Save($destination, [System.Drawing.Imaging.ImageFormat]::Png)
            }
            finally { $map.Dispose() }
            Write-Output "Generated $destination"
        }
        finally { $source.Dispose() }
    }
}
finally { $zip.Dispose() }
