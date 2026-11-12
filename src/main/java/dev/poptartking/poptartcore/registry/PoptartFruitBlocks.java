package dev.poptartking.poptartcore.registry;

import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.BUILDING;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import dev.poptartking.poptartcore.fruit.FloweringLemonLeavesBlock;
import dev.poptartking.poptartcore.fruit.LemonFoliagePlacer;
import dev.poptartking.poptartcore.fruit.LemonFruitBlock;
import dev.poptartking.poptartcore.fruit.LemonFruitDecorator;
import dev.poptartking.poptartcore.fruit.LemonTrunkPlacer;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PoptartFruitBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PoptartCore.MOD_ID);
    private static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS =
            DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, PoptartCore.MOD_ID);
    private static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACERS =
            DeferredRegister.create(Registries.TRUNK_PLACER_TYPE, PoptartCore.MOD_ID);
    private static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACERS =
            DeferredRegister.create(Registries.FOLIAGE_PLACER_TYPE, PoptartCore.MOD_ID);
    private static final ResourceKey<ConfiguredFeature<?, ?>> LEMON_TREE =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, PoptartCore.location("lemon_tree"));
    private static final TreeGrower LEMON_GROWER = new TreeGrower(
            PoptartCore.location("lemon").toString(), Optional.empty(), Optional.of(LEMON_TREE), Optional.empty());

    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<LemonFruitDecorator>>
            LEMON_FRUIT_DECORATOR =
                    TREE_DECORATORS.register("lemon_fruit", () -> new TreeDecoratorType<>(LemonFruitDecorator.CODEC));
    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<LemonTrunkPlacer>> LEMON_TRUNK_PLACER =
            TRUNK_PLACERS.register("lemon", () -> new TrunkPlacerType<>(LemonTrunkPlacer.CODEC));
    public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<LemonFoliagePlacer>>
            LEMON_FOLIAGE_PLACER =
                    FOLIAGE_PLACERS.register("lemon", () -> new FoliagePlacerType<>(LemonFoliagePlacer.CODEC));

    public static final CatalogBlockDefinition<SaplingBlock> LEMON_SAPLING = PoptartCatalog.block(
                    "lemon_sapling",
                    () -> new SaplingBlock(LEMON_GROWER, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)))
            .externalModel()
            .tags(BlockTags.SAPLINGS)
            .itemTags(ItemTags.SAPLINGS)
            .creative(BUILDING);

    public static final CatalogBlockDefinition<LeavesBlock> LEMON_LEAVES = PoptartCatalog.block(
                    "lemon_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)))
            .externalModel()
            .customLoot()
            .tags(BlockTags.LEAVES)
            .itemTags(ItemTags.LEAVES)
            .creative(BUILDING);

    public static final CatalogBlockDefinition<FloweringLemonLeavesBlock> FLOWERING_LEMON_LEAVES = PoptartCatalog.block(
                    "flowering_lemon_leaves",
                    () -> new FloweringLemonLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)))
            .externalModel()
            .customLoot()
            .tags(BlockTags.LEAVES)
            .itemTags(ItemTags.LEAVES)
            .creative(BUILDING);

    public static final DeferredBlock<LemonFruitBlock> LEMON_FRUIT = BLOCKS.register(
            "lemon_fruit",
            () -> new LemonFruitBlock(BlockBehaviour.Properties.of()
                    .noOcclusion()
                    .instabreak()
                    .offsetType(BlockBehaviour.OffsetType.XYZ)
                    .dynamicShape()
                    .sound(SoundType.CROP)
                    .pushReaction(PushReaction.DESTROY)));

    private PoptartFruitBlocks() {}

    public static void initialize() {}

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        TREE_DECORATORS.register(eventBus);
        TRUNK_PLACERS.register(eventBus);
        FOLIAGE_PLACERS.register(eventBus);
    }
}
