$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$javaRoot = Join-Path $projectRoot 'src/main/java/dev/poptartking/poptartcore'

# These are source regression guards, not Minecraft integration tests.
function Assert-SourceContains($source, $fragment, $message) {
    if (-not $source.Contains($fragment)) { throw $message }
}

$shared = Get-Content -Raw -LiteralPath (Join-Path $javaRoot 'crucible/CrucibleBlockEntity.java')
$block = Get-Content -Raw -LiteralPath (Join-Path $javaRoot 'blastfurnace/BlastFurnaceBlock.java')
$container = Get-Content -Raw -LiteralPath (Join-Path $javaRoot 'blastfurnace/BlastFurnaceBlockEntity.java')
Assert-SourceContains $shared 'tag.putInt("CookingBatches", cookingBatches)' 'Active batch size must be saved.'
Assert-SourceContains $shared 'tag.getInt("CookingBatches")' 'Active batch size must be loaded.'
Assert-SourceContains $shared 'meltingBatches >= activeBatches ? activeBatches : 0' 'Melting must use the fixed batch only when enough inputs and space remain.'
Assert-SourceContains $shared 'while (batches >= Math.max(1, requiredBatches))' 'Alloy planning must not silently shrink an active batch.'
Assert-SourceContains $shared 'performMelting(meltingRecipe, meltingBatches)' 'Melting completion must consume the planned batch.'
Assert-SourceContains $block 'UPDATE_ALL | UPDATE_KNOWN_SHAPE' 'Removing the lower half must not recursively remove the selected upper half.'
Assert-SourceContains $block 'return super.onDestroyedByPlayer' 'Player removal must still report success through the normal destruction path.'
Assert-SourceContains $block 'super.playerDestroy(' 'Loot must remain in the correct-tool harvest callback.'
Assert-SourceContains $container 'super.canPlaceItem(slot, stack)' 'Keep shared melting, mould, and crucible-alloy input validation.'
Assert-SourceContains $container 'BLAST_FURNACE_ALLOYING_TYPE.get()' 'Accept blast-furnace-specific alloy ingredients.'
Write-Host 'Blast furnace source guards passed. In-game behavior still needs testing.'

Push-Location $projectRoot
try {
    & ./gradlew.bat build
    if ($LASTEXITCODE -ne 0) { throw 'Gradle build failed.' }
} finally {
    Pop-Location
}

Write-Host 'In game: add ingredients and free output space during a batch; check progress and output count.'
Write-Host 'Save/reload during a batch; check that its size and progress survive.'
Write-Host 'Break each half with a correct pickaxe, by hand, and in Creative; check block and inventory drops.'
Write-Host 'Try valid ingredients and dirt through hoppers/Create on both halves; check the existing side layout.'
