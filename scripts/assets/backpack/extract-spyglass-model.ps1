param([switch]$Reset)
$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$root=(Resolve-Path "$PSScriptRoot/../../..").Path
$assets=Join-Path $root 'src/main/resources/assets/poptartcore'
$modelPath="$assets/models/item/backpack_attachments/spyglass.json"
$texturePath="$assets/textures/item/backpack_attachments/spyglass.png"
if((Test-Path $modelPath) -and !$Reset){
    $existing=Get-Content $modelPath -Raw|ConvertFrom-Json -AsHashtable
    if($existing.ContainsKey('elements')){throw 'Editable spyglass already exists. Use -Reset only to discard your edits.'}
}
$zip=[IO.Compression.ZipFile]::OpenRead("$env:USERPROFILE/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar")
try{
    $reader=[IO.StreamReader]::new($zip.GetEntry('assets/minecraft/models/item/spyglass_in_hand.json').Open())
    try{$model=$reader.ReadToEnd()|ConvertFrom-Json -AsHashtable}finally{$reader.Dispose()}
    $model.textures.spyglass='poptartcore:item/backpack_attachments/spyglass'
    # Use one editable display transform, independent of vanilla's hand/GUI presets.
    $model.display=[ordered]@{head=[ordered]@{rotation=@(0,0,0);translation=@(0,0,0);scale=@(1,1,1)}}
    [IO.Compression.ZipFileExtensions]::ExtractToFile($zip.GetEntry('assets/minecraft/textures/item/spyglass_model.png'),$texturePath,$true)
    [IO.File]::WriteAllText($modelPath,($model|ConvertTo-Json -Depth 32)+"`n",[Text.UTF8Encoding]::new($false))
}finally{$zip.Dispose()}
Write-Output 'Extracted editable vanilla spyglass geometry and texture; Head display controls the attachment transform.'
