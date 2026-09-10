package dev.poptartking.poptartcore.quern;

import dev.poptartking.poptartcore.registry.PoptartCoreRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record GrindingRecipe(Ingredient ingredient, ItemStack result, int cranks, int powderColor)
        implements Recipe<GrindingRecipeInput> {
    public static final int DEFAULT_CRANKS = 8;
    public static final int DEFAULT_POWDER_COLOR = 0xFFFFFF;

    public GrindingRecipe {
        if (cranks < 1) {
            throw new IllegalArgumentException("Grinding recipe cranks must be at least 1");
        }
        if (powderColor < 0 || powderColor > 0xFFFFFF) {
            throw new IllegalArgumentException("Grinding recipe powder color must be between 0 and 16777215");
        }
    }

    @Override
    public boolean matches(GrindingRecipeInput input, Level level) {
        return ingredient.test(input.ingredient());
    }

    @Override
    public ItemStack assemble(GrindingRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PoptartCoreRecipes.GRINDING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return PoptartCoreRecipes.GRINDING_TYPE.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, ingredient);
    }
}
