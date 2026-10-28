package dev.poptartking.poptartcore.registry;

import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.BUILDING;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import dev.poptartking.poptartcore.flesh.DecayingFleshBlock;
import dev.poptartking.poptartcore.flesh.LifebudBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PoptartFleshBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PoptartCore.MOD_ID);

    public static final CatalogBlockDefinition<DecayingFleshBlock> FLESH = PoptartCatalog.block(
                    "flesh",
                    () -> new DecayingFleshBlock(
                            () -> PoptartFleshBlocks.ROTTEN_FLESH.get(),
                            BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK)
                                    .mapColor(MapColor.NETHER)
                                    .randomTicks()))
            .externalModel()
            .customLoot()
            .tags(BlockTags.MINEABLE_WITH_HOE)
            .creative(BUILDING, 680);

    public static final CatalogBlockDefinition<DecayingFleshBlock> LIVING_FLESH = PoptartCatalog.block(
                    "living_flesh",
                    () -> new DecayingFleshBlock(
                            FLESH::get,
                            BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK)
                                    .mapColor(MapColor.COLOR_RED)
                                    .randomTicks()))
            .externalModel()
            .customLoot()
            .tags(BlockTags.MINEABLE_WITH_HOE)
            .creative(BUILDING, 630);

    public static final CatalogBlockDefinition<Block> ROTTEN_FLESH = PoptartCatalog.block(
                    "rotten_flesh",
                    () -> new Block(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK).mapColor(MapColor.COLOR_BROWN)))
            .externalModel()
            .customLoot()
            .tags(BlockTags.MINEABLE_WITH_HOE)
            .creative(BUILDING, 690);

    public static final CatalogBlockDefinition<DecayingFleshBlock> BUDDING_FLESH = PoptartCatalog.block(
                    "budding_flesh",
                    () -> new DecayingFleshBlock(
                            LIVING_FLESH::get,
                            BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK)
                                    .mapColor(MapColor.COLOR_RED)
                                    .randomTicks()
                                    .noLootTable()))
            .externalModel()
            .noLoot()
            .tags(BlockTags.MINEABLE_WITH_HOE)
            .creative(BUILDING, 620);

    public static final DeferredBlock<LifebudBlock> LIFEBUD = BLOCKS.register(
            "lifebud",
            () -> new LifebudBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .instabreak()
                    .sound(SoundType.HONEY_BLOCK)
                    .randomTicks()
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    private static CatalogBlockDefinition<dev.poptartking.poptartcore.flesh.FleshTendrilsBlock> tendrils(String id) {
        return PoptartCatalog.block(
                        id,
                        () -> new dev.poptartking.poptartcore.flesh.FleshTendrilsBlock(BlockBehaviour.Properties.of()
                                .mapColor(MapColor.COLOR_RED)
                                .replaceable()
                                .noCollission()
                                .strength(.2F)
                                .sound(SoundType.SCULK_VEIN)
                                .pushReaction(PushReaction.DESTROY)
                                .noLootTable()))
                .externalModel()
                .noLoot();
    }

    public static final CatalogBlockDefinition<dev.poptartking.poptartcore.flesh.FleshTendrilsBlock>
            LIVING_FLESH_TENDRILS = tendrils("living_flesh_tendrils").creative(BUILDING, 640);

    public static final CatalogBlockDefinition<dev.poptartking.poptartcore.flesh.FleshTendrilsBlock> FLESH_TENDRILS =
            tendrils("flesh_tendrils").creative(BUILDING, 650);

    public static final CatalogBlockDefinition<dev.poptartking.poptartcore.flesh.FleshTendrilsBlock>
            ROTTEN_FLESH_TENDRILS = tendrils("rotten_flesh_tendrils").creative(BUILDING, 660);

    public static final CatalogBlockDefinition<dev.poptartking.poptartcore.flesh.UlcerBlock> ULCER =
            PoptartCatalog.block(
                            "ulcer",
                            () -> new dev.poptartking.poptartcore.flesh.UlcerBlock(
                                    BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK)
                                            .mapColor(MapColor.COLOR_RED)
                                            .noOcclusion()
                                            .pushReaction(PushReaction.DESTROY)))
                    .externalModel()
                    .customLoot()
                    .tags(BlockTags.MINEABLE_WITH_HOE)
                    .creative(BUILDING, 670);

    private PoptartFleshBlocks() {}

    public static void initialize() {
        PoptartCatalog.blockAlias("fresh_flesh", "living_flesh");
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
