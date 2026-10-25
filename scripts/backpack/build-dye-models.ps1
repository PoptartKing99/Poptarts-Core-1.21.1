param([ValidateSet('backpack','rucksack')][string]$Id='backpack')
$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Drawing
$root=(Resolve-Path "$PSScriptRoot/../..").Path
$assets=Join-Path $root 'src/main/resources/assets/poptartcore'
if(!(Test-Path "$assets/textures/block/${Id}_flaps.png")){return}
# The artist's flap mask is authoritative. Its complement contains body leather and fixed metal.
$original=[Drawing.Bitmap]::new("$assets/textures/block/$Id.png")
$mask=[Drawing.Bitmap]::new("$assets/textures/block/${Id}_flaps.png")
$layers=@{}
foreach($name in @('body','body_neutral','flaps_brown','metal')){
    $layers[$name]=[Drawing.Bitmap]::new($original.Width,$original.Height)
}
$ramp=@{'3B261A'=0x66;'472D1E'=0x7A;'503423'=0x8C;'5C3C2A'=0xA2;'6E4630'=0xBE;'7D4D33'=0xD3;'8E5537'=0xEB}
try{
    for($y=0;$y -lt $original.Height;$y++){for($x=0;$x -lt $original.Width;$x++){
        $pixel=$original.GetPixel($x,$y)
        if(!$pixel.A){continue}
        $key='{0:X2}{1:X2}{2:X2}' -f $pixel.R,$pixel.G,$pixel.B
        if($mask.GetPixel($x,$y).A){
            if(!$ramp.ContainsKey($key)){throw "Flap mask includes non-leather pixel $x,$y"}
            $layers.flaps_brown.SetPixel($x,$y,$pixel)
        }elseif($ramp.ContainsKey($key)){
            $layers.body.SetPixel($x,$y,$pixel)
            $gray=$ramp[$key]
            $layers.body_neutral.SetPixel($x,$y,[Drawing.Color]::FromArgb($pixel.A,$gray,$gray,$gray))
        }else{$layers.metal.SetPixel($x,$y,$pixel)}
    }}
    foreach($name in $layers.Keys){$layers[$name].Save("$assets/textures/block/${Id}_$name.png",[Drawing.Imaging.ImageFormat]::Png)}
}finally{$original.Dispose();$mask.Dispose();foreach($bitmap in $layers.Values){$bitmap.Dispose()}}

function Write-Json($path,$value){[IO.File]::WriteAllText($path,($value|ConvertTo-Json -Depth 32)+"`n",[Text.UTF8Encoding]::new($false))}
$originalModel=Get-Content "$assets/models/block/$Id.json" -Raw|ConvertFrom-Json -AsHashtable
if(@($originalModel.elements|Where-Object{$_.name -like 'poptartcore_solid_backing_*'}).Count){throw 'Source model still contains backing boxes'}
if($originalModel.elements.Count -ne $(if($Id -eq 'rucksack'){6}else{4})){throw 'Unexpected source model geometry'}
if($Id -eq 'rucksack'){
    $grounded=$originalModel|ConvertTo-Json -Depth 32|ConvertFrom-Json -AsHashtable
    foreach($element in $grounded.elements){$element.from[1]-=3;$element.to[1]-=3}
    Write-Json "$assets/models/block/rucksack_placed.json" $grounded
}
$item=Get-Content "$assets/models/item/$Id.json" -Raw|ConvertFrom-Json -AsHashtable
$names=@{1="${Id}_dyed";2="${Id}_body_dyed";3="${Id}_both_dyed"}
$item.overrides=@()
foreach($bits in 1..3){
    $name=$names[$bits]
    $model=@{render_type='minecraft:cutout';textures=@{
        body=$(if($bits -band 2){"poptartcore:block/${Id}_body_neutral"}else{"poptartcore:block/${Id}_body"})
        flaps=$(if($bits -band 1){"poptartcore:block/${Id}_flaps"}else{"poptartcore:block/${Id}_flaps_brown"})
        metal="poptartcore:block/${Id}_metal";particle="poptartcore:block/$Id"
    };elements=@()}
    foreach($layer in @('metal','body','flaps')){foreach($element in $originalModel.elements){
        $copy=$element|ConvertTo-Json -Depth 24|ConvertFrom-Json -AsHashtable
        foreach($face in $copy.faces.Values){
            $face.texture='#'+$layer
            $face.Remove('tintindex')|Out-Null
            if($layer -eq 'flaps' -and ($bits -band 1)){$face.tintindex=0}
            if($layer -eq 'body' -and ($bits -band 2)){$face.tintindex=1}
        }
        $model.elements+=,$copy
    }}
    Write-Json "$assets/models/block/$name.json" $model
    if($Id -eq 'rucksack'){
        $placed=$model|ConvertTo-Json -Depth 32|ConvertFrom-Json -AsHashtable
        foreach($element in $placed.elements){$element.from[1]-=3;$element.to[1]-=3}
        Write-Json "$assets/models/block/${name}_placed.json" $placed
    }
    Write-Json "$assets/models/item/$name.json" @{parent="poptartcore:block/$name";display=$item.display}
    $item.overrides+=@{predicate=@{'poptartcore:dyed'=$bits};model="poptartcore:item/$name"}
}
Write-Json "$assets/models/item/$Id.json" $item
$variants=@{}
foreach($direction in @('north','east','south','west')){
    $rotation=@{north=180;east=270;south=0;west=90}[$direction]
    foreach($body in @('false','true')){foreach($flaps in @('false','true')){
        $bits=$(if($flaps -eq 'true'){1}else{0})+$(if($body -eq 'true'){2}else{0})
        $name=if($bits){$names[$bits]}else{$Id}
        if($Id -eq 'rucksack'){
            $variants["facing=$direction,dyed=$flaps,body_dyed=$body,half=lower"]=@{model="poptartcore:block/${name}_placed";y=$rotation}
            $variants["facing=$direction,dyed=$flaps,body_dyed=$body,half=upper"]=@{model='poptartcore:block/rucksack_upper'}
        }else{$variants["facing=$direction,dyed=$flaps,body_dyed=$body"]=@{model="poptartcore:block/$name";y=$rotation}}
    }}
}
Write-Json "$assets/blockstates/$Id.json" @{variants=$variants}
Write-Output "Generated $Id body/flap materials with fixed metal and all four color states."
if($Id -eq 'backpack' -and (Test-Path "$assets/textures/block/rucksack_flaps.png")){
    & $PSCommandPath -Id rucksack
}
