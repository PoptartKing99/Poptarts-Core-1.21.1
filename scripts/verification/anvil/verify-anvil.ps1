param([switch]$RunServer)
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/../../lib/resources.ps1"
$repo = (Resolve-Path "$PSScriptRoot/../../..").Path
$names = @('anvil','chipped_anvil','damaged_anvil','primitive_anvil','chipped_primitive_anvil','damaged_primitive_anvil')
$assets = "$repo/src/main/resources/assets/poptartcore"
foreach ($name in $names) {
    foreach ($path in @("$assets/blockstates/$name.json","$assets/models/item/$name.json","$repo/src/main/resources/data/poptartcore/loot_table/blocks/$name.json")) {
        if (!(Test-Path -LiteralPath $path)) { throw "Missing anvil resource: $path" }
        Get-Content -LiteralPath $path -Raw | ConvertFrom-Json -AsHashtable | Out-Null
    }
}
foreach ($model in Get-ChildItem "$assets/models/block/anvils" -Filter '*.json') {
    $json = Get-Content $model.FullName -Raw | ConvertFrom-Json -AsHashtable
    foreach ($texture in $json.textures.Values) {
        if ($texture.StartsWith('poptartcore:') -and !(Test-Path "$assets/textures/$($texture.Substring(12)).png")) { throw "Missing anvil model texture: $texture" }
    }
}
$lang = Get-Content "$assets/lang/en_us.json" -Raw | ConvertFrom-Json -AsHashtable
foreach ($key in @('menu.title.poptartcore.anvil','menu.title.poptartcore.primitive_anvil','menu.message.poptartcore.needs_hammer','item.poptartcore.worn_out')) { if (!$lang[$key]) { throw "Missing language: $key" } }
Push-Location $repo
try {
    & ./gradlew.bat spotlessJavaApply --offline --console=plain
    if ($LASTEXITCODE) { throw 'Formatting failed' }
    & ./gradlew.bat build compileGameTestJava -PgameTests -I scripts/verification/anvil/gametest.gradle --no-configuration-cache --offline --console=plain
    if ($LASTEXITCODE) { throw 'Build or test compilation failed' }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $jar = [IO.Compression.ZipFile]::OpenRead((Get-ModJarPath $repo))
    try {
        foreach ($name in $names) { foreach ($path in @("assets/poptartcore/blockstates/$name.json","assets/poptartcore/models/item/$name.json","data/poptartcore/loot_table/blocks/$name.json")) {
            if (!$jar.GetEntry($path)) { throw "Missing packaged resource: $path" }
        } }
        foreach ($entry in $jar.Entries | Where-Object { $_.FullName -match '^data/[^/]+/recipe/.+\.json$' }) {
            $reader = [IO.StreamReader]::new($entry.Open())
            try { $recipe = $reader.ReadToEnd() } finally { $reader.Dispose() }
            foreach ($name in $names) { if ($recipe.Contains('"poptartcore:' + $name + '"')) { throw "Unexpected anvil recipe: $($entry.FullName)" } }
        }
    } finally { $jar.Dispose() }
    if ($RunServer) {
        & ./gradlew.bat runGameTestServer -PgameTests -I scripts/verification/anvil/gametest.gradle --no-configuration-cache --offline --console=plain
        if ($LASTEXITCODE) { throw 'Anvil GameTests failed' }
    }
    'Anvil models, texture references, language, packaged resources and no-recipe scope verified.'
} finally { Pop-Location }
