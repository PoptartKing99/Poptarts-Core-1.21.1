param([ValidateSet('backpack','rucksack')][string]$Id='backpack')
$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression.FileSystem
$root=(Resolve-Path "$PSScriptRoot/../..").Path
$assets="$root/src/main/resources/assets/poptartcore"
$original=[Drawing.Bitmap]::new("$assets/textures/block/$Id.png")
$layers=@{}
foreach($name in @('body','body_neutral','flaps_brown','metal','flaps')){
    $layers[$name]=[Drawing.Bitmap]::new("$assets/textures/block/${Id}_$name.png")
}
try{
    for($y=0;$y -lt $original.Height;$y++){for($x=0;$x -lt $original.Width;$x++){
        $o=$original.GetPixel($x,$y);$coverage=0
        foreach($name in @('body','flaps_brown','metal')){
            $pixel=$layers[$name].GetPixel($x,$y)
            if($pixel.A){$coverage++;if($pixel.ToArgb() -ne $o.ToArgb()){throw "Original material changed: $name $x,$y"}}
        }
        if($coverage -ne $(if($o.A){1}else{0})){throw "Material overlap or hole: $x,$y"}
        foreach($pair in @(@('body','body_neutral'),@('flaps_brown','flaps'))){
            $brown=$layers[$pair[0]].GetPixel($x,$y);$neutral=$layers[$pair[1]].GetPixel($x,$y)
            if($brown.A -ne $neutral.A){throw 'Neutral material coverage changed'}
            if($neutral.A -and ($neutral.R -ne $neutral.G -or $neutral.G -ne $neutral.B)){throw 'Non-neutral dye pixel'}
        }
    }}
}finally{$original.Dispose();foreach($bitmap in $layers.Values){$bitmap.Dispose()}}
$normal=Get-Content "$assets/models/item/$Id.json" -Raw|ConvertFrom-Json -AsHashtable
$names=@{1="${Id}_dyed";2="${Id}_body_dyed";3="${Id}_both_dyed"}
foreach($bits in 1..3){
    $name=$names[$bits]
    $model=Get-Content "$assets/models/block/$name.json" -Raw|ConvertFrom-Json -AsHashtable
    foreach($element in $model.elements){foreach($face in $element.faces.Values){
        $expected=if($face.texture -eq '#flaps' -and ($bits -band 1)){0}elseif($face.texture -eq '#body' -and ($bits -band 2)){1}else{-1}
        if($expected -eq -1 -and $face.ContainsKey('tintindex')){throw "Fixed material tinted: $name"}
        if($expected -ge 0 -and (!$face.ContainsKey('tintindex') -or $face.tintindex -ne $expected)){throw "Wrong tint index: $name"}
    }}
    $dyed=Get-Content "$assets/models/item/$name.json" -Raw|ConvertFrom-Json -AsHashtable
    foreach($context in $normal.display.Keys){foreach($property in @('rotation','translation','scale')){
        if(($normal.display[$context][$property] -join ',') -ne ($dyed.display[$context][$property] -join ',')){throw 'Dyed pose differs from normal pose'}
    }}
    if($normal.overrides[$bits-1].predicate['poptartcore:dyed'] -ne $bits){throw 'Override order changed'}
}
$paths=@("assets/poptartcore/blockstates/$Id.json","data/poptartcore/loot_table/blocks/$Id.json")
$paths+="assets/poptartcore/textures/block/$Id.png"
foreach($name in @('flaps','body','body_neutral','flaps_brown','metal')){$paths+="assets/poptartcore/textures/block/${Id}_$name.png"}
if($Id -eq 'rucksack'){foreach($name in $names.Values){$paths+="assets/poptartcore/models/block/${name}_placed.json"}}
foreach($name in $names.Values){$paths+="assets/poptartcore/models/block/$name.json";$paths+="assets/poptartcore/models/item/$name.json"}
$archive=[IO.Compression.ZipFile]::OpenRead("$root/build/libs/poptartcore-0.0.1.jar")
try{foreach($path in $paths){
    $entry=$archive.GetEntry($path)
    if(!$entry){throw "Missing packaged asset: $path"}
    $stream=$entry.Open();$hasher=[Security.Cryptography.SHA256]::Create()
    try{$actual=[Convert]::ToHexString($hasher.ComputeHash($stream))}finally{$stream.Dispose();$hasher.Dispose()}
    if($actual -ne (Get-FileHash "$root/src/main/resources/$path").Hash){throw "Stale packaged asset: $path"}
}}finally{$archive.Dispose()}
Write-Output 'Verified disjoint materials, original brown/metal pixels, neutral masks, independent tint indices, matching poses, and packaged dye assets.'
