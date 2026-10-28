package dev.poptartking.poptartcore.registry;

import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.EQUIPMENT;

import dev.poptartking.poptartcore.catalog.CatalogItemDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import net.minecraft.world.item.Item;

public final class PoptartCapeItems {
    private PoptartCapeItems() {}

    public static final CatalogItemDefinition<Item> AGILITY_CAPE = PoptartCatalog.item(
                    "agility_cape", () -> new Item(new Item.Properties().stacksTo(1)))
            .withoutGeneratedModel()
            .creative(EQUIPMENT, 330);

    public static void initialize() {}
}
