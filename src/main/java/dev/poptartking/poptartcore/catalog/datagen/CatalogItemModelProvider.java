package dev.poptartking.poptartcore.catalog.datagen;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.catalog.CatalogItemDefinition;
import dev.poptartking.poptartcore.catalog.CatalogItemModel;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class CatalogItemModelProvider extends ItemModelProvider {
    public CatalogItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, PoptartCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (CatalogItemDefinition<?> definition : PoptartCatalog.items()) {
            if (definition.model() == CatalogItemModel.GENERATED) {
                basicItem(definition.item().get());
            } else if (definition.model() == CatalogItemModel.HANDHELD) {
                handheldItem(definition.item().get());
            }
        }
    }
}
