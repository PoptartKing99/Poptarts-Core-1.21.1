package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.blastfurnace.BlastFurnaceBlock;
import dev.poptartking.poptartcore.bloomery.BloomeryBlock;
import dev.poptartking.poptartcore.bloomery.IronBloomBlock;
import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import dev.poptartking.poptartcore.clinker.ClinkerPillarBlock;
import dev.poptartking.poptartcore.crucible.CrucibleBlock;
import dev.poptartking.poptartcore.crucible.CrucibleBlockItem;
import dev.poptartking.poptartcore.millstone.MillstoneBlock;
import dev.poptartking.poptartcore.millstone.MillstoneBlockItem;
import dev.poptartking.poptartcore.millstone.MillstoneRotorBlock;
import dev.poptartking.poptartcore.millstone.MillstoneStructuralBlock;
import dev.poptartking.poptartcore.quern.QuernBlock;
import dev.poptartking.poptartcore.rift.PackedRiftSedimentBlock;
import dev.poptartking.poptartcore.rift.RiftFireBlock;
import dev.poptartking.poptartcore.rift.RiftSedimentBlock;
import dev.poptartking.poptartcore.spider.TemporaryCobwebBlock;
import dev.poptartking.poptartcore.workbench.WorkbenchBlock;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PoptartCoreBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PoptartCore.MOD_ID);

    public static final DeferredBlock<TemporaryCobwebBlock> TEMPORARY_COBWEB = BLOCKS.register(
            "temporary_cobweb",
            () -> new TemporaryCobwebBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.COBWEB).noLootTable()));
    public static final DeferredBlock<RiftFireBlock> RIFT_FIRE = BLOCKS.register(
            "rift_fire",
            () -> new RiftFireBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.FIRE).noLootTable()));

    public static final CatalogBlockDefinition<MillstoneBlock> MILLSTONE = PoptartCatalog.block(
                    "millstone", () -> new MillstoneBlock(millstoneProperties()), MillstoneBlockItem::new)
            .externalModel()
            .mineableWithPickaxe();
    public static final DeferredBlock<MillstoneStructuralBlock> MILLSTONE_STRUCTURAL = BLOCKS.register(
            "millstone_structural",
            () -> new MillstoneStructuralBlock(millstoneProperties().noLootTable()));
    public static final DeferredBlock<MillstoneRotorBlock> MILLSTONE_ROTOR = BLOCKS.register(
            "millstone_rotor",
            () -> new MillstoneRotorBlock(millstoneProperties().noOcclusion().noLootTable()));

    private static BlockBehaviour.Properties millstoneProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(3.0F)
                .pushReaction(PushReaction.BLOCK);
    }

    public static final CatalogBlockDefinition<CrucibleBlock> CRUCIBLE = PoptartCatalog.block(
                    "crucible",
                    () -> new CrucibleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAMPFIRE)
                            .sound(SoundType.MUD_BRICKS)
                            .lightLevel(state -> {
                                if (state.getValue(CrucibleBlock.LIT)) {
                                    return 15;
                                }
                                return state.getValue(CrucibleBlock.FLUID_LEVEL) > 0 ? 8 : 0;
                            })),
                    CrucibleBlockItem::new)
            .externalModel()
            .mineableWithAxe()
            .mineableWithPickaxe();

    public static final CatalogBlockDefinition<BlastFurnaceBlock> BLAST_FURNACE = PoptartCatalog.block(
                    "blast_furnace",
                    () -> new BlastFurnaceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BLAST_FURNACE)
                            .lightLevel(state -> state.getValue(BlastFurnaceBlock.LIT) ? 13 : 0)))
            .externalModel()
            .mineableWithPickaxe();

    public static final CatalogBlockDefinition<BloomeryBlock> BLOOMERY = PoptartCatalog.block(
                    "bloomery", () -> new BloomeryBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MUD_BRICKS)))
            .externalModel()
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<WorkbenchBlock> WORKBENCH = PoptartCatalog.block(
                    "workbench", () -> new WorkbenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)))
            .externalModel()
            .mineableWithAxe();
    public static final CatalogBlockDefinition<QuernBlock> QUERN = PoptartCatalog.block(
                    "quern",
                    () -> new QuernBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONECUTTER)
                            .noOcclusion()))
            .externalModel()
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<PortableEngineBlock> PORTABLE_ENGINE = PoptartCatalog.block(
                    "portable_engine",
                    () -> new PortableEngineBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.TERRACOTTA_WHITE)
                                    .requiresCorrectToolForDrops()
                                    .strength(3.0F, 6.0F)
                                    .sound(SoundType.COPPER)
                                    .lightLevel(state -> PortableEngineBlock.isLitState(state) ? 6 : 0)
                                    .noOcclusion(),
                            null))
            .externalModel()
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<IronBloomBlock> IRON_BLOOM = PoptartCatalog.block(
                    "iron_bloom",
                    () -> new IronBloomBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)
                            .noOcclusion()
                            .noLootTable()))
            .externalModel()
            .withoutGeneratedLoot()
            .mineableWithPickaxe();

    public static final CatalogBlockDefinition<RiftSedimentBlock> RIFT_SEDIMENT = PoptartCatalog.block(
                    "rift_sediment",
                    () -> new RiftSedimentBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .noCollission()
                            .instabreak()
                            .sound(SoundType.SAND)
                            .lightLevel(state -> state.getValue(RiftSedimentBlock.LIT) ? 7 : 0)
                            .pushReaction(PushReaction.DESTROY)))
            .externalModel()
            .withoutGeneratedLoot();
    public static final CatalogBlockDefinition<PackedRiftSedimentBlock> RIFT_SEDIMENT_BLOCK = PoptartCatalog.block(
                    "rift_sediment_block",
                    () -> new PackedRiftSedimentBlock(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).mapColor(MapColor.COLOR_PURPLE)))
            .withName("Block of Rift Sediment")
            .withStorageRecipes(PoptartCoreBlocks.RIFT_SEDIMENT::get, "rift_sediment_from_block")
            .simpleModel(PoptartCore.location("block/rift_sediment_block"), "")
            .tags(BlockTags.MINEABLE_WITH_SHOVEL);

    public static final CatalogBlockDefinition<Block> TIN_BLOCK = PoptartCatalog.block(
                    "tin_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(1.0F, 6.0F)))
            .withName("Block of Tin")
            .withStorageRecipes(() -> PoptartCoreItems.TIN_INGOT.get(), "tin_ingots_from_block")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<Block> TIN_ORE = PoptartCatalog.block(
                    "tin_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_ORE)
                            .strength(2.0F, 6.0F)))
            .withOreDrop(() -> PoptartCoreItems.RAW_TIN.get())
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<Block> DEEPSLATE_TIN_ORE = PoptartCatalog.block(
                    "deepslate_tin_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_COPPER_ORE)
                            .strength(3.0F, 6.0F)))
            .withOreDrop(() -> PoptartCoreItems.RAW_TIN.get())
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<Block> RAW_TIN_BLOCK = PoptartCatalog.block(
                    "raw_tin_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_COPPER_BLOCK)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Raw Tin")
            .withStorageRecipes(() -> PoptartCoreItems.RAW_TIN.get(), "raw_tin_from_block")
            .mineableWithPickaxe();

    public static final CatalogBlockDefinition<Block> LEAD_BLOCK = PoptartCatalog.block(
                    "lead_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(1.0F, 6.0F)))
            .withName("Block of Lead")
            .withStorageRecipes(() -> PoptartCoreItems.LEAD_INGOT.get(), "lead_ingots_from_block")
            .requiresIronTool();
    public static final CatalogBlockDefinition<Block> LEAD_ORE = PoptartCatalog.block(
                    "lead_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .strength(2.0F, 6.0F)))
            .withOreDrop(() -> PoptartCoreItems.RAW_LEAD.get())
            .requiresIronTool();
    public static final CatalogBlockDefinition<Block> DEEPSLATE_LEAD_ORE = PoptartCatalog.block(
                    "deepslate_lead_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                            .sound(SoundType.DEEPSLATE)
                            .requiresCorrectToolForDrops()
                            .strength(3.0F, 6.0F)))
            .withOreDrop(() -> PoptartCoreItems.RAW_LEAD.get())
            .requiresIronTool();
    public static final CatalogBlockDefinition<Block> RAW_LEAD_BLOCK = PoptartCatalog.block(
                    "raw_lead_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Raw Lead")
            .withStorageRecipes(() -> PoptartCoreItems.RAW_LEAD.get(), "raw_lead_from_block")
            .requiresIronTool();

    public static final CatalogBlockDefinition<Block> SILVER_BLOCK = PoptartCatalog.block(
                    "silver_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Silver")
            .withStorageRecipes(() -> PoptartCoreItems.SILVER_INGOT.get(), "silver_ingots_from_block")
            .requiresIronTool();
    public static final CatalogBlockDefinition<Block> SILVER_ORE = PoptartCatalog.block(
                    "silver_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .strength(5.0F, 6.0F)))
            .withOreDrop(() -> PoptartCoreItems.RAW_SILVER.get())
            .requiresIronTool();
    public static final CatalogBlockDefinition<Block> DEEPSLATE_SILVER_ORE = PoptartCatalog.block(
                    "deepslate_silver_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)
                            .sound(SoundType.DEEPSLATE)
                            .requiresCorrectToolForDrops()
                            .strength(6.0F, 6.0F)))
            .withOreDrop(() -> PoptartCoreItems.RAW_SILVER.get())
            .requiresIronTool();
    public static final CatalogBlockDefinition<Block> RAW_SILVER_BLOCK = PoptartCatalog.block(
                    "raw_silver_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Raw Silver")
            .withStorageRecipes(() -> PoptartCoreItems.RAW_SILVER.get(), "raw_silver_from_block")
            .requiresIronTool();

    public static final CatalogBlockDefinition<Block> WAX_BLOCK = PoptartCatalog.block(
                    "wax_block",
                    () -> new Block(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.MUD).strength(0.7F, 0.3F)))
            .withStorageRecipes(() -> PoptartCoreItems.WAX.get(), "wax_from_block");
    public static final CatalogBlockDefinition<Block> COAL_COKE_BLOCK = PoptartCatalog.block(
                    "coal_coke_block", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_BLOCK)))
            .withStorageRecipes(() -> PoptartCoreItems.COAL_COKE.get(), "coal_coke_from_block")
            .fuelBurnTime(28800)
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<Block> BRONZE_BLOCK = PoptartCatalog.block(
                    "bronze_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(3.0F, 6.0F)))
            .withName("Block of Bronze")
            .withStorageRecipes(() -> PoptartCoreItems.BRONZE_INGOT.get(), "bronze_ingot_from_block")
            .requiresStoneTool();
    public static final CatalogBlockDefinition<Block> STEEL_BLOCK = PoptartCatalog.block(
                    "steel_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(7.0F, 8.0F)))
            .withName("Block of Steel")
            .withStorageRecipes(() -> PoptartCoreItems.STEEL_INGOT.get(), "steel_ingot_from_block")
            .requiresIronTool();

    public static final CatalogBlockDefinition<Block> CLINKER_BRICKS = PoptartCatalog.block(
                    "clinker_bricks", () -> new Block(clinkerProperties()))
            .randomCubeModel(PoptartCore.location("block/clinker/clinker_bricks"), "clinker", 12)
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<SlabBlock> CLINKER_BRICK_SLAB = PoptartCatalog.block(
                    "clinker_brick_slab", () -> new SlabBlock(clinkerProperties()))
            .slabModel(PoptartCore.location("block/clinker/clinker_bricks_1"), "clinker")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<StairBlock> CLINKER_BRICK_STAIRS = PoptartCatalog.block(
                    "clinker_brick_stairs",
                    () -> new StairBlock(CLINKER_BRICKS.get().defaultBlockState(), clinkerProperties()))
            .stairsModel(PoptartCore.location("block/clinker/clinker_bricks_1"), "clinker")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<WallBlock> CLINKER_BRICK_WALL = PoptartCatalog.block(
                    "clinker_brick_wall", () -> new WallBlock(clinkerProperties()))
            .wallModel(PoptartCore.location("block/clinker/clinker_bricks_1"), "clinker")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS);
    public static final CatalogBlockDefinition<Block> CLINKER_TILE = PoptartCatalog.block(
                    "clinker_tile", () -> new Block(clinkerProperties()))
            .simpleModel(PoptartCore.location("block/clinker/clinker_tile"), "clinker")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<SlabBlock> CLINKER_TILE_SLAB = PoptartCatalog.block(
                    "clinker_tile_slab", () -> new SlabBlock(clinkerProperties()))
            .slabModel(PoptartCore.location("block/clinker/clinker_tile"), "clinker")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<StairBlock> CLINKER_TILE_STAIRS = PoptartCatalog.block(
                    "clinker_tile_stairs",
                    () -> new StairBlock(CLINKER_TILE.get().defaultBlockState(), clinkerProperties()))
            .stairsModel(PoptartCore.location("block/clinker/clinker_tile"), "clinker")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<WallBlock> CLINKER_TILE_WALL = PoptartCatalog.block(
                    "clinker_tile_wall", () -> new WallBlock(clinkerProperties()))
            .wallModel(PoptartCore.location("block/clinker/clinker_tile"), "clinker")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS);
    public static final CatalogBlockDefinition<Block> MOSAIC_CLINKER_TILE = PoptartCatalog.block(
                    "mosaic_clinker_tile", () -> new Block(clinkerProperties()))
            .randomBottomTopModel(
                    PoptartCore.location("block/clinker/mosaic_clinker_tile"),
                    PoptartCore.location("block/clinker/mosaic_clinker_tile_top"),
                    PoptartCore.location("block/clinker/mosaic_clinker_tile_bottom"),
                    "clinker",
                    4)
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<Block> CHISELED_CLINKER_TILE = PoptartCatalog.block(
                    "chiseled_clinker_tile", () -> new Block(clinkerProperties()))
            .simpleModel(PoptartCore.location("block/clinker/chiseled_clinker_tile"), "clinker")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<ClinkerPillarBlock> CLINKER_PILLAR = PoptartCatalog.block(
                    "clinker_pillar", () -> new ClinkerPillarBlock(clinkerProperties()))
            .externalModel()
            .mineableWithPickaxe();

    private static BlockBehaviour.Properties clinkerProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
