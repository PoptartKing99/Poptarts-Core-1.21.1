package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.blastfurnace.BlastFurnaceBlock;
import dev.poptartking.poptartcore.bloomery.BloomeryBlock;
import dev.poptartking.poptartcore.bloomery.IronBloomBlock;
import dev.poptartking.poptartcore.clinker.ClinkerPillarBlock;
import dev.poptartking.poptartcore.crucible.CrucibleBlock;
import dev.poptartking.poptartcore.millstone.MillstoneBlock;
import dev.poptartking.poptartcore.millstone.MillstoneRotorBlock;
import dev.poptartking.poptartcore.millstone.MillstoneStructuralBlock;
import dev.poptartking.poptartcore.quern.QuernBlock;
import dev.poptartking.poptartcore.workbench.WorkbenchBlock;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlock;
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

    public static final DeferredBlock<MillstoneBlock> MILLSTONE =
            BLOCKS.register("millstone", () -> new MillstoneBlock(millstoneProperties()));
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

    public static final DeferredBlock<CrucibleBlock> CRUCIBLE = BLOCKS.register(
            "crucible",
            () -> new CrucibleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAMPFIRE)
                    .sound(SoundType.MUD_BRICKS)
                    .lightLevel(state -> {
                        if (state.getValue(CrucibleBlock.LIT)) {
                            return 15;
                        }
                        return state.getValue(CrucibleBlock.FLUID_LEVEL) > 0 ? 8 : 0;
                    })));

    public static final DeferredBlock<BlastFurnaceBlock> BLAST_FURNACE = BLOCKS.register(
            "blast_furnace",
            () -> new BlastFurnaceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BLAST_FURNACE)
                    .lightLevel(state -> state.getValue(BlastFurnaceBlock.LIT) ? 13 : 0)));

    public static final DeferredBlock<BloomeryBlock> BLOOMERY = BLOCKS.register(
            "bloomery", () -> new BloomeryBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MUD_BRICKS)));
    public static final DeferredBlock<WorkbenchBlock> WORKBENCH = BLOCKS.register(
            "workbench", () -> new WorkbenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)));
    public static final DeferredBlock<QuernBlock> QUERN = BLOCKS.register(
            "quern",
            () -> new QuernBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.STONECUTTER).noOcclusion()));
    public static final DeferredBlock<PortableEngineBlock> PORTABLE_ENGINE = BLOCKS.register(
            "portable_engine",
            () -> new PortableEngineBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.TERRACOTTA_WHITE)
                            .requiresCorrectToolForDrops()
                            .strength(3.0F, 6.0F)
                            .sound(SoundType.COPPER)
                            .noOcclusion(),
                    null));
    public static final DeferredBlock<IronBloomBlock> IRON_BLOOM = BLOCKS.register(
            "iron_bloom",
            () -> new IronBloomBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)
                    .noOcclusion()
                    .noLootTable()));

    public static final DeferredBlock<Block> TIN_BLOCK = BLOCKS.register(
            "tin_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .sound(SoundType.METAL)
                    .strength(1.0F, 6.0F)));
    public static final DeferredBlock<Block> TIN_ORE = BLOCKS.register(
            "tin_ore",
            () -> new Block(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_ORE).strength(2.0F, 6.0F)));
    public static final DeferredBlock<Block> DEEPSLATE_TIN_ORE = BLOCKS.register(
            "deepslate_tin_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_COPPER_ORE)
                    .strength(3.0F, 6.0F)));
    public static final DeferredBlock<Block> RAW_TIN_BLOCK = BLOCKS.register(
            "raw_tin_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_COPPER_BLOCK)
                    .strength(5.0F, 6.0F)));

    public static final DeferredBlock<Block> LEAD_BLOCK = BLOCKS.register(
            "lead_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .sound(SoundType.METAL)
                    .strength(1.0F, 6.0F)));
    public static final DeferredBlock<Block> LEAD_ORE = BLOCKS.register(
            "lead_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 6.0F)));
    public static final DeferredBlock<Block> DEEPSLATE_LEAD_ORE = BLOCKS.register(
            "deepslate_lead_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                    .sound(SoundType.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(3.0F, 6.0F)));
    public static final DeferredBlock<Block> RAW_LEAD_BLOCK = BLOCKS.register(
            "raw_lead_block",
            () -> new Block(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK).strength(5.0F, 6.0F)));

    public static final DeferredBlock<Block> SILVER_BLOCK = BLOCKS.register(
            "silver_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .sound(SoundType.METAL)
                    .strength(5.0F, 6.0F)));
    public static final DeferredBlock<Block> SILVER_ORE = BLOCKS.register(
            "silver_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 6.0F)));
    public static final DeferredBlock<Block> DEEPSLATE_SILVER_ORE = BLOCKS.register(
            "deepslate_silver_ore",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)
                    .sound(SoundType.DEEPSLATE)
                    .requiresCorrectToolForDrops()
                    .strength(6.0F, 6.0F)));
    public static final DeferredBlock<Block> RAW_SILVER_BLOCK = BLOCKS.register(
            "raw_silver_block",
            () -> new Block(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK).strength(5.0F, 6.0F)));

    public static final DeferredBlock<Block> WAX_BLOCK = BLOCKS.register(
            "wax_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.MUD).strength(0.7F, 0.3F)));
    public static final DeferredBlock<Block> COAL_COKE_BLOCK = BLOCKS.register(
            "coal_coke_block", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_BLOCK)));
    public static final DeferredBlock<Block> BRONZE_BLOCK = BLOCKS.register(
            "bronze_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .sound(SoundType.METAL)
                    .strength(3.0F, 6.0F)));
    public static final DeferredBlock<Block> STEEL_BLOCK = BLOCKS.register(
            "steel_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .sound(SoundType.METAL)
                    .strength(7.0F, 8.0F)));

    public static final DeferredBlock<Block> CLINKER_BRICKS =
            BLOCKS.register("clinker_bricks", () -> new Block(clinkerProperties()));
    public static final DeferredBlock<SlabBlock> CLINKER_BRICK_SLAB =
            BLOCKS.register("clinker_brick_slab", () -> new SlabBlock(clinkerProperties()));
    public static final DeferredBlock<StairBlock> CLINKER_BRICK_STAIRS = BLOCKS.register(
            "clinker_brick_stairs",
            () -> new StairBlock(CLINKER_BRICKS.get().defaultBlockState(), clinkerProperties()));
    public static final DeferredBlock<WallBlock> CLINKER_BRICK_WALL =
            BLOCKS.register("clinker_brick_wall", () -> new WallBlock(clinkerProperties()));
    public static final DeferredBlock<Block> CLINKER_TILE =
            BLOCKS.register("clinker_tile", () -> new Block(clinkerProperties()));
    public static final DeferredBlock<SlabBlock> CLINKER_TILE_SLAB =
            BLOCKS.register("clinker_tile_slab", () -> new SlabBlock(clinkerProperties()));
    public static final DeferredBlock<StairBlock> CLINKER_TILE_STAIRS = BLOCKS.register(
            "clinker_tile_stairs", () -> new StairBlock(CLINKER_TILE.get().defaultBlockState(), clinkerProperties()));
    public static final DeferredBlock<WallBlock> CLINKER_TILE_WALL =
            BLOCKS.register("clinker_tile_wall", () -> new WallBlock(clinkerProperties()));
    public static final DeferredBlock<Block> MOSAIC_CLINKER_TILE =
            BLOCKS.register("mosaic_clinker_tile", () -> new Block(clinkerProperties()));
    public static final DeferredBlock<Block> CHISELED_CLINKER_TILE =
            BLOCKS.register("chiseled_clinker_tile", () -> new Block(clinkerProperties()));
    public static final DeferredBlock<ClinkerPillarBlock> CLINKER_PILLAR =
            BLOCKS.register("clinker_pillar", () -> new ClinkerPillarBlock(clinkerProperties()));

    private static BlockBehaviour.Properties clinkerProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
