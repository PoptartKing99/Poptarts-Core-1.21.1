param()

$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '../..')).Path
$main = Join-Path $projectRoot 'src/main/resources'
$generated = Join-Path $projectRoot 'src/generated/resources'

function Write-JsonFile([string]$relativePath, $value) {
    $path = Join-Path $main $relativePath
    $directory = Split-Path -Parent $path
    New-Item -ItemType Directory -Path $directory -Force | Out-Null
    $json = ConvertTo-Json -InputObject $value -Depth 32
    [System.IO.File]::WriteAllText($path, $json + "`n", [System.Text.UTF8Encoding]::new($false))
}

function Copy-Blockstate([string]$template, [string]$target, [string]$oldName, [string]$newName) {
    $source = Join-Path $generated "assets/poptartcore/blockstates/$template.json"
    $destination = Join-Path $main "assets/poptartcore/blockstates/$target.json"
    $contents = [System.IO.File]::ReadAllText($source).Replace($oldName, $newName)
    $contents = $contents.Replace('poptartcore:block/', 'poptartcore:block/concrete/')
    [System.IO.File]::WriteAllText($destination, $contents, [System.Text.UTF8Encoding]::new($false))
}

Copy-Blockstate 'bronze_bricks' 'concrete' 'bronze_bricks' 'concrete'
Copy-Blockstate 'bronze_bricks' 'concrete_powder' 'bronze_bricks' 'concrete_powder'
Copy-Blockstate 'bronze_brick_slab' 'concrete_slab' 'bronze_brick_slab' 'concrete_slab'
Copy-Blockstate 'bronze_brick_stairs' 'concrete_stairs' 'bronze_brick_stairs' 'concrete_stairs'

foreach ($name in @('concrete', 'concrete_powder')) {
    Write-JsonFile "assets/poptartcore/models/block/concrete/$name.json" ([ordered]@{
        parent = 'minecraft:block/cube_all'
        textures = [ordered]@{ all = "poptartcore:block/$name" }
    })
}

foreach ($entry in @(
    @('concrete_slab', 'minecraft:block/slab'),
    @('concrete_slab_top', 'minecraft:block/slab_top'),
    @('concrete_slab_double', 'minecraft:block/cube_all'),
    @('concrete_stairs', 'minecraft:block/stairs'),
    @('concrete_stairs_inner', 'minecraft:block/inner_stairs'),
    @('concrete_stairs_outer', 'minecraft:block/outer_stairs')
)) {
    $name = $entry[0]
    $parent = $entry[1]
    $textures = if ($name -eq 'concrete_slab_double') {
        [ordered]@{ all = 'poptartcore:block/concrete' }
    } else {
        [ordered]@{
            bottom = 'poptartcore:block/concrete'
            side = 'poptartcore:block/concrete'
            top = 'poptartcore:block/concrete'
        }
    }
    Write-JsonFile "assets/poptartcore/models/block/concrete/$name.json" ([ordered]@{
        parent = $parent
        textures = $textures
    })
}

foreach ($name in @('concrete', 'concrete_powder', 'concrete_slab', 'concrete_stairs')) {
    Write-JsonFile "assets/poptartcore/models/item/$name.json" ([ordered]@{
        parent = "poptartcore:block/concrete/$name"
    })
    $template = if ($name -eq 'concrete_slab') { 'bronze_brick_slab' } else { 'bronze_bricks' }
    $old = if ($name -eq 'concrete_slab') { 'bronze_brick_slab' } else { 'bronze_bricks' }
    $lootSource = Join-Path $generated "data/poptartcore/loot_table/blocks/$template.json"
    $lootTarget = Join-Path $main "data/poptartcore/loot_table/blocks/$name.json"
    New-Item -ItemType Directory -Path (Split-Path -Parent $lootTarget) -Force | Out-Null
    $loot = [System.IO.File]::ReadAllText($lootSource).Replace($old, $name)
    [System.IO.File]::WriteAllText($lootTarget, $loot, [System.Text.UTF8Encoding]::new($false))
}

function Ingredient([string]$item) { return [ordered]@{ item = $item } }
function Shaped([string]$name, [int]$count, [string[]]$pattern, $key) {
    $fileName = $name.Replace(':', '_')
    Write-JsonFile "data/poptartcore/recipe/$fileName.json" ([ordered]@{
        type = 'minecraft:crafting_shaped'
        category = 'building'
        pattern = $pattern
        key = $key
        result = [ordered]@{ id = $name; count = $count }
    })
}

Shaped 'poptartcore:concrete_powder' 8 @('GSG', 'SCS', 'GSG') ([ordered]@{
    G = Ingredient 'minecraft:gravel'
    S = Ingredient 'minecraft:sand'
    C = Ingredient 'minecraft:clay_ball'
})
Shaped 'poptartcore:concrete' 8 @('CCC', 'CWC', 'CCC') ([ordered]@{
    C = Ingredient 'poptartcore:concrete_powder'
    W = Ingredient 'minecraft:water_bucket'
})
Shaped 'poptartcore:concrete_slab' 6 @('CCC') ([ordered]@{
    C = Ingredient 'poptartcore:concrete'
})
Shaped 'poptartcore:concrete_stairs' 4 @('C  ', 'CC ', 'CCC') ([ordered]@{
    C = Ingredient 'poptartcore:concrete'
})

Write-JsonFile 'data/poptartcore/recipe/concrete_splashing.json' ([ordered]@{
    type = 'create:splashing'
    ingredients = @((Ingredient 'poptartcore:concrete_powder'))
    results = @([ordered]@{ id = 'poptartcore:concrete' })
})

$colors = @('white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray',
    'light_gray', 'cyan', 'purple', 'blue', 'brown', 'green', 'red', 'black')
foreach ($color in $colors) {
    Shaped "minecraft:${color}_concrete_powder" 8 @('CCC', 'CDC', 'CCC') ([ordered]@{
        C = Ingredient 'poptartcore:concrete_powder'
        D = Ingredient "minecraft:${color}_dye"
    })
    Shaped "minecraft:${color}_concrete" 8 @('CCC', 'CDC', 'CCC') ([ordered]@{
        C = Ingredient 'poptartcore:concrete'
        D = Ingredient "minecraft:${color}_dye"
    })
    $powder = "minecraft:${color}_concrete_powder"
    Shaped "${color}_concrete_from_powder" 8 @('CCC', 'CWC', 'CCC') ([ordered]@{
        C = Ingredient $powder
        W = Ingredient 'minecraft:water_bucket'
    })
}

# The Catalog generates these tags when runData works. Keep the checked-in generated
# tag data usable in dev environments where unrelated mods prevent runData.
foreach ($entry in @(
    @('pickaxe', @('concrete', 'concrete_slab', 'concrete_stairs')),
    @('shovel', @('concrete_powder'))
)) {
    $path = Join-Path $generated "data/minecraft/tags/block/mineable/$($entry[0]).json"
    $tag = Get-Content -Raw -LiteralPath $path | ConvertFrom-Json
    $values = [System.Collections.Generic.List[string]]::new()
    foreach ($value in $tag.values) { $values.Add($value) }
    foreach ($name in $entry[1]) {
        $value = "poptartcore:$name"
        if (-not $values.Contains($value)) { $values.Add($value) }
    }
    $tag.values = @($values)
    [System.IO.File]::WriteAllText($path, (ConvertTo-Json -InputObject $tag -Depth 10) + "`n", [System.Text.UTF8Encoding]::new($false))
}

Write-Output 'Generated four concrete block assets, loot tables, and 53 recipes.'
