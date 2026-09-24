package dev.poptartking.poptartcore.blastfurnace;

import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
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
        event.registerBlock(
                Capabilities.FluidHandler.BLOCK,
                (level, pos, state, blockEntity, side) -> {
                    var lowerPos = state.getValue(BlastFurnaceBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
                    var lowerState = level.getBlockState(lowerPos);
                    if (!lowerState.is(PoptartCoreBlocks.BLAST_FURNACE.get())
                            || lowerState.getValue(BlastFurnaceBlock.HALF) != DoubleBlockHalf.LOWER) return null;
                    return level.getBlockEntity(lowerPos) instanceof BlastFurnaceBlockEntity furnace
                            ? furnace.outputFluidHandler() : null;
                },
                PoptartCoreBlocks.BLAST_FURNACE.get());
    }
}
