package dev.poptartking.poptartcore.catalog.datagen;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

public final class CatalogRecipeProvider extends RecipeProvider {
    public CatalogRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        for (CatalogBlockDefinition<?> definition : PoptartCatalog.blocks()) {
            CatalogBlockDefinition.StorageRecipes recipes = definition.storageRecipes();
            if (recipes == null) {
                continue;
            }
            nineBlockStorageRecipes(
                    output,
                    RecipeCategory.MISC,
                    recipes.ingredient().get(),
                    RecipeCategory.BUILDING_BLOCKS,
                    definition.get(),
                    PoptartCore.MOD_ID + ":" + recipes.packingRecipeId(),
                    null,
                    PoptartCore.MOD_ID + ":" + recipes.unpackingRecipeId(),
                    null);
        }
    }
}
