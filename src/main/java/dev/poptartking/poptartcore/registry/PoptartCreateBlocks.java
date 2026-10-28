package dev.poptartking.poptartcore.registry;

import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.BUILDING;
import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.TOOLS;

import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedShaftBlock;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PoptartCreateBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PoptartCore.MOD_ID);

    public static final CatalogBlockDefinition<CasingBlock> INDUSTRIAL_PLATING = PoptartCatalog.block(
                    "industrial_plating",
                    () -> new CasingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(4.0F, 6.0F)))
            .simpleModel(PoptartCore.location("block/industrial_plating"), "")
            .mineableWithPickaxe()
            .creative(BUILDING, 150);

    public static final DeferredBlock<EncasedShaftBlock> INDUSTRIAL_ENCASED_SHAFT = BLOCKS.register(
            "industrial_encased_shaft",
            () -> new EncasedShaftBlock(industrialEncasedProperties(), INDUSTRIAL_PLATING::get));

    public static final DeferredBlock<EncasedCogwheelBlock> INDUSTRIAL_ENCASED_COGWHEEL = BLOCKS.register(
            "industrial_encased_cogwheel",
            () -> new EncasedCogwheelBlock(industrialEncasedProperties(), false, INDUSTRIAL_PLATING::get));

    public static final DeferredBlock<EncasedCogwheelBlock> INDUSTRIAL_ENCASED_LARGE_COGWHEEL = BLOCKS.register(
            "industrial_encased_large_cogwheel",
            () -> new EncasedCogwheelBlock(industrialEncasedProperties(), true, INDUSTRIAL_PLATING::get));

    public static final CatalogBlockDefinition<CasingBlock> TREATED_WOOD = PoptartCatalog.block(
                    "treated_wood",
                    () -> new CasingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS)
                            .sound(SoundType.WOOD)
                            .strength(2.0F, 3.0F)))
            .simpleModel(PoptartCore.location("block/treated_wood"), "")
            .mineableWithAxe()
            .creative(BUILDING, 160);

    public static final DeferredBlock<EncasedShaftBlock> TREATED_WOOD_ENCASED_SHAFT = BLOCKS.register(
            "treated_wood_encased_shaft",
            () -> new EncasedShaftBlock(treatedWoodEncasedProperties(), TREATED_WOOD::get));

    public static final DeferredBlock<EncasedCogwheelBlock> TREATED_WOOD_ENCASED_COGWHEEL = BLOCKS.register(
            "treated_wood_encased_cogwheel",
            () -> new EncasedCogwheelBlock(treatedWoodEncasedProperties(), false, TREATED_WOOD::get));

    public static final DeferredBlock<EncasedCogwheelBlock> TREATED_WOOD_ENCASED_LARGE_COGWHEEL = BLOCKS.register(
            "treated_wood_encased_large_cogwheel",
            () -> new EncasedCogwheelBlock(treatedWoodEncasedProperties(), true, TREATED_WOOD::get));

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
            .mineableWithPickaxe()
            .creative(TOOLS, 90);

    private static BlockBehaviour.Properties industrialEncasedProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                .sound(SoundType.METAL)
                .strength(4.0F, 6.0F)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties treatedWoodEncasedProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS)
                .sound(SoundType.WOOD)
                .strength(2.0F, 3.0F)
                .noOcclusion();
    }

    private PoptartCreateBlocks() {}

    public static void initialize() {}

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
