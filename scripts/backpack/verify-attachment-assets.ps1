$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$root=(Resolve-Path "$PSScriptRoot/../..").Path
$assets=Join-Path $root 'src/main/resources/assets/poptartcore'
$archive=[IO.Compression.ZipFile]::OpenRead("$root/build/libs/poptartcore-0.0.1.jar")
try{
    foreach($name in @('compass','clock_day','clock_night','recovery_compass','heavy_core','bundle','rucksack_banner','spyglass')){
        $path="models/item/backpack_attachments/$name.json"
        $model=Get-Content "$assets/$path" -Raw|ConvertFrom-Json -AsHashtable
        if(!$archive.GetEntry("assets/poptartcore/$path")){throw "Missing attachment model: $name"}
        if($name -eq 'spyglass' -and (!$model.elements.Count -or !$model.display.ContainsKey('head'))){throw 'Spyglass must contain editable geometry and a Head display transform'}
        foreach($texture in $model.textures.Values){
            if($texture.StartsWith('#')){continue}
            if(!$texture.StartsWith('poptartcore:')){throw "Unexpected attachment texture namespace: $texture"}
            $texturePath='textures/'+$texture.Substring(12)+'.png'
            if(!(Test-Path "$assets/$texturePath") -or !$archive.GetEntry("assets/poptartcore/$texturePath")){
                throw "Missing attachment texture: $texturePath"
            }
        }
    }
    foreach($id in @('backpack','rucksack')){
        if(!$archive.GetEntry("assets/poptartcore/textures/gui/backpack/${id}_attachments_panel.png")){throw 'Missing Wayward attachment panel texture'}
        $loot=Get-Content "$root/src/main/resources/data/poptartcore/loot_table/blocks/$id.json" -Raw|ConvertFrom-Json -AsHashtable
        if($loot.pools[0].entries[0].functions[0].include -notcontains 'poptartcore:backpack_attachments'){throw 'Loot would lose attachments'}
    }
    Write-Output 'Verified eight attachment models, editable 3D spyglass, referenced textures, packaged resources, and attachment loot preservation.'
}finally{$archive.Dispose()}
