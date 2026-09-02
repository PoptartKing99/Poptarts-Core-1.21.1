package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.item.MiningHelmetItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PoptartCoreItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(PoptartCore.MOD_ID);

    // Items
    public static final DeferredItem<MiningHelmetItem> MINING_HELMET =
            ITEMS.register(
                    "mining_helmet",
                    () -> new MiningHelmetItem(new Item.Properties()
                            .durability(100)

                    )
            );

    // Helper Functions
    private static DeferredItem<Item> registerItem(String name) {
        return registerItem(name, new Item.Properties());
    }

    private static DeferredItem<Item> registerItem(String name, Item.Properties properties) {
        return ITEMS.registerSimpleItem(name, properties);
    }

    // Registration
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}