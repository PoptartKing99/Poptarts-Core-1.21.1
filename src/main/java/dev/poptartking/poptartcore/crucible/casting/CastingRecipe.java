package dev.poptartking.poptartcore.crucible.casting;

import dev.poptartking.poptartcore.registry.PoptartCoreRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public record CastingRecipe(FluidStack fluid, Ingredient ingredient, ItemStack result)
        implements Recipe<CastingRecipeInput> {

    @Override
    public boolean matches(CastingRecipeInput input, Level level) {
        return ingredient.test(input.stack())
                && FluidStack.isSameFluid(input.fluid(), fluid)
                && fluid.getAmount() <= input.fluid().getAmount();
    }

    @Override
    public ItemStack assemble(CastingRecipeInput input, HolderLookup.Provider registries) {
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
        return PoptartCoreRecipes.CRUCIBLE_CASTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return PoptartCoreRecipes.CRUCIBLE_CASTING_TYPE.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, ingredient);
    }
}
