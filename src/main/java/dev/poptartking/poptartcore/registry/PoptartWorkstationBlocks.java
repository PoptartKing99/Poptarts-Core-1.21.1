package dev.poptartking.poptartcore.registry;

import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.BUILDING;
import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.MATERIALS;
import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.TOOLS;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.barrel.FluidBarrelBlock;
import dev.poptartking.poptartcore.barrel.FluidBarrelBlockItem;
import dev.poptartking.poptartcore.blastfurnace.BlastFurnaceBlock;
import dev.poptartking.poptartcore.bloomery.BloomeryBlock;
import dev.poptartking.poptartcore.bloomery.IronBloomBlock;
import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import dev.poptartking.poptartcore.crucible.CrucibleBlock;
import dev.poptartking.poptartcore.crucible.CrucibleBlockItem;
import dev.poptartking.poptartcore.millstone.MillstoneBlock;
import dev.poptartking.poptartcore.millstone.MillstoneBlockItem;
import dev.poptartking.poptartcore.millstone.MillstoneRotorBlock;
import dev.poptartking.poptartcore.millstone.MillstoneStructuralBlock;
import dev.poptartking.poptartcore.quern.QuernBlock;
import dev.poptartking.poptartcore.scribing.ScribingTableBlock;
import dev.poptartking.poptartcore.workbench.WorkbenchBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PoptartWorkstationBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PoptartCore.MOD_ID);

    public static final CatalogBlockDefinition<dev.poptartking.poptartcore.decoration.TreeTapperBlock> TREE_TAPPER =
            PoptartCatalog.block(
                            "wooden_bucket",
                            () -> new dev.poptartking.poptartcore.decoration.TreeTapperBlock(
                                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                                            .strength(1.0F)
                                            .noOcclusion()))
                    .withName("Tree Tapper")
                    .externalModel()
                    .customLoot()
                    .tags(BlockTags.MINEABLE_WITH_AXE)
                    .creative(BUILDING, 700);

    public static final CatalogBlockDefinition<MillstoneBlock> MILLSTONE = PoptartCatalog.block(
                    "millstone", () -> new MillstoneBlock(millstoneProperties()), MillstoneBlockItem::new)
            .externalModel()
            .mineableWithPickaxe()
            .creative(TOOLS, 40);

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
            .mineableWithPickaxe()
            .creative(TOOLS, 70);

    public static final CatalogBlockDefinition<BlastFurnaceBlock> BLAST_FURNACE = PoptartCatalog.block(
                    "blast_furnace",
                    () -> new BlastFurnaceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BLAST_FURNACE)
                            .lightLevel(state -> state.getValue(BlastFurnaceBlock.LIT) ? 13 : 0)))
            .externalModel()
            .mineableWithPickaxe()
            .creative(TOOLS, 60);

    public static final CatalogBlockDefinition<FluidBarrelBlock> FLUID_BARREL = PoptartCatalog.block(
                    "fluid_barrel",
                    () -> new FluidBarrelBlock(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).noOcclusion()),
                    FluidBarrelBlockItem::new)
            .externalModel()
            .customLoot()
            .mineableWithAxe()
            .creative(TOOLS, 80);

    public static final CatalogBlockDefinition<BloomeryBlock> BLOOMERY = PoptartCatalog.block(
                    "bloomery", () -> new BloomeryBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MUD_BRICKS)))
            .externalModel()
            .mineableWithPickaxe()
            .creative(TOOLS, 50);

    public static final CatalogBlockDefinition<WorkbenchBlock> WORKBENCH = PoptartCatalog.block(
                    "workbench", () -> new WorkbenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)))
            .externalModel()
            .mineableWithAxe()
            .creative(TOOLS, 10);

    public static final CatalogBlockDefinition<QuernBlock> QUERN = PoptartCatalog.block(
                    "quern",
                    () -> new QuernBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONECUTTER)
                            .noOcclusion()))
            .externalModel()
            .mineableWithPickaxe()
            .creative(TOOLS, 30);

    public static final CatalogBlockDefinition<IronBloomBlock> IRON_BLOOM = PoptartCatalog.block(
                    "iron_bloom",
                    () -> new IronBloomBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)
                            .noOcclusion()
                            .noLootTable()))
            .externalModel()
            .noLoot()
            .mineableWithPickaxe()
            .creative(MATERIALS, 430);

    public static final CatalogBlockDefinition<ScribingTableBlock> SCRIBING_TABLE = PoptartCatalog.block(
                    "scribing_table",
                    () -> new ScribingTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)))
            .externalModel()
            .mineableWithAxe()
            .creative(TOOLS, 20);

    private static CatalogBlockDefinition<dev.poptartking.poptartcore.anvil.AnvilBlock> anvil(
            String id, boolean primitive) {
        return PoptartCatalog.block(
                        id,
                        () -> primitive
                                ? new dev.poptartking.poptartcore.anvil.PrimitiveAnvilBlock(
                                        BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL))
                                : new dev.poptartking.poptartcore.anvil.AnvilBlock(
                                        BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL)))
                .externalModel()
                .customLoot()
                .tags(BlockTags.MINEABLE_WITH_PICKAXE);
    }

    public static final CatalogBlockDefinition<dev.poptartking.poptartcore.anvil.AnvilBlock> ANVIL =
            anvil("anvil", false).creative(TOOLS, 580);

    public static final CatalogBlockDefinition<dev.poptartking.poptartcore.anvil.AnvilBlock> CHIPPED_ANVIL =
            anvil("chipped_anvil", false).creative(TOOLS, 590);

    public static final CatalogBlockDefinition<dev.poptartking.poptartcore.anvil.AnvilBlock> DAMAGED_ANVIL =
            anvil("damaged_anvil", false).creative(TOOLS, 600);

    public static final CatalogBlockDefinition<dev.poptartking.poptartcore.anvil.AnvilBlock> PRIMITIVE_ANVIL =
            anvil("primitive_anvil", true).creative(TOOLS, 610);

    public static final CatalogBlockDefinition<dev.poptartking.poptartcore.anvil.AnvilBlock> CHIPPED_PRIMITIVE_ANVIL =
            anvil("chipped_primitive_anvil", true).creative(TOOLS, 620);

    public static final CatalogBlockDefinition<dev.poptartking.poptartcore.anvil.AnvilBlock> DAMAGED_PRIMITIVE_ANVIL =
            anvil("damaged_primitive_anvil", true).creative(TOOLS, 630);

    private PoptartWorkstationBlocks() {}

    public static void initialize() {}

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
