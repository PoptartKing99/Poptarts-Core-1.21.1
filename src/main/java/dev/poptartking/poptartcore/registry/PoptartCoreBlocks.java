package dev.poptartking.poptartcore.registry;

import net.neoforged.bus.api.IEventBus;

/** Loads block groups before the shared catalog is frozen, then registers their itemless blocks. */
public final class PoptartCoreBlocks {
    private PoptartCoreBlocks() {}

    public static void initialize() {
        PoptartEquipmentBlocks.initialize();
        PoptartWorkstationBlocks.initialize();
        PoptartMaterialBlocks.initialize();
        PoptartSpiderBlocks.initialize();
        PoptartRiftBlocks.initialize();
        PoptartCreateBlocks.initialize();
        PoptartFleshBlocks.initialize();
        PoptartBuildingBlocks.initialize();
        PoptartBeekeepingBlocks.initialize();
    }

    public static void register(IEventBus eventBus) {
        PoptartWorkstationBlocks.register(eventBus);
        PoptartMaterialBlocks.register(eventBus);
        PoptartSpiderBlocks.register(eventBus);
        PoptartRiftBlocks.register(eventBus);
        PoptartCreateBlocks.register(eventBus);
        PoptartFleshBlocks.register(eventBus);
    }
}
