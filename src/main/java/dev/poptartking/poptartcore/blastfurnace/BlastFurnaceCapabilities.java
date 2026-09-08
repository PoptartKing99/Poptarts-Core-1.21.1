package dev.poptartking.poptartcore.blastfurnace;

import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.world.WorldlyContainer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

public final class BlastFurnaceCapabilities {
    private BlastFurnaceCapabilities() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(BlastFurnaceCapabilities::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
                Capabilities.ItemHandler.BLOCK,
                (level, pos, state, blockEntity, side) -> {
                    BlastFurnaceBlock block = (BlastFurnaceBlock) state.getBlock();
                    WorldlyContainer container = block.getContainer(state, level, pos);
                    return container == null ? null : new SidedInvWrapper(container, side);
                },
                PoptartCoreBlocks.BLAST_FURNACE.get());
    }
}
