$ErrorActionPreference = 'Stop'
$organizationRoot = (Resolve-Path "$PSScriptRoot/../..").Path
$registryRoot = Join-Path $organizationRoot 'src/main/java/dev/poptartking/poptartcore/registry'
$baseline = Get-Content -Raw "$PSScriptRoot/item-registration-baseline.json" | ConvertFrom-Json
$actual = @{}
foreach ($group in ($baseline.group | Select-Object -Unique)) {
    $groupSource = Get-Content -Raw (Join-Path $registryRoot "$group.java")
    foreach ($match in [regex]::Matches($groupSource, '(?s)public static final\s+.*?\s+(?<name>\w+)\s*=.*?;')) {
        $name = $match.Groups['name'].Value
        if ($actual.ContainsKey($name)) { throw "Duplicate registration field: $name" }
        $canonical = $match.Value -replace '\s+', '' -replace 'Poptart(?:Ring|Cape|Tool|Armor|Material|Block|Processing)Items\.', 'PoptartCoreItems.'
        $actual[$name] = @{ Group = $group; Declaration = $canonical }
    }
}
if ($actual.Count -ne $baseline.Count) { throw 'Registration field count changed' }
foreach ($entry in $baseline) {
    if (!$actual.ContainsKey($entry.field) -or $actual[$entry.field].Group -ne $entry.group -or $actual[$entry.field].Declaration -ne $entry.declaration) {
        throw "Registration settings changed: $($entry.field)"
    }
}
$bootstrap = Get-Content -Raw (Join-Path $registryRoot 'PoptartCoreItems.java')
if ($bootstrap -match 'public static final') { throw 'Legacy registration fields remain in bootstrap' }
$language = Get-Content -Raw "$organizationRoot/src/main/resources/assets/poptartcore/lang/en_us.json" | ConvertFrom-Json -AsHashtable
foreach ($tab in @('poptartcore_tab')) {
    if (!$language["creativetab.poptartcore.$tab"]) { throw "Missing creative tab title: $tab" }
}
Write-Output "Verified all $($baseline.Count) moved declarations retain their original registration settings. Single creative tab title present."
