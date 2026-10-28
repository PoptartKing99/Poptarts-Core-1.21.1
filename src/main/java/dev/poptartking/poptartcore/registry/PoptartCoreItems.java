package dev.poptartking.poptartcore.registry;

/** Initializes each item group before the shared catalog is frozen and registered. */
public final class PoptartCoreItems {
    private PoptartCoreItems() {}

    public static void initialize() {
        PoptartRingItems.initialize();
        PoptartCapeItems.initialize();
        PoptartToolItems.initialize();
        PoptartArmorItems.initialize();
        PoptartMaterialItems.initialize();
        PoptartProcessingItems.initialize();
    }
}
