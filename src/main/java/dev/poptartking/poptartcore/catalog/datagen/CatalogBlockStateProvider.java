package dev.poptartking.poptartcore.catalog.datagen;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class CatalogBlockStateProvider extends BlockStateProvider {
    public CatalogBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, PoptartCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (CatalogBlockDefinition<?> definition : PoptartCatalog.blocks()) {
            simpleBlockWithItem(definition.get(), cubeAll(definition.get()));
        }
    }
}
