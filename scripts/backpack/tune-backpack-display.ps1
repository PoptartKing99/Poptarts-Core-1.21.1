param([switch]$VerifyOnly)
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path "$PSScriptRoot/../..").Path
$assets = Join-Path $root 'src/main/resources/assets/poptartcore/models'

function Get-ProjectedBounds($model, $rotation) {
    $rx = $rotation[0] * [Math]::PI / 180
    $ry = $rotation[1] * [Math]::PI / 180
    $points = foreach ($element in $model.elements) {
        foreach ($x in @($element.from[0],$element.to[0])) {
            foreach ($y in @($element.from[1],$element.to[1])) {
                foreach ($z in @($element.from[2],$element.to[2])) {
                    $px=$x-8; $py=$y-8; $pz=$z-8
                    # Minecraft ItemTransform uses XYZ rotation; these GUI poses have zero Z rotation.
                    [pscustomobject]@{
                        X=[Math]::Cos($ry)*$px+[Math]::Sin($ry)*$pz
                        Y=[Math]::Sin($rx)*[Math]::Sin($ry)*$px+[Math]::Cos($rx)*$py-[Math]::Sin($rx)*[Math]::Cos($ry)*$pz
                    }
                }
            }
        }
    }
    $xs=$points.X | Measure-Object -Minimum -Maximum
    $ys=$points.Y | Measure-Object -Minimum -Maximum
    return @{Left=$xs.Minimum;Right=$xs.Maximum;Bottom=$ys.Minimum;Top=$ys.Maximum}
}
foreach ($id in @('backpack','rucksack')) {
    $path=Join-Path $assets "item/$id.json"
    $item=Get-Content -Raw $path | ConvertFrom-Json -AsHashtable
    $body=Get-Content -Raw (Join-Path $assets "block/$id.json") | ConvertFrom-Json
    if (!$VerifyOnly) {
        $rotation=@(20,30,0)
        $bounds=Get-ProjectedBounds $body $rotation
        $guiSize=if($id -eq 'backpack'){12}else{14}
        $scale=[Math]::Min($guiSize/($bounds.Right-$bounds.Left),$guiSize/($bounds.Top-$bounds.Bottom))
        $scale=[Math]::Floor($scale*10000)/10000
        $item.display.gui=@{
            rotation=$rotation
            translation=@([Math]::Round(-($bounds.Left+$bounds.Right)/2*$scale,4),[Math]::Round(-($bounds.Bottom+$bounds.Top)/2*$scale,4),0)
            scale=@($scale,$scale,$scale)
        }
        $large=$id -eq 'rucksack'
        # Use the same world-unit scale so the rucksack's larger geometry stays visibly larger.
        $firstScale=0.415
        $thirdScale=0.39
        foreach($hand in @('righthand','lefthand')) {
            $item.display["firstperson_$hand"]=@{
                rotation=@(0,0,0)
                translation=$(if($large){@(0,0,-2)}else{@(0,2,-1)})
                scale=@($firstScale,$firstScale,$firstScale)
            }
            $item.display["thirdperson_$hand"]=@{
                rotation=@(75,225,0)
                translation=$(if($large){@(0,1.5,-2)}else{@(0,2,-1)})
                scale=@($thirdScale,$thirdScale,$thirdScale)
            }
        }
        [IO.File]::WriteAllText($path,($item | ConvertTo-Json -Depth 32)+"`n",[Text.UTF8Encoding]::new($false))
    }
    $bounds=Get-ProjectedBounds $body $item.display.gui.rotation
    $scale=$item.display.gui.scale[0]
    $tx=$item.display.gui.translation[0];$ty=$item.display.gui.translation[1]
    $left=$bounds.Left*$scale+$tx; $right=$bounds.Right*$scale+$tx
    $bottom=$bounds.Bottom*$scale+$ty; $top=$bounds.Top*$scale+$ty
    if([Math]::Min($left,$bottom) -lt -7.01 -or [Math]::Max($right,$top) -gt 7.01){throw "$id extends outside its inventory framing"}
    if([Math]::Abs($left+$right) -gt .01 -or [Math]::Abs($top+$bottom) -gt .01){throw "$id is not centered"}
    foreach($context in @('firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand')) {
        if($item.display[$context].scale.Count -ne 3){throw "Missing $id $context pose"}
    }
    Write-Output ("{0}: centered GUI {1:N2}x{2:N2} pixels; left/right first- and third-person poses verified." -f $id,($right-$left),($top-$bottom))
}
if (!$VerifyOnly) { & "$PSScriptRoot/build-dye-models.ps1" }
