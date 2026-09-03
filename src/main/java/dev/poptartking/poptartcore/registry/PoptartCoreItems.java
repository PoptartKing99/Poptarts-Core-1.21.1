package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.crucible.CrucibleBlockItem;
import dev.poptartking.poptartcore.item.MiningHelmetItem;
import dev.poptartking.poptartcore.item.RawHideArmorItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PoptartCoreItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(PoptartCore.MOD_ID);

    // Items
    public static final DeferredItem<MiningHelmetItem> MINING_HELMET =
            ITEMS.register("mining_helmet", () -> new MiningHelmetItem(new Item.Properties().durability(100)));
    public static final DeferredItem<RawHideArmorItem> RAW_HIDE_HELMET =
            ITEMS.register("raw_hide_helmet", () -> new RawHideArmorItem(ArmorItem.Type.HELMET, new Item.Properties().durability(140)));
    public static final DeferredItem<RawHideArmorItem> RAW_HIDE_CHESTPLATE =
            ITEMS.register("raw_hide_chestplate", () -> new RawHideArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(140)));
    public static final DeferredItem<RawHideArmorItem> RAW_HIDE_LEGGINGS =
            ITEMS.register("raw_hide_leggings", () -> new RawHideArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties().durability(140)));
    public static final DeferredItem<CrucibleBlockItem> CRUCIBLE =
            ITEMS.register("crucible", () -> new CrucibleBlockItem(PoptartCoreBlocks.CRUCIBLE.get(), new Item.Properties()));
    public static final DeferredItem<Item> UNFIRED_INGOT_MOULD =
            ITEMS.register("unfired_ingot_mould", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_MOULD =
            ITEMS.register("ingot_mould", () -> new Item(new Item.Properties().durability(32)));

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