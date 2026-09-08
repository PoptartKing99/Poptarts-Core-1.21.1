package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.blastfurnace.BlastFurnaceBlock;
import dev.poptartking.poptartcore.bloomery.BloomeryBlock;
import dev.poptartking.poptartcore.bloomery.IronBloomBlock;
import dev.poptartking.poptartcore.clinker.ClinkerPillarBlock;
import dev.poptartking.poptartcore.crucible.CrucibleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PoptartCoreBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PoptartCore.MOD_ID);

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
    public static final DeferredBlock<IronBloomBlock> IRON_BLOOM = BLOCKS.register(
            "iron_bloom",
            () -> new IronBloomBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)
                    .noOcclusion()
                    .noLootTable()));

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
