package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.blastfurnace.BlastFurnaceBlockEntity;
import dev.poptartking.poptartcore.bloomery.BloomeryBlockEntity;
import dev.poptartking.poptartcore.crucible.CrucibleBlockEntity;
import dev.poptartking.poptartcore.workbench.WorkbenchBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PoptartCoreBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PoptartCore.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrucibleBlockEntity>> CRUCIBLE =
            BLOCK_ENTITIES.register("crucible", () -> BlockEntityType.Builder.of(
                            CrucibleBlockEntity::new, PoptartCoreBlocks.CRUCIBLE.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlastFurnaceBlockEntity>> BLAST_FURNACE =
            BLOCK_ENTITIES.register("blast_furnace", () -> BlockEntityType.Builder.of(
                            BlastFurnaceBlockEntity::new, PoptartCoreBlocks.BLAST_FURNACE.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BloomeryBlockEntity>> BLOOMERY =
            BLOCK_ENTITIES.register("bloomery", () -> BlockEntityType.Builder.of(
                            BloomeryBlockEntity::new, PoptartCoreBlocks.BLOOMERY.get())
                    .build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WorkbenchBlockEntity>> WORKBENCH =
            BLOCK_ENTITIES.register("workbench", () -> BlockEntityType.Builder.of(
                            WorkbenchBlockEntity::new, PoptartCoreBlocks.WORKBENCH.get())
                    .build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
