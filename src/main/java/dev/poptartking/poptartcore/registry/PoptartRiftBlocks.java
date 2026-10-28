package dev.poptartking.poptartcore.registry;

import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.BUILDING;
import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.MATERIALS;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import dev.poptartking.poptartcore.rift.CupricBrazierBlock;
import dev.poptartking.poptartcore.rift.CupricSconceLeverBlock;
import dev.poptartking.poptartcore.rift.CupricSconceTorchBlock;
import dev.poptartking.poptartcore.rift.CupricSconceWallTorchBlock;
import dev.poptartking.poptartcore.rift.CupricStoneBrazierBlock;
import dev.poptartking.poptartcore.rift.PackedRiftSedimentBlock;
import dev.poptartking.poptartcore.rift.RiftBrazierBlock;
import dev.poptartking.poptartcore.rift.RiftCampfireBlock;
import dev.poptartking.poptartcore.rift.RiftFireBlock;
import dev.poptartking.poptartcore.rift.RiftSconceLeverBlock;
import dev.poptartking.poptartcore.rift.RiftSconceTorchBlock;
import dev.poptartking.poptartcore.rift.RiftSconceWallTorchBlock;
import dev.poptartking.poptartcore.rift.RiftSedimentBlock;
import dev.poptartking.poptartcore.rift.RiftStoneBrazierBlock;
import dev.poptartking.poptartcore.rift.RiftTorchBlock;
import dev.poptartking.poptartcore.rift.RiftWallTorchBlock;
import dev.poptartking.poptartcore.rift.SoulSconceLeverBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SconceLeverBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PoptartRiftBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PoptartCore.MOD_ID);

    public static final DeferredBlock<RiftFireBlock> RIFT_FIRE = BLOCKS.register(
            "rift_fire",
            () -> new RiftFireBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.FIRE).noLootTable()));

    public static final DeferredBlock<RiftWallTorchBlock> RIFT_WALL_TORCH = BLOCKS.register(
            "rift_wall_torch",
            () -> new RiftWallTorchBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .lightLevel(state -> 14)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)));

    public static final CatalogBlockDefinition<RiftTorchBlock> RIFT_TORCH = PoptartCatalog.block(
                    "rift_torch",
                    () -> new RiftTorchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TORCH)),
                    (block, properties) ->
                            new StandingAndWallBlockItem(block, RIFT_WALL_TORCH.get(), properties, Direction.DOWN))
            .externalModel()
            .creative(BUILDING, 10);

    public static final DeferredBlock<RiftSconceWallTorchBlock> SCONCE_RIFT_WALL_TORCH = BLOCKS.register(
            "sconce_rift_wall_torch",
            () -> new RiftSconceWallTorchBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .lightLevel(state -> 14)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)));

    public static final CatalogBlockDefinition<RiftSconceTorchBlock> SCONCE_RIFT_TORCH = PoptartCatalog.block(
                    "sconce_rift_torch",
                    () -> new RiftSconceTorchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TORCH)),
                    (block, properties) -> new StandingAndWallBlockItem(
                            block, SCONCE_RIFT_WALL_TORCH.get(), properties, Direction.DOWN))
            .externalModel()
            .customLoot()
            .creative(BUILDING, 20);

    public static final DeferredBlock<CupricSconceWallTorchBlock> SCONCE_CUPRIC_WALL_TORCH = BLOCKS.register(
            "sconce_cupric_wall_torch",
            () -> new CupricSconceWallTorchBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .lightLevel(state -> 10)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)));

    public static final CatalogBlockDefinition<CupricSconceTorchBlock> SCONCE_CUPRIC_TORCH = PoptartCatalog.block(
                    "sconce_cupric_torch",
                    () -> new CupricSconceTorchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_TORCH)),
                    (block, properties) -> new StandingAndWallBlockItem(
                            block, SCONCE_CUPRIC_WALL_TORCH.get(), properties, Direction.DOWN))
            .externalModel()
            .creative(BUILDING, 30);

    public static final CatalogBlockDefinition<LanternBlock> RIFT_LANTERN = PoptartCatalog.block(
                    "rift_lantern", () -> new LanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN)))
            .externalModel()
            .mineableWithPickaxe()
            .creative(BUILDING, 40);

    public static final CatalogBlockDefinition<RiftCampfireBlock> RIFT_CAMPFIRE = PoptartCatalog.block(
                    "rift_campfire", () -> new RiftCampfireBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAMPFIRE)))
            .externalModel()
            .mineableWithAxe()
            .creative(BUILDING, 50);

    public static final CatalogBlockDefinition<RiftBrazierBlock> RIFT_BRAZIER = PoptartCatalog.block(
                    "rift_brazier", () -> new RiftBrazierBlock(riftBrazierProperties()))
            .externalModel()
            .mineableWithPickaxe()
            .creative(BUILDING, 60);

    public static final CatalogBlockDefinition<RiftStoneBrazierBlock> RIFT_STONE_BRAZIER = PoptartCatalog.block(
                    "rift_stone_brazier", () -> new RiftStoneBrazierBlock(stoneBrazierProperties(15)))
            .externalModel()
            .mineableWithPickaxe()
            .creative(BUILDING, 70);

    public static final CatalogBlockDefinition<CupricBrazierBlock> CUPRIC_BRAZIER = PoptartCatalog.block(
                    "cupric_brazier", () -> new CupricBrazierBlock(cupricBrazierProperties()))
            .externalModel()
            .mineableWithPickaxe()
            .creative(BUILDING, 80);

    public static final CatalogBlockDefinition<CupricStoneBrazierBlock> CUPRIC_STONE_BRAZIER = PoptartCatalog.block(
                    "cupric_stone_brazier", () -> new CupricStoneBrazierBlock(stoneBrazierProperties(10)))
            .externalModel()
            .mineableWithPickaxe()
            .creative(BUILDING, 90);

    public static final CatalogBlockDefinition<SconceLeverBlock> SCONCE_TORCH_LEVER = PoptartCatalog.block(
                    "sconce_torch_lever",
                    () -> new SconceLeverBlock(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.LEVER)
                                    .noOcclusion()
                                    .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 14 : 0),
                            () -> ParticleTypes.FLAME))
            .externalModel()
            .customLoot()
            .creative(BUILDING, 100);

    public static final CatalogBlockDefinition<RiftSconceLeverBlock> SCONCE_RIFT_TORCH_LEVER = PoptartCatalog.block(
                    "sconce_rift_torch_lever",
                    () -> new RiftSconceLeverBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LEVER)
                            .noOcclusion()
                            .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 15 : 0)))
            .externalModel()
            .creative(BUILDING, 110);

    public static final CatalogBlockDefinition<CupricSconceLeverBlock> SCONCE_CUPRIC_TORCH_LEVER = PoptartCatalog.block(
                    "sconce_cupric_torch_lever",
                    () -> new CupricSconceLeverBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LEVER)
                            .noOcclusion()
                            .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 10 : 0)))
            .externalModel()
            .creative(BUILDING, 120);

    public static final CatalogBlockDefinition<SoulSconceLeverBlock> SCONCE_SOUL_TORCH_LEVER = PoptartCatalog.block(
                    "sconce_soul_torch_lever",
                    () -> new SoulSconceLeverBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LEVER)
                            .noOcclusion()
                            .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 10 : 0)))
            .externalModel()
            .creative(BUILDING, 130);

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
            .noLoot()
            .creative(MATERIALS, 630);

    public static final CatalogBlockDefinition<PackedRiftSedimentBlock> RIFT_SEDIMENT_BLOCK = PoptartCatalog.block(
                    "rift_sediment_block",
                    () -> new PackedRiftSedimentBlock(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).mapColor(MapColor.COLOR_PURPLE)))
            .withName("Block of Rift Sediment")
            .withStorageRecipes(PoptartRiftBlocks.RIFT_SEDIMENT::get, "rift_sediment_from_block")
            .simpleModel(PoptartCore.location("block/rift_sediment_block"), "")
            .tags(BlockTags.MINEABLE_WITH_SHOVEL)
            .creative(MATERIALS, 640);

    private static BlockBehaviour.Properties riftBrazierProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN)
                .strength(4.0F, 8.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 15 : 0);
    }

    private static BlockBehaviour.Properties cupricBrazierProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_LANTERN)
                .strength(4.0F, 8.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 10 : 0);
    }

    private static BlockBehaviour.Properties stoneBrazierProperties(int light) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(4.0F, 8.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .ignitedByLava()
                .instrument(NoteBlockInstrument.BASS)
                .sound(SoundType.POLISHED_DEEPSLATE)
                .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? light : 0);
    }

    private PoptartRiftBlocks() {}

    public static void initialize() {}

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
