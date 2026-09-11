package dev.poptartking.poptartcore.millstone.recipe;

import dev.poptartking.poptartcore.registry.PoptartCoreRecipes;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record MillingRecipe(
        Ingredient ingredient, int inputCount, ItemStack result, int duration, float bonusChance, int bonusCount)
        implements Recipe<MillingRecipeInput> {
    public MillingRecipe {
        if (inputCount < 1
                || inputCount > 64
                || result.isEmpty()
                || duration < 1
                || !Float.isFinite(bonusChance)
                || bonusChance < 0
                || bonusChance > 1
                || bonusCount < 0
                || bonusCount > 64 - result.getCount()) {
            throw new IllegalArgumentException("Invalid milling recipe counts, duration or bonus");
        }
    }

    public boolean matches(MillingRecipeInput input, Level level) {
        ItemStack stack = input.ingredient();
        return !stack.isEmpty() && this.ingredient.test(stack) && stack.getCount() >= this.inputCount;
    }

    public int maxResultCount() {
        return this.result.getCount() + this.bonusCount;
    }

    public ItemStack assemble(MillingRecipeInput input, Provider registries) {
        return this.result.copy();
    }

    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    public ItemStack getResultItem(Provider provider) {
        return this.result;
    }

    public int getGrindingTime() {
        return this.duration;
    }

    public boolean isSpecial() {
        return true;
    }

    public RecipeSerializer<?> getSerializer() {
        return PoptartCoreRecipes.MILLING_SERIALIZER.get();
    }

    public RecipeType<?> getType() {
        return PoptartCoreRecipes.MILLING_TYPE.get();
    }

    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, new Ingredient[] {this.ingredient});
    }
}
