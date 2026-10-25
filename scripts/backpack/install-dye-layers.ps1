param(
    [string]$Flaps='C:/Users/badas/AppData/Local/Temp/codex-clipboard-d072fba0-4ecd-4b20-ae79-f27007469f6a.png'
)
$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Drawing
$root=(Resolve-Path "$PSScriptRoot/../..").Path
$destination="$root/src/main/resources/assets/poptartcore/textures/block"
$flap=[Drawing.Bitmap]::new($Flaps)
$original=[Drawing.Bitmap]::new("$destination/backpack.png")
try{
    if($flap.Size -ne $original.Size){throw 'Layer size mismatch'}
    for($y=0;$y -lt $flap.Height;$y++){for($x=0;$x -lt $flap.Width;$x++){
        $f=$flap.GetPixel($x,$y);$o=$original.GetPixel($x,$y)
        if($f.A -gt 0){
            if($f.R -ne $f.G -or $f.G -ne $f.B){throw 'Flap pixel is not neutral gray'}
            if($o.A -eq 0 -or !($o.R -gt $o.G*1.1 -and $o.R -gt $o.B*1.3)){throw 'Selected flap pixel was not leather'}
        }
    }}
    Copy-Item -LiteralPath $Flaps -Destination "$destination/backpack_flaps.png"
    'Copied supplied flap mask. Body and metal layers are derived from the original texture.'
}finally{$flap.Dispose();$original.Dispose()}
& "$PSScriptRoot/build-dye-models.ps1"
