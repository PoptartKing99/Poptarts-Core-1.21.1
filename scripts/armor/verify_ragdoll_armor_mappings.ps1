$ErrorActionPreference = 'Stop'

$project = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$armorDirectory = Join-Path $project 'src/main/java/dev/poptartking/poptartcore/armor/client'
$compatPath = Join-Path $project 'src/main/java/dev/poptartking/poptartcore/integration/ragdoll/RagdollArmorCompat.java'
$transformPath = Join-Path $project 'src/main/java/dev/poptartking/poptartcore/mixin/integration/ragdoll/ArmorPartTransformMixin.java'
$models = @(
    'MetalArmorModels.java',
    'LeatherArmorModels.java',
    'BeekeeperArmorModel.java',
    'RawHideArmorModel.java',
    'MiningHelmetModel.java'
)
$canonical = @('head', 'body', 'left_arm', 'right_arm', 'left_leg', 'right_leg')
$mappings = @{}
$used = @{}

$compatSource = Get-Content -LiteralPath $compatPath -Raw
foreach ($match in [regex]::Matches($compatSource, 'map\("([^\"]+)",\s*"([^\"]+)"\)')) {
    $part = $match.Groups[1].Value
    $limb = $match.Groups[2].Value
    if ($mappings.ContainsKey($part)) { throw "Duplicate Ragdoll mapping: $part" }
    if ($limb -notin $canonical) { throw "Invalid target for ${part}: $limb" }
    $mappings[$part] = $limb
}

foreach ($model in $models) {
    $source = Get-Content -LiteralPath (Join-Path $armorDirectory $model) -Raw
    foreach ($match in [regex]::Matches($source, 'addOrReplaceChild\(\s*"([^\"]+)"')) {
        $part = $match.Groups[1].Value
        if ($part -in $canonical) { continue }
        if (-not $mappings.ContainsKey($part)) { throw "Unmapped armor part in ${model}: $part" }
        $used[$part] = $true
    }
}

foreach ($part in $mappings.Keys) {
    if (-not $used.ContainsKey($part)) { throw "Stale Ragdoll mapping: $part" }
}

$transformSource = Get-Content -LiteralPath $transformPath -Raw
$overrideList = [regex]::Match(
    $transformSource,
    'POPTARTCORE\$PARTS_NEEDING_PLAYER_SCALE\s*=\s*Set\.of\((.*?)\);',
    [System.Text.RegularExpressions.RegexOptions]::Singleline)
if (-not $overrideList.Success) { throw 'Could not find Ragdoll armor transform overrides.' }
$overrideCount = 0
foreach ($match in [regex]::Matches($overrideList.Groups[1].Value, '"([^\"]+)"')) {
    $part = $match.Groups[1].Value
    if (-not $mappings.ContainsKey($part)) { throw "Transform override without mapping: $part" }
    $overrideCount++
}

Write-Output "Verified $($mappings.Count) Ragdoll armor part mappings across $($models.Count) model files and $overrideCount transform overrides."
