# Shared by feature checks: a resource has one owner, handwritten or generated.
function Get-ModJarPath([string] $ProjectRoot, [string] $JarPath) {
    if ($JarPath) {
        if ([IO.Path]::IsPathRooted($JarPath)) { return [IO.Path]::GetFullPath($JarPath) }
        return [IO.Path]::GetFullPath((Join-Path $ProjectRoot $JarPath))
    }
    $properties = Get-Content -LiteralPath (Join-Path $ProjectRoot 'gradle.properties') -Raw | ConvertFrom-StringData
    return Join-Path $ProjectRoot "build/libs/$($properties.mod_id)-$($properties.mod_version).jar"
}

function Resolve-ModFile([string] $Path) {
    $full = [IO.Path]::GetFullPath($Path)
    if (Test-Path -LiteralPath $full -PathType Leaf) { return $full }
    $alternate = $full -replace '([\\/])src[\\/]main[\\/]resources([\\/])', '$1src/generated/resources$2'
    if ($alternate -eq $full) {
        $alternate = $full -replace '([\\/])src[\\/]generated[\\/]resources([\\/])', '$1src/main/resources$2'
    }
    if ($alternate -ne $full -and (Test-Path -LiteralPath $alternate -PathType Leaf)) { return $alternate }
    return $full
}

function Read-ModLanguage([string] $ProjectRoot, [switch] $AsHashtable) {
    $language = @{}
    foreach ($relative in @('src/main/resources/assets/poptartcore/lang/en_us.json',
                           'src/generated/resources/assets/poptart_catalog/lang/en_us.json')) {
        $values = Get-Content -LiteralPath (Join-Path $ProjectRoot $relative) -Raw | ConvertFrom-Json -AsHashtable
        foreach ($key in $values.Keys) { $language[$key] = $values[$key] }
    }
    if ($AsHashtable) { return $language }
    return [PSCustomObject]$language
}
