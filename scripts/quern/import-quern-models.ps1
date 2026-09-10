param(
    [string]$BaseSource = "C:\Users\badas\Downloads\quern_stone_base.json",
    [string]$RotorSource = "C:\Users\badas\Downloads\quern_stone.json",
    [string]$FlourSource = "C:\Users\badas\Downloads\HorsePower-1.12.2-2.6.4.74\assets\horsepower\textures\blocks\flour.png"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$modelOutput = Join-Path $projectRoot "src\main\resources\assets\poptartcore\models\block\quern"
$textureOutput = Join-Path $projectRoot "src\main\resources\assets\poptartcore\textures\block\quern"
$flourDestination = Join-Path $textureOutput "flour.png"
foreach ($source in @($BaseSource, $RotorSource)) {
    if (-not (Test-Path -LiteralPath $source)) {
        throw "Missing Quern source asset: $source"
    }
}

New-Item -ItemType Directory -Force -Path $modelOutput, $textureOutput | Out-Null
$base = Get-Content -Raw -LiteralPath $BaseSource | ConvertFrom-Json
$rotor = Get-Content -Raw -LiteralPath $RotorSource | ConvertFrom-Json
$base.PSObject.Properties.Remove("format_version")
$base.PSObject.Properties.Remove("groups")
$rotor.PSObject.Properties.Remove("format_version")
$rotor.PSObject.Properties.Remove("groups")
$base.parent = "minecraft:block/block"
$rotor.parent = "minecraft:block/block"
$powder = @($base.elements | Where-Object name -eq "Powder")
if ($powder.Count -ne 1 -or $powder[0].from[1] -ne 2.99) {
    throw "Expected exactly one Powder element beginning at Y=2.99"
}

$base.elements = @($base.elements | Where-Object name -ne "Powder")
$base.textures = [ordered]@{
    stone = "minecraft:block/stone"
    wood = "minecraft:block/oak_planks"
    particle = "minecraft:block/oak_planks"
}
$rotor.textures = $base.textures
foreach ($element in @($base.elements)) {
    foreach ($face in $element.faces.PSObject.Properties.Value) {
        if ($face.texture -eq "#2") { $face.texture = "#stone" }
        if ($face.texture -eq "#3") { $face.texture = "#wood" }
    }
}
foreach ($element in @($rotor.elements)) {
    $element.PSObject.Properties.Remove("shade")
    $texture = if ($element.name -eq "quern_stone_handle") { "#wood" } else { "#stone" }
    foreach ($face in $element.faces.PSObject.Properties.Value) {
        $face.texture = $texture
    }
}

$flour = [ordered]@{
    parent = "minecraft:block/block"
    textures = [ordered]@{
        flour = "poptartcore:block/quern/flour"
        particle = "poptartcore:block/quern/flour"
    }
    elements = $powder
}
foreach ($face in $powder[0].faces.PSObject.Properties.Value) {
    $face.texture = "#flour"
    $face | Add-Member -NotePropertyName tintindex -NotePropertyValue 0 -Force
}

$inventory = [ordered]@{
    parent = "minecraft:block/block"
    textures = $base.textures
    elements = @($base.elements) + @($rotor.elements)
}

$base | ConvertTo-Json -Depth 100 | Set-Content -LiteralPath (Join-Path $modelOutput "quern_stone_base.json")
$rotor | ConvertTo-Json -Depth 100 | Set-Content -LiteralPath (Join-Path $modelOutput "quern_stone.json")
$flour | ConvertTo-Json -Depth 100 | Set-Content -LiteralPath (Join-Path $modelOutput "quern_flour.json")
$inventory | ConvertTo-Json -Depth 100 | Set-Content -LiteralPath (Join-Path $modelOutput "quern_inventory.json")
if ((Test-Path -LiteralPath $FlourSource) -and
    (Resolve-Path -LiteralPath $FlourSource).Path -ne (Resolve-Path -LiteralPath $flourDestination).Path) {
    Copy-Item -LiteralPath $FlourSource -Destination $flourDestination -Force
} elseif (-not (Test-Path -LiteralPath $flourDestination)) {
    throw "Missing Quern flour texture: $FlourSource"
}

Write-Host "Imported Quern base, rotor, flour layer, inventory model, and flour texture."
