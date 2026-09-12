$ErrorActionPreference = 'Stop'

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$failures = [System.Collections.Generic.List[string]]::new()

function Require-Text([string]$relativePath, [string[]]$patterns) {
    $path = Join-Path $projectRoot $relativePath
    if (-not (Test-Path -LiteralPath $path)) {
        $failures.Add("Missing file: $relativePath")
        return
    }
    $content = Get-Content -LiteralPath $path -Raw
    foreach ($pattern in $patterns) {
        if ($content -notmatch $pattern) {
            $failures.Add("Missing pattern '$pattern' in $relativePath")
        }
    }
}

function Require-File([string]$relativePath) {
    if (-not (Test-Path -LiteralPath (Join-Path $projectRoot $relativePath))) {
        $failures.Add("Missing file: $relativePath")
    }
}

Require-Text 'src/main/java/dev/poptartking/poptartcore/registry/PoptartCoreItems.java' @(
    'REDSTONE_CIRCUIT', 'BONE_PICK', 'FLINT_AXE', 'FLINT_SHOVEL', 'FIRESTARTER', 'REPEATING_CROSSBOW', 'durability\(300\)'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/registry/PoptartCoreTiers.java' @(
    'public static final Tier FLINT', 'public static final Tier BONE', '100', '150'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/tool/BonePickItem.java' @(
    'ItemAbilities\.HOE_TILL', 'DEFAULT_HOE_ACTIONS'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/crossbow/RepeatingCrossbowItem.java' @(
    'return 15;', 'inaccuracy \* 2\.0F', 'REDSTONE_CIRCUIT'
)
Require-Text 'src/main/java/dev/poptartking/poptartcore/client/PoptartCoreItemProperties.java' @(
    'location\("pull"\)', 'location\("pulling"\)', 'location\("charged"\)', 'location\("firework"\)'
)

$itemNames = @('bone_pick', 'flint_axe', 'flint_shovel', 'firestarter', 'redstone_circuit', 'repeating_crossbow')
foreach ($name in $itemNames) {
    Require-File "src/main/resources/assets/poptartcore/models/item/$name.json"
    Require-File "src/main/resources/data/poptartcore/recipe/$name.json"
}
foreach ($name in $itemNames | Where-Object { $_ -ne 'repeating_crossbow' }) {
    Require-File "src/main/resources/assets/poptartcore/textures/item/$name.png"
}

$crossbowTextures = @(
    'repeating_crossbow_1.png', 'repeating_crossbow_2.png', 'repeating_crossbow_3.png',
    'repeating_crossbow_arrow_base.png', 'repeating_crossbow_arrow.png', 'repeating_crossbow_standby.png'
)
foreach ($name in $crossbowTextures) {
    Require-File "src/main/resources/assets/poptartcore/textures/item/crossbows/$name"
}

Require-Text 'src/main/resources/data/minecraft/tags/item/pickaxes.json' @('poptartcore:bone_pick')
Require-Text 'src/main/resources/data/minecraft/tags/item/hoes.json' @('poptartcore:bone_pick')
Require-Text 'src/main/resources/data/minecraft/tags/item/axes.json' @('poptartcore:flint_axe')
Require-Text 'src/main/resources/data/minecraft/tags/item/shovels.json' @('poptartcore:flint_shovel')
Require-Text 'src/main/resources/data/minecraft/tags/item/creeper_igniters.json' @('poptartcore:firestarter')
Require-Text 'src/main/resources/data/minecraft/tags/item/enchantable/crossbow.json' @('poptartcore:repeating_crossbow')

@(
    Get-ChildItem -LiteralPath (Join-Path $projectRoot 'src/main/resources/assets/poptartcore/models/item') -Recurse -Filter '*.json'
    Get-ChildItem -LiteralPath (Join-Path $projectRoot 'src/main/resources/data/poptartcore/recipe') -Filter '*.json'
    Get-ChildItem -LiteralPath (Join-Path $projectRoot 'src/main/resources/data/minecraft/tags/item') -Recurse -Filter '*.json'
) | ForEach-Object {
    $relativePath = $_.FullName.Substring($projectRoot.Length + 1)
    try {
        Get-Content -LiteralPath $_.FullName -Raw | ConvertFrom-Json | Out-Null
    } catch {
        $failures.Add("Invalid JSON: $relativePath")
    }
}

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Output 'Primitive tools, repeating crossbow, redstone circuit, assets, recipes, and tags verified.'
