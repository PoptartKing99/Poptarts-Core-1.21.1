param([string]$Reference="$PSScriptRoot/../../../../decompiled-review-2026-10-24/wayward-neoforge-1.0.0+1.21.1/assets/wayward/models/block")
$ErrorActionPreference='Stop'
$root=(Resolve-Path "$PSScriptRoot/../../..").Path
$models=Join-Path $root 'src/main/resources/assets/poptartcore/models/block'
foreach($id in @('backpack','rucksack')){
    $expected=(Get-Content "$Reference/$id.json" -Raw).Replace('wayward:','poptartcore:')|ConvertFrom-Json -AsHashtable
    $actual=Get-Content "$models/$id.json" -Raw|ConvertFrom-Json -AsHashtable
    if(($expected|ConvertTo-Json -Depth 32 -Compress) -ne ($actual|ConvertTo-Json -Depth 32 -Compress)){
        throw "$id source geometry or UV mapping differs from Wayward"
    }
    foreach($variant in @('dyed','body_dyed','both_dyed')){
        $model=Get-Content "$models/${id}_$variant.json" -Raw|ConvertFrom-Json -AsHashtable
        if($model.elements.Count -ne $expected.elements.Count*3){throw 'Unexpected dye geometry or backing boxes'}
        for($i=0;$i -lt $model.elements.Count;$i++){
            $referenceElement=$expected.elements[$i % $expected.elements.Count]
            $element=$model.elements[$i]
            foreach($key in @('from','to')){if(($element[$key] -join ',') -ne ($referenceElement[$key] -join ',')){throw 'Dye model moved source geometry'}}
            foreach($face in $referenceElement.faces.Keys){
                if(($element.faces[$face].uv -join ',') -ne ($referenceElement.faces[$face].uv -join ',')){throw 'Dye model changed original UVs'}
            }
        }
    }
}
Write-Output 'Verified original Wayward geometry and UVs, with no backing boxes in either bag or dye variants.'
