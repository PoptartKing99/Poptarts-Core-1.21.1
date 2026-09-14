$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..')).Path
$dataRoot = Join-Path $projectRoot 'src\main\resources\data\poptartcore'

$expectedOres = @{
    'tin' = @{ Size = 12; Count = 5; MinY = 65; MaxY = 240; Rarity = $null }
    'lead' = @{ Size = 8; Count = 1; MinY = -40; MaxY = 20; Rarity = $null }
    'silver' = @{ Size = 4; Count = 1; MinY = -50; MaxY = -30; Rarity = 2 }
}

foreach ($entry in $expectedOres.GetEnumerator()) {
    $metal = $entry.Key
    $expected = $entry.Value
    $configuredPath = Join-Path $dataRoot "worldgen\configured_feature\ore_$metal.json"
    $placedPath = Join-Path $dataRoot "worldgen\placed_feature\ore_$metal.json"
    $configured = Get-Content -Raw -LiteralPath $configuredPath | ConvertFrom-Json
    $placed = Get-Content -Raw -LiteralPath $placedPath | ConvertFrom-Json

    if ($configured.type -ne 'minecraft:ore' -or $configured.config.size -ne $expected.Size) {
        throw "Wrong configured feature type or vein size for $metal"
    }
    $expectedStates = @("poptartcore:${metal}_ore", "poptartcore:deepslate_${metal}_ore") | Sort-Object
    $actualStates = @($configured.config.targets.state.Name) | Sort-Object
    if (($expectedStates -join "`n") -ne ($actualStates -join "`n")) {
        throw "Wrong stone or deepslate ore targets for $metal"
    }
    if ($placed.feature -ne "poptartcore:ore_$metal") {
        throw "Wrong configured-feature reference for $metal"
    }

    $count = $placed.placement | Where-Object type -eq 'minecraft:count'
    $height = $placed.placement | Where-Object type -eq 'minecraft:height_range'
    $rarity = $placed.placement | Where-Object type -eq 'minecraft:rarity_filter'
    if ($count.count -ne $expected.Count -or
        $height.height.type -ne 'minecraft:uniform' -or
        $height.height.min_inclusive.absolute -ne $expected.MinY -or
        $height.height.max_inclusive.absolute -ne $expected.MaxY) {
        throw "Wrong placement count or height range for $metal"
    }
    if (($null -eq $expected.Rarity -and $null -ne $rarity) -or
        ($null -ne $expected.Rarity -and $rarity.chance -ne $expected.Rarity)) {
        throw "Wrong rarity filter for $metal"
    }
    foreach ($requiredModifier in @('minecraft:in_square', 'minecraft:biome')) {
        if ($placed.placement.type -notcontains $requiredModifier) {
            throw "Missing $requiredModifier placement modifier for $metal"
        }
    }
}

$biomeModifier = Get-Content -Raw -LiteralPath (Join-Path $dataRoot 'neoforge\biome_modifier\add_ores.json') | ConvertFrom-Json
$expectedFeatures = @($expectedOres.Keys | ForEach-Object { "poptartcore:ore_$_" } | Sort-Object)
$actualFeatures = @($biomeModifier.features | Sort-Object)
if ($biomeModifier.type -ne 'neoforge:add_features' -or
    $biomeModifier.biomes -ne '#c:is_overworld' -or
    $biomeModifier.step -ne 'underground_ores' -or
    ($expectedFeatures -join "`n") -ne ($actualFeatures -join "`n")) {
    throw 'The ore biome modifier does not add all three ores to Overworld underground generation'
}

Write-Output "Verified $($expectedOres.Count) Overworld ore-generation definitions."
