param(
    [Parameter(Mandatory = $true)][string]$StillSource,
    [Parameter(Mandatory = $true)][string]$FlowingSource,
    [Parameter(Mandatory = $true)][string]$OutputDirectory
)

Add-Type -AssemblyName System.Drawing

# Exact opaque colors from the existing jungle tree-tap latex pool and drip.
$dark = [System.Drawing.Color]::FromArgb(255, 189, 196, 188)
$middle = [System.Drawing.Color]::FromArgb(255, 222, 226, 218)
$light = [System.Drawing.Color]::FromArgb(255, 244, 245, 239)

function Get-Luminance([System.Drawing.Color]$color) {
    return 0.2126 * $color.R + 0.7152 * $color.G + 0.0722 * $color.B
}

function Mix-Channel([int]$from, [int]$to, [double]$fraction) {
    return [int][Math]::Round($from + ($to - $from) * $fraction)
}

function Convert-Texture([string]$source, [string]$destination, [int]$width, [int]$height) {
    $inputImage = [System.Drawing.Bitmap]::new($source)
    try {
        if ($inputImage.Width -ne $width -or $inputImage.Height -ne $height) {
            throw "Unexpected source dimensions for $source. Expected ${width}x${height}."
        }

        $minimum = [double]::PositiveInfinity
        $maximum = [double]::NegativeInfinity
        for ($y = 0; $y -lt $height; $y++) {
            for ($x = 0; $x -lt $width; $x++) {
                $pixel = $inputImage.GetPixel($x, $y)
                if ($pixel.A -eq 0) { continue }
                $luminance = Get-Luminance $pixel
                $minimum = [Math]::Min($minimum, $luminance)
                $maximum = [Math]::Max($maximum, $luminance)
            }
        }

        $outputImage = [System.Drawing.Bitmap]::new($width, $height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        try {
            for ($y = 0; $y -lt $height; $y++) {
                for ($x = 0; $x -lt $width; $x++) {
                    $pixel = $inputImage.GetPixel($x, $y)
                    if ($pixel.A -eq 0) {
                        $outputImage.SetPixel($x, $y, $pixel)
                        continue
                    }
                    $position = if ($maximum -eq $minimum) { 0.5 } else {
                        ((Get-Luminance $pixel) - $minimum) / ($maximum - $minimum)
                    }
                    if ($position -le 0.5) {
                        $start = $dark
                        $end = $middle
                        $fraction = $position * 2
                    } else {
                        $start = $middle
                        $end = $light
                        $fraction = ($position - 0.5) * 2
                    }
                    $recolored = [System.Drawing.Color]::FromArgb(
                        $pixel.A,
                        (Mix-Channel $start.R $end.R $fraction),
                        (Mix-Channel $start.G $end.G $fraction),
                        (Mix-Channel $start.B $end.B $fraction))
                    $outputImage.SetPixel($x, $y, $recolored)
                }
            }
            $outputImage.Save($destination, [System.Drawing.Imaging.ImageFormat]::Png)
        } finally {
            $outputImage.Dispose()
        }
    } finally {
        $inputImage.Dispose()
    }
}

New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
Convert-Texture $StillSource (Join-Path $OutputDirectory 'latex_still.png') 16 512
Convert-Texture $FlowingSource (Join-Path $OutputDirectory 'latex_flowing.png') 32 1024
