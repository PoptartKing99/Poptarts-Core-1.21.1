package dev.poptartking.poptartcore.catalog.datagen;

import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.CatalogItemDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

public final class CatalogDataMapProvider extends DataMapProvider {
    public CatalogDataMapProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void gather(HolderLookup.Provider lookupProvider) {
        Builder<FurnaceFuel, Item> fuels = builder(NeoForgeDataMaps.FURNACE_FUELS);

        for (CatalogItemDefinition<?> definition : PoptartCatalog.items()) {
            addFuel(fuels, definition.item().getKey(), definition.fuelBurnTime());
        }
        for (CatalogBlockDefinition<?> definition : PoptartCatalog.blocks()) {
            addFuel(fuels, definition.item().getKey(), definition.fuelBurnTime());
        }
    }

    private static void addFuel(
            Builder<FurnaceFuel, Item> fuels, net.minecraft.resources.ResourceKey<Item> itemKey, int burnTime) {
        if (burnTime > 0) {
            fuels.add(itemKey, new FurnaceFuel(burnTime), false, new ICondition[0]);
        }
    }
}
