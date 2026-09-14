package dev.poptartking.poptartcore.catalog.datagen;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class CatalogBlockTagProvider extends BlockTagsProvider {
    public CatalogBlockTagProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, PoptartCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        for (CatalogBlockDefinition<?> definition : PoptartCatalog.blocks()) {
            definition.tags().forEach(blockTag -> tag(blockTag).add(definition.get()));
        }

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(PoptartCoreBlocks.MILLSTONE_STRUCTURAL.get(), PoptartCoreBlocks.MILLSTONE_ROTOR.get());
    }
}
