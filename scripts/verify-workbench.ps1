$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$sourceRoot = "C:\Users\badas\IdeaProjects\intellij_wayfarer\decompiled"
$resourceRoot = Join-Path $projectRoot "src/main/resources"
Add-Type -AssemblyName System.Web.Extensions
$jsonParser = New-Object System.Web.Script.Serialization.JavaScriptSerializer

function Assert-Contains([string] $Text, [string] $Expected, [string] $Message) {
    if (-not $Text.Contains($Expected)) {
        throw $Message
    }
}

function Assert-File([string] $Path) {
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "Missing workbench file: $Path"
    }
}

$blockEntity = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/workbench/WorkbenchBlockEntity.java")
$menu = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/workbench/menu/WorkbenchMenu.java")
$craftingContainer = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/workbench/menu/WorkbenchCraftingContainer.java")
$blocks = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/registry/PoptartCoreBlocks.java")
$items = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/registry/PoptartCoreItems.java")
$blockEntities = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/registry/PoptartCoreBlockEntities.java")
$menus = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/registry/PoptartCoreMenus.java")
$clientEvents = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/client/ClientEvents.java")
$tabs = Get-Content -Raw (Join-Path $projectRoot "src/main/java/dev/poptartking/poptartcore/registry/PoptartCoreTabs.java")

Assert-Contains $blockEntity "GRID_SIZE = 9" "The workbench must store a 3x3 crafting grid."
Assert-Contains $blockEntity "STORAGE_SIZE = 16" "The workbench must provide sixteen storage slots."
Assert-Contains $blockEntity "ContainerHelper.saveAllItems" "Workbench contents are not persisted."
Assert-Contains $blockEntity "ContainerHelper.loadAllItems" "Workbench contents are not restored."
Assert-Contains $menu "new ResultSlot" "The workbench has no crafting output slot."
Assert-Contains $menu "RecipeType.CRAFTING" "The workbench does not use vanilla crafting recipes."
Assert-Contains $craftingContainer "menu.slotsChanged(this)" "Changing the crafting grid does not refresh its result."
Assert-Contains $blocks '"workbench"' "The workbench block is not registered."
Assert-Contains $items 'registerBlockItem("workbench"' "The workbench item is not registered."
Assert-Contains $blockEntities 'BLOCK_ENTITIES.register("workbench"' "The workbench block entity is not registered."
Assert-Contains $menus 'MENUS.register("workbench"' "The workbench menu is not registered."
Assert-Contains $clientEvents "PoptartCoreMenus.WORKBENCH" "The workbench screen is not registered."
Assert-Contains $tabs "PoptartCoreItems.WORKBENCH" "The workbench is missing from the creative tab."

$jsonFiles = @(
    "assets/poptartcore/blockstates/workbench.json",
    "assets/poptartcore/models/block/workbench.json",
    "assets/poptartcore/models/item/workbench.json",
    "assets/poptartcore/lang/en_us.json",
    "data/poptartcore/loot_table/blocks/workbench.json",
    "data/minecraft/tags/block/mineable/axe.json"
)
foreach ($relativePath in $jsonFiles) {
    $path = Join-Path $resourceRoot $relativePath
    Assert-File $path
    $jsonParser.DeserializeObject((Get-Content -Raw $path)) | Out-Null
}

$textureFiles = @(
    "block/workbench_bottom.png",
    "block/workbench_side_1.png",
    "block/workbench_side_2.png",
    "block/workbench_top.png",
    "gui/workbench.png"
)
foreach ($relativePath in $textureFiles) {
    $source = Join-Path $sourceRoot "assets/wayfarer_core/textures/$relativePath"
    $target = Join-Path $resourceRoot "assets/poptartcore/textures/$relativePath"
    Assert-File $target
    if ((Get-FileHash -LiteralPath $source).Hash -ne (Get-FileHash -LiteralPath $target).Hash) {
        throw "Copied workbench texture differs from Wayfarer: $relativePath"
    }
}

Write-Host "Workbench verification passed."
