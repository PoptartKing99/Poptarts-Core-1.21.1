$ErrorActionPreference = 'Stop'
$root = (Resolve-Path "$PSScriptRoot/../../..").Path
$assets = Join-Path $root 'src/main/resources/assets/poptartcore'
$model = Get-Content -Raw "$assets/models/block/backpack.json" | ConvertFrom-Json
$exterior = @($model.elements | Where-Object { $_.name -notlike 'poptartcore_solid_backing_*' })
$variants = (Get-Content -Raw "$assets/blockstates/backpack.json" | ConvertFrom-Json -AsHashtable).variants
# The front pocket projects beyond the main body toward south in the imported model.
if ($exterior[1].from[2] -ne $exterior[0].to[2] -or $exterior[1].to[2] -le $exterior[0].to[2]) {
    throw 'Reference model front changed; review the facing convention.'
}
$rotations = @{north=180; east=270; south=0; west=90}
foreach ($direction in $rotations.Keys) {
    foreach ($body in @('false','true')) { foreach ($dyed in @('false','true')) {
        $variant = $variants["facing=$direction,dyed=$dyed,body_dyed=$body"]
        $expected = if ($body -eq 'true') {
            if ($dyed -eq 'true') {'poptartcore:block/backpack_both_dyed'} else {'poptartcore:block/backpack_body_dyed'}
        } else { if ($dyed -eq 'true') {'poptartcore:block/backpack_dyed'} else {'poptartcore:block/backpack'} }
        if ($variant.model -ne $expected -or [int]$variant.y -ne $rotations[$direction]) {
            throw "Backpack model front does not match facing=$direction dyed=$dyed"
        }
    }}
}
Write-Output 'Backpack front matches FACING in all four directions.'
