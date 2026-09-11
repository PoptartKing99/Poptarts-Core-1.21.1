package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.quern.QuernItemHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

public final class PoptartCoreCapabilities {
    private PoptartCoreCapabilities() {}

    public static void register(IEventBus eventBus) {
        eventBus.addListener(PoptartCoreCapabilities::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                PoptartCoreBlockEntities.CRUCIBLE.get(),
                // Require a face so undirected queries cannot bypass the crucible's routing.
                (crucible, side) -> side == null ? null : new SidedInvWrapper(crucible, side));
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                PoptartCoreBlockEntities.QUERN.get(),
                (quern, side) -> new QuernItemHandler(quern));
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                PoptartCoreBlockEntities.WORKBENCH.get(),
                (workbench, side) -> new InvWrapper(workbench));
    }
}
