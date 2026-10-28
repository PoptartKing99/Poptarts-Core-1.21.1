param([switch]$Reset)
$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression.FileSystem
$root=(Resolve-Path "$PSScriptRoot/../../..").Path
$destination=Join-Path $root 'src/main/resources/assets/poptartcore/textures/gui/backpack'
New-Item -ItemType Directory -Force $destination|Out-Null
# Bootstrap only: normal builds never regenerate these artist-editable textures.
if($Reset -or !(Test-Path "$destination/storage.png")){
    $archive=[IO.Compression.ZipFile]::OpenRead("$env:USERPROFILE/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar")
    $stream=$archive.GetEntry('assets/minecraft/textures/gui/container/generic_54.png').Open()
    $source=[Drawing.Bitmap]::new($stream)
    $output=[Drawing.Bitmap]::new(256,256)
    try{
        for($y=0;$y -lt 168;$y++){for($x=0;$x -lt 176;$x++){
            $sourceY=if($y -lt 71){$y}else{126+$y-71}
            # Vanilla draws 96 inventory rows; its final extra row is transparent.
            if($y -lt 167){$output.SetPixel($x,$y,$source.GetPixel($x,$sourceY))}
        }}
        $output.Save("$destination/storage.png",[Drawing.Imaging.ImageFormat]::Png)
    }finally{$output.Dispose();$source.Dispose();$stream.Dispose();$archive.Dispose()}
}
if($Reset -or !(Test-Path "$destination/colors.png")){
    $output=[Drawing.Bitmap]::new(250,232)
    $graphics=[Drawing.Graphics]::FromImage($output)
    function Fill($x,$y,$w,$h,$hex){
        $brush=[Drawing.SolidBrush]::new([Drawing.ColorTranslator]::FromHtml($hex))
        try{$graphics.FillRectangle($brush,$x,$y,$w,$h)}finally{$brush.Dispose()}
    }
    function Panel($x,$y,$w,$h,$hex){
        Fill $x $y $w $h '#373737'
        Fill ($x+1) ($y+1) ($w-2) ($h-2) '#FFFFFF'
        Fill ($x+2) ($y+2) ($w-4) ($h-4) $hex
    }
    function Slot($x,$y){
        Fill ($x-1) ($y-1) 18 18 '#373737'
        Fill $x $y 17 17 '#FFFFFF'
        Fill $x $y 16 16 '#8B8B8B'
    }
    try{
        Panel 0 0 180 208 '#C6C6C6'
        Panel 8 19 89 92 '#292929'
        Slot 105 87
        for($row=0;$row -lt 3;$row++){for($col=0;$col -lt 9;$col++){Slot (10+$col*18) (126+$row*18)}}
        for($col=0;$col -lt 9;$col++){Slot (10+$col*18) 184}
        Panel 154 25 15 15 '#7D4D33'
        Panel 154 51 15 15 '#7D4D33'
        $output.Save("$destination/colors.png",[Drawing.Imaging.ImageFormat]::Png)
    }finally{$graphics.Dispose();$output.Dispose()}
}
Write-Output 'Backpack GUI textures ready. Existing PNG edits are preserved unless -Reset is supplied.'
