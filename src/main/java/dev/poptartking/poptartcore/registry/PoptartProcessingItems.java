package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.catalog.CatalogItemDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class PoptartProcessingItems {
    private PoptartProcessingItems() {}

    public static final CatalogItemDefinition<Item> UNFIRED_INGOT_MOULD =
            PoptartCatalog.simpleItem("unfired_ingot_mould");

    public static final CatalogItemDefinition<Item> INGOT_MOULD =
            PoptartCatalog.item("ingot_mould", () -> new Item(new Item.Properties().durability(32)));

    public static final CatalogItemDefinition<Item> UNFIRED_PLATE_MOULD =
            PoptartCatalog.simpleItem("unfired_plate_mould");

    public static final CatalogItemDefinition<Item> PLATE_MOULD =
            PoptartCatalog.item("plate_mould", () -> new Item(new Item.Properties().durability(32)));

    public static boolean isMould(ItemStack stack) {
        return stack.is(INGOT_MOULD.get()) || stack.is(PLATE_MOULD.get());
    }

    public static void initialize() {}
}
