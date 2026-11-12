package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.barrel.FluidBarrelBlockEntity;
import dev.poptartking.poptartcore.blastfurnace.BlastFurnaceBlockEntity;
import dev.poptartking.poptartcore.bloomery.BloomeryBlockEntity;
import dev.poptartking.poptartcore.crucible.CrucibleBlockEntity;
import dev.poptartking.poptartcore.ingotpile.IngotPileBlockEntity;
import dev.poptartking.poptartcore.millstone.MillstoneBlockEntity;
import dev.poptartking.poptartcore.millstone.MillstoneRotorBlockEntity;
import dev.poptartking.poptartcore.quern.QuernBlockEntity;
import dev.poptartking.poptartcore.scribing.ScribingTableBlockEntity;
import dev.poptartking.poptartcore.workbench.WorkbenchBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PoptartCoreBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PoptartCore.MOD_ID);

    public static final DeferredHolder<
                    BlockEntityType<?>, BlockEntityType<dev.poptartking.poptartcore.backpack.BackpackBlockEntity>>
            BACKPACK = BLOCK_ENTITIES.register("backpack", () -> BlockEntityType.Builder.of(
                    dev.poptartking.poptartcore.backpack.BackpackBlockEntity::new,
                    PoptartEquipmentBlocks.BACKPACK.get(),
                    PoptartEquipmentBlocks.RUCKSACK.get())
            .build(null));

    public static final DeferredHolder<
                    BlockEntityType<?>, BlockEntityType<dev.poptartking.poptartcore.anvil.AnvilBlockEntity>>
            ANVIL = BLOCK_ENTITIES.register("anvil", () -> BlockEntityType.Builder.of(
                    dev.poptartking.poptartcore.anvil.AnvilBlockEntity::new,
                    PoptartWorkstationBlocks.ANVIL.get(),
                    PoptartWorkstationBlocks.CHIPPED_ANVIL.get(),
                    PoptartWorkstationBlocks.DAMAGED_ANVIL.get(),
                    PoptartWorkstationBlocks.PRIMITIVE_ANVIL.get(),
                    PoptartWorkstationBlocks.CHIPPED_PRIMITIVE_ANVIL.get(),
                    PoptartWorkstationBlocks.DAMAGED_PRIMITIVE_ANVIL.get())
            .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IngotPileBlockEntity>> INGOT_PILE =
            BLOCK_ENTITIES.register("ingot_pile", () -> BlockEntityType.Builder.of(
                            IngotPileBlockEntity::new, PoptartMaterialBlocks.INGOT_PILE.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MillstoneBlockEntity>> MILLSTONE =
            BLOCK_ENTITIES.register("millstone", () -> BlockEntityType.Builder.of(
                            MillstoneBlockEntity::new, PoptartWorkstationBlocks.MILLSTONE.get())
                    .build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MillstoneRotorBlockEntity>> MILLSTONE_ROTOR =
            BLOCK_ENTITIES.register("millstone_rotor", () -> BlockEntityType.Builder.of(
                            MillstoneRotorBlockEntity::new, PoptartWorkstationBlocks.MILLSTONE_ROTOR.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrucibleBlockEntity>> CRUCIBLE =
            BLOCK_ENTITIES.register("crucible", () -> BlockEntityType.Builder.of(
                            CrucibleBlockEntity::new, PoptartWorkstationBlocks.CRUCIBLE.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlastFurnaceBlockEntity>> BLAST_FURNACE =
            BLOCK_ENTITIES.register("blast_furnace", () -> BlockEntityType.Builder.of(
                            BlastFurnaceBlockEntity::new, PoptartWorkstationBlocks.BLAST_FURNACE.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidBarrelBlockEntity>> FLUID_BARREL =
            BLOCK_ENTITIES.register("fluid_barrel", () -> BlockEntityType.Builder.of(
                            FluidBarrelBlockEntity::new, PoptartWorkstationBlocks.FLUID_BARREL.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BloomeryBlockEntity>> BLOOMERY =
            BLOCK_ENTITIES.register("bloomery", () -> BlockEntityType.Builder.of(
                            BloomeryBlockEntity::new, PoptartWorkstationBlocks.BLOOMERY.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WorkbenchBlockEntity>> WORKBENCH =
            BLOCK_ENTITIES.register("workbench", () -> BlockEntityType.Builder.of(
                            WorkbenchBlockEntity::new, PoptartWorkstationBlocks.WORKBENCH.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ScribingTableBlockEntity>> SCRIBING_TABLE =
            BLOCK_ENTITIES.register("scribing_table", () -> BlockEntityType.Builder.of(
                            ScribingTableBlockEntity::new, PoptartWorkstationBlocks.SCRIBING_TABLE.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QuernBlockEntity>> QUERN =
            BLOCK_ENTITIES.register("quern", () -> BlockEntityType.Builder.of(
                            QuernBlockEntity::new, PoptartWorkstationBlocks.QUERN.get())
                    .build(null));

    public static final DeferredHolder<
                    BlockEntityType<?>, BlockEntityType<dev.poptartking.poptartcore.decoration.TreeTapperBlockEntity>>
            TREE_TAPPER = BLOCK_ENTITIES.register("tree_tapper", () -> BlockEntityType.Builder.of(
                    dev.poptartking.poptartcore.decoration.TreeTapperBlockEntity::new,
                    PoptartWorkstationBlocks.TREE_TAPPER.get())
            .build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
