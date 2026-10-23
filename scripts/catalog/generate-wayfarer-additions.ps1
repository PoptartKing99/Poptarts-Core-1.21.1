# Targeted equivalent of the standard catalog providers for environments where
# Caverns & Chasms datagen cannot load its unpublished Gallery dependency.
# Registrations remain in PoptartCatalog; custom crystal geometry stays in main resources.
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path "$PSScriptRoot/../..").Path
$generated = "$root/src/generated/resources"
$registry = Get-Content -Raw "$root/src/main/java/dev/poptartking/poptartcore/registry/PoptartCoreBlocks.java"
foreach ($id in @('rubber_block', 'quartz_crystal')) {
    if ($registry -notmatch ('PoptartCatalog.block\(\s*"' + $id + '"')) { throw "Missing catalog block $id" }
}
function Write-Resource($path, $value) {
    $target = Join-Path $generated $path
    New-Item -ItemType Directory -Force -Path (Split-Path $target) | Out-Null
    [IO.File]::WriteAllText($target, (($value | ConvertTo-Json -Depth 32) + "`n"), [Text.UTF8Encoding]::new($false))
}
Write-Resource 'assets/poptartcore/blockstates/rubber_block.json' @{variants=@{''=@{model='poptartcore:block/rubber_block'}}}
Write-Resource 'assets/poptartcore/models/block/rubber_block.json' @{parent='minecraft:block/cube_all';textures=@{all='poptartcore:block/rubber_block'}}
Write-Resource 'assets/poptartcore/models/item/rubber_block.json' @{parent='poptartcore:block/rubber_block'}
foreach ($id in @('rubber_block','quartz_crystal')) {
    Write-Resource "data/poptartcore/loot_table/blocks/$id.json" @{
        type='minecraft:block'
        pools=@(@{rolls=1;entries=@(@{type='minecraft:item';name="poptartcore:$id"});conditions=@(@{condition='minecraft:survives_explosion'})})
    }
}
$tagPath = 'data/minecraft/tags/block/mineable/pickaxe.json'
$tag = Get-Content -Raw "$generated/$tagPath" | ConvertFrom-Json -AsHashtable
if ($tag.values -notcontains 'poptartcore:quartz_crystal') { $tag.values += 'poptartcore:quartz_crystal' }
Write-Resource $tagPath $tag
$langPath = 'assets/poptart_catalog/lang/en_us.json'
$lang = Get-Content -Raw "$generated/$langPath" | ConvertFrom-Json -AsHashtable
$lang.Remove('item.poptartcore.quartz_crystal')
$lang['block.poptartcore.quartz_crystal'] = 'Quartz Crystal'
$lang['block.poptartcore.rubber_block'] = 'Block of Rubber'
$lang['item.poptartcore.magnet_ring'] = 'Magnet Ring'
Write-Resource $langPath $lang
Write-Output 'Generated standard resources for the three catalog additions. No recipes exist in the reference JAR.'
