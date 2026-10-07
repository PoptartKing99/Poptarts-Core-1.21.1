$packDirectory = Join-Path $PSScriptRoot 'resourcepacks/Conduit Heart Test'
Add-Type -AssemblyName System.Drawing
$texturePaths = @(
    'assets/minecraft/textures/entity/conduit/cage.png',
    'assets/minecraft/textures/entity/conduit/wind.png',
    'assets/minecraft/textures/entity/conduit/wind_vertical.png',
    'assets/minecraft/textures/particle/conduit_test_invisible.png'
)
$bitmap = [System.Drawing.Bitmap]::new(16, 16)
try {
    foreach ($relativePath in $texturePaths) {
        $destination = Join-Path $packDirectory $relativePath
        [System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($destination)) | Out-Null
        $bitmap.Save($destination, [System.Drawing.Imaging.ImageFormat]::Png)
    }
} finally {
    $bitmap.Dispose()
}
$archive = Join-Path $PSScriptRoot '../../build/Conduit Heart Test.zip'
Compress-Archive -Path (Join-Path $packDirectory '*') -DestinationPath $archive -Force
Write-Output ([System.IO.Path]::GetFullPath($archive))
