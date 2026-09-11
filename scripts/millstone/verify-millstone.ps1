param([switch]$RunServer)
$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$resources = Join-Path $project 'src/main/resources'
$assets = Join-Path $resources 'assets/poptartcore'

function Assert-Resource($relative) {
    if (-not (Test-Path -LiteralPath (Join-Path $assets $relative))) { throw "Missing resource: $relative" }
}

$models = Get-ChildItem -LiteralPath "$assets/models/block/millstone" -Filter '*.json'
foreach ($file in $models) {
    $model = Get-Content -Raw -LiteralPath $file.FullName | ConvertFrom-Json -AsHashtable
    foreach ($texture in $model.textures.Values) {
        if ($texture.StartsWith('poptartcore:')) {
            Assert-Resource ("textures/" + $texture.Substring('poptartcore:'.Length) + '.png')
        }
    }
    if ($model.parent -and $model.parent.StartsWith('poptartcore:')) {
        Assert-Resource ("models/" + $model.parent.Substring('poptartcore:'.Length) + '.json')
    }
    if ((Get-Content -Raw -LiteralPath $file.FullName).Contains('wayfarer_core:')) {
        throw "Unconverted Wayfarer reference in $($file.Name)"
    }
}
foreach ($name in @('millstone', 'millstone_structural', 'millstone_rotor')) {
    $json = Get-Content -Raw -LiteralPath "$assets/blockstates/$name.json"
    $null = $json | ConvertFrom-Json -AsHashtable
    foreach ($match in [regex]::Matches($json, '"model"\s*:\s*"poptartcore:([^"]+)"')) {
        Assert-Resource ("models/" + $match.Groups[1].Value + '.json')
    }
}
Assert-Resource 'models/item/millstone.json'
foreach ($sound in @('loop', 'use1', 'use2', 'use3')) { Assert-Resource "sounds/millstone/$sound.ogg" }
$recipes = @(Get-ChildItem -LiteralPath "$resources/data/poptartcore/recipe/milling" -Filter '*.json')
if ($recipes.Count -ne 1) { throw 'Expected only the agreed wheat recipe' }
$recipe = Get-Content -Raw -LiteralPath $recipes[0].FullName | ConvertFrom-Json
if ($recipe.type -ne 'poptartcore:milling' -or $recipe.ingredient.item -ne 'minecraft:wheat' -or
    $recipe.result.id -ne 'create:wheat_flour' -or $recipe.result.count -ne 1 -or $recipe.duration -ne 20) {
    throw 'Unexpected wheat milling recipe'
}
Write-Host "Millstone models, blockstates, textures, sounds and wheat recipe validated."

if ($RunServer) {
    $mods = Join-Path $project 'build/millstone-test-run/mods'
    New-Item -ItemType Directory -Force -Path $mods | Out-Null
    $properties = Get-Content -LiteralPath "$project/gradle.properties" -Raw | ConvertFrom-StringData
    $required = @("create-$($properties.minecraft_version)-$($properties.create_version).jar",
        "supplementaries-$($properties.supplementaries_version)-neoforge.jar")
    foreach ($name in $required) {
        Copy-Item -LiteralPath "$project/run/mods/$name" -Destination "$mods/$name"
    }
    $moonlight = @(Get-ChildItem -LiteralPath "$project/run/mods" -Filter 'moonlight-*.jar')
    if ($moonlight.Count -ne 1) { throw 'Expected one Moonlight dependency in run/mods' }
    Copy-Item -LiteralPath $moonlight[0].FullName -Destination "$mods/$($moonlight[0].Name)"
    Push-Location $project
    try {
        & .\gradlew.bat runGameTestServer -PmillstoneTests --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'Millstone server tests failed' }
    } finally { Pop-Location }
}
