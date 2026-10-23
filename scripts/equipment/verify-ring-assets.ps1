param(
    [string]$ReferenceTextures = 'C:/Users/badas/IdeaProjects/wayfarer_core_decompiled_clean/sources/assets/wayfarer/textures/item/rings',
    [string]$JarPath
)
$ErrorActionPreference = 'Stop'
$ringRepo = (Resolve-Path "$PSScriptRoot/../..").Path
$ringNames = @('rusted_iron_ring', 'dull_iron_ring', 'moonlight_ring', 'ring_of_warmth', 'fireward_ring', 'silvercat_ring', 'tiny_horseshoe', 'thorn_ring', 'ring_of_the_afflicted', 'mercury_gemstone_ring', 'ancient_blessing', 'sunlight_ring', 'malevolent_ring')
$ringTag = Get-Content -Raw "$ringRepo/src/main/resources/data/poptartcore/tags/item/equipment/rings.json" | ConvertFrom-Json
$ringLanguage = Get-Content -Raw "$ringRepo/src/main/resources/assets/poptartcore/lang/en_us.json" | ConvertFrom-Json -AsHashtable
foreach ($ringName in $ringNames) {
    $ringSource = Join-Path $ReferenceTextures "$ringName.png"
    $ringTarget = Join-Path $ringRepo "src/main/resources/assets/poptartcore/textures/item/$ringName.png"
    if ((Get-FileHash -LiteralPath $ringSource).Hash -ne (Get-FileHash -LiteralPath $ringTarget).Hash) {
        throw "Ring texture differs from supplied asset: $ringName"
    }
    $ringModel = Get-Content -Raw "$ringRepo/src/main/resources/assets/poptartcore/models/item/$ringName.json" | ConvertFrom-Json
    if ($ringModel.textures.layer0 -ne "poptartcore:item/$ringName") { throw "Wrong model texture: $ringName" }
    if ($ringTag.values -notcontains "poptartcore:$ringName") { throw "Missing ring slot tag: $ringName" }
    if (!$ringLanguage["item.poptartcore.$ringName"] -or !$ringLanguage["item.poptartcore.$ringName.desc"]) { throw "Missing name or tooltip: $ringName" }
    if (Test-Path -LiteralPath "$ringRepo/src/main/resources/data/poptartcore/recipe/$ringName.json") { throw "Unexpected ring recipe: $ringName" }
}
foreach ($materialName in @('bone_ash', 'chthonic_calculus')) {
    $source = Join-Path (Split-Path $ReferenceTextures) "$materialName.png"
    $target = "$ringRepo/src/main/resources/assets/poptartcore/textures/item/$materialName.png"
    if ((Get-FileHash -LiteralPath $source).Hash -ne (Get-FileHash -LiteralPath $target).Hash) { throw "Material texture differs: $materialName" }
    $model = Get-Content -Raw "$ringRepo/src/main/resources/assets/poptartcore/models/item/$materialName.json" | ConvertFrom-Json
    if ($model.textures.layer0 -ne "poptartcore:item/$materialName" -or !$ringLanguage["item.poptartcore.$materialName"]) { throw "Missing material model or name: $materialName" }
    if (Test-Path -LiteralPath "$ringRepo/src/main/resources/data/poptartcore/recipe/$materialName.json") { throw "Unexpected material recipe: $materialName" }
}
Write-Output 'All thirteen ring textures and both repair materials match their source assets; models, tags, names and tooltips verified. No recipes.'
if ($JarPath) {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = [IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $JarPath).Path)
    try {
        $packagedFiles = @('assets/poptartcore/lang/en_us.json', 'data/poptartcore/tags/item/equipment/rings.json', 'META-INF/enumextensions.json')
        foreach ($name in ($ringNames + @('bone_ash', 'chthonic_calculus'))) {
            $packagedFiles += "assets/poptartcore/textures/item/$name.png", "assets/poptartcore/models/item/$name.json"
        }
        foreach ($path in $packagedFiles) {
            $entry = $archive.GetEntry($path)
            if (!$entry) { throw "Missing packaged ring resource: $path" }
            $stream = $entry.Open()
            $sha = [Security.Cryptography.SHA256]::Create()
            try { $hash = [Convert]::ToHexString($sha.ComputeHash($stream)) }
            finally { $stream.Dispose(); $sha.Dispose() }
            if ($hash -ne (Get-FileHash -LiteralPath "$ringRepo/src/main/resources/$path").Hash) { throw "Packaged resource differs: $path" }
        }
        $newIds = @('tiny_horseshoe', 'thorn_ring', 'ring_of_the_afflicted', 'mercury_gemstone_ring', 'ancient_blessing', 'sunlight_ring', 'malevolent_ring', 'bone_ash', 'chthonic_calculus')
        $forbiddenRecipeItem = '"poptartcore:(' + ($newIds -join '|') + ')"'
        foreach ($entry in $archive.Entries) {
            if ($entry.FullName -notmatch '^data/poptartcore/recipe/.+\.json$') { continue }
            $reader = [IO.StreamReader]::new($entry.Open())
            try { $recipe = $reader.ReadToEnd() } finally { $reader.Dispose() }
            if ($recipe -match $forbiddenRecipeItem) { throw "Unexpected new item recipe: $($entry.FullName)" }
        }
        $metadata = $archive.GetEntry('META-INF/neoforge.mods.toml')
        $reader = [IO.StreamReader]::new($metadata.Open())
        try { $metadataText = $reader.ReadToEnd() } finally { $reader.Dispose() }
        if ($metadataText -notmatch 'enumExtensions="META-INF/enumextensions.json"') { throw 'Packaged rarity extension is not enabled' }
        Write-Output 'Built JAR resources match the checked source assets; rarity metadata and absence of new recipes verified.'
    } finally { $archive.Dispose() }
}
