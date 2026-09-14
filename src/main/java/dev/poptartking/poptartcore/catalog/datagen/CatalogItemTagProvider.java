package dev.poptartking.poptartcore.catalog.datagen;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.catalog.CatalogItemDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class CatalogItemTagProvider extends ItemTagsProvider {
    public CatalogItemTagProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper) {
        super(
                output,
                lookupProvider,
                CompletableFuture.completedFuture(TagsProvider.TagLookup.<Block>empty()),
                PoptartCore.MOD_ID,
                existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        for (CatalogItemDefinition<?> definition : PoptartCatalog.items()) {
            definition.tags().forEach(itemTag -> tag(itemTag).add(definition.get()));
        }
    }
}
