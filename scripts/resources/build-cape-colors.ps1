param([switch]$VerifyOnly, [string]$JarPath)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$capeRepo = (Resolve-Path "$PSScriptRoot/../..").Path
$capeRoot = "$capeRepo/src/main/resources/assets/poptartcore"
foreach ($surface in @('item','entity')) {
    $obsolete = @(Get-ChildItem -LiteralPath "$capeRoot/textures/$surface" -File | Where-Object Name -Like '*_cape.png')
    if ($obsolete.Count) { throw "Unused standalone cape textures remain: $($obsolete.Name -join ', ')" }
}
foreach ($model in Get-ChildItem -LiteralPath "$capeRoot/models/item" -File) {
    if ($model.Name -like '*_cape.json' -and $model.Name -ne 'agility_cape.json') {
        throw "Unused standalone cape model remains: $($model.Name)"
    }
}
$capePalette = Get-Content -Raw "$PSScriptRoot/cape-palette.json" | ConvertFrom-Json -AsHashtable
$capeNames = @('white','orange','magenta','light_blue','yellow','lime','pink','gray','light_gray','cyan','purple','blue','brown','green','red','black')
if ($capePalette.Count -ne 16) { throw 'Expected exactly sixteen dye colors' }
$capeShades = @{}
for ($i=0; $i -lt 5; $i++) { $capeShades[$capePalette.white[$i]] = $i }
foreach ($surface in @('item','entity')) {
    $source = [Drawing.Bitmap]::new("$capeRoot/textures/$surface/cape/white.png")
    try {
        $expectedWidth = if ($surface -eq 'item') { 16 } else { 64 }
        $expectedHeight = if ($surface -eq 'item') { 16 } else { 32 }
        if ($source.Width -ne $expectedWidth -or $source.Height -ne $expectedHeight) { throw "Unexpected $surface UV dimensions" }
        if (!$VerifyOnly) { New-Item -ItemType Directory -Force -Path "$capeRoot/textures/$surface/cape" | Out-Null }
        foreach ($name in $capeNames) {
            $output = [Drawing.Bitmap]::new($source.Width, $source.Height, [Drawing.Imaging.PixelFormat]::Format32bppArgb)
            $existing = $null
            try {
                if ($capePalette[$name].Count -ne 5) { throw "Expected five fabric shades for $name" }
                $path = "$capeRoot/textures/$surface/cape/$name.png"
                if ($VerifyOnly) { $existing = [Drawing.Bitmap]::new($path) }
                if ($existing -and ($existing.Width -ne $source.Width -or $existing.Height -ne $source.Height)) { throw "Stretched dimensions: $surface/$name" }
                $fabricPixels = 0
                for ($y=0; $y -lt $source.Height; $y++) {
                    for ($x=0; $x -lt $source.Width; $x++) {
                        $original = $source.GetPixel($x,$y)
                        $rgb = '{0:X2}{1:X2}{2:X2}' -f $original.R,$original.G,$original.B
                        $pixel = $original
                        if ($capeShades.ContainsKey($rgb)) {
                            $fabricPixels++
                            $color = [Convert]::ToInt32($capePalette[$name][$capeShades[$rgb]],16)
                            $pixel = [Drawing.Color]::FromArgb($original.A, ($color -shr 16) -band 255, ($color -shr 8) -band 255, $color -band 255)
                        }
                        $output.SetPixel($x,$y,$pixel)
                        if ($existing -and $existing.GetPixel($x,$y).ToArgb() -ne $pixel.ToArgb()) { throw "Changed trim, transparency or fabric shade at $surface/$name [$x,$y]" }
                    }
                }
                if ($fabricPixels -eq 0) { throw 'Fabric mask is empty' }
                if (!$VerifyOnly -and $name -ne 'white') { $output.Save($path,[Drawing.Imaging.ImageFormat]::Png) }
            } finally {
                $output.Dispose()
                if ($existing) { $existing.Dispose() }
            }
        }
    } finally { $source.Dispose() }
}
# Item overrides and entity texture selection both follow vanilla's stable DyeColor ID order.
$baseModel = Get-Content -Raw "$capeRoot/models/item/agility_cape.json" | ConvertFrom-Json
if ($baseModel.overrides.Count -ne 16) { throw 'Expected sixteen inventory model overrides' }
for ($i=0; $i -lt $capeNames.Count; $i++) {
    $name = $capeNames[$i]
    $override = $baseModel.overrides[$i]
    if ($override.predicate.'poptartcore:cape_color' -ne (($i+1)/16) -or $override.model -ne "poptartcore:item/cape/$name") { throw "Inventory model mapping mismatch: $name" }
    $model = Get-Content -Raw "$capeRoot/models/item/cape/$name.json" | ConvertFrom-Json
    if ($model.textures.layer0 -ne "poptartcore:item/cape/$name") { throw "Wrong inventory texture: $name" }
}
if (!$VerifyOnly) {
    $preview = [Drawing.Bitmap]::new(576,688)
    $graphics = [Drawing.Graphics]::FromImage($preview)
    $font = [Drawing.Font]::new('Consolas',11)
    try {
        $graphics.Clear([Drawing.Color]::FromArgb(255,35,35,35))
        $graphics.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
        $graphics.PixelOffsetMode = [Drawing.Drawing2D.PixelOffsetMode]::Half
        for ($i=0; $i -lt $capeNames.Count; $i++) {
            $name=$capeNames[$i]; $tileX=($i%4)*144; $tileY=[Math]::Floor($i/4)*172
            $icon=[Drawing.Bitmap]::new("$capeRoot/textures/item/cape/$name.png")
            $worn=[Drawing.Bitmap]::new("$capeRoot/textures/entity/cape/$name.png")
            try {
                $graphics.DrawString($name,$font,[Drawing.Brushes]::White,$tileX+5,$tileY+3)
                $graphics.DrawImage($icon,[Drawing.Rectangle]::new($tileX+5,$tileY+27,64,64),0,0,16,16,[Drawing.GraphicsUnit]::Pixel)
                $graphics.DrawImage($worn,[Drawing.Rectangle]::new($tileX+4,$tileY+99,132,68),0,0,33,17,[Drawing.GraphicsUnit]::Pixel)
            } finally { $icon.Dispose(); $worn.Dispose() }
        }
        $preview.Save("$capeRepo/build/cape-colors-preview.png",[Drawing.Imaging.ImageFormat]::Png)
    } finally { $graphics.Dispose(); $font.Dispose(); $preview.Dispose() }
}
Write-Output 'Verified sixteen cape colors: all trim, alpha, shading and UV dimensions preserved.'
if ($JarPath) {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = [IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $JarPath).Path)
    try {
        foreach ($entry in $archive.Entries) {
            if ($entry.FullName -match '^assets/poptartcore/textures/(item|entity)/[^/]+_cape\.png$' -or
                ($entry.FullName -match '^assets/poptartcore/models/item/[^/]+_cape\.json$' -and
                 $entry.FullName -ne 'assets/poptartcore/models/item/agility_cape.json')) {
                throw "Obsolete cape resource packaged: $($entry.FullName)"
            }
        }
        $paths = @('assets/poptartcore/models/item/agility_cape.json', 'assets/poptartcore/lang/en_us.json', 'data/poptartcore/recipe/cape_dye.json')
        foreach ($name in $capeNames) {
            $paths += "assets/poptartcore/textures/item/cape/$name.png", "assets/poptartcore/textures/entity/cape/$name.png", "assets/poptartcore/models/item/cape/$name.json"
        }
        foreach ($path in $paths) {
            $entry = $archive.GetEntry($path)
            if (!$entry) { throw "Missing packaged cape resource: $path" }
            $stream = $entry.Open()
            $sha = [Security.Cryptography.SHA256]::Create()
            try { $hash = [Convert]::ToHexString($sha.ComputeHash($stream)) }
            finally { $stream.Dispose(); $sha.Dispose() }
            if ($hash -ne (Get-FileHash -LiteralPath "$capeRepo/src/main/resources/$path").Hash) { throw "Packaged cape resource differs: $path" }
        }
        Write-Output 'Built JAR contains the verified sixteen inventory and worn textures, models, language and dye recipe.'
    } finally { $archive.Dispose() }
}
