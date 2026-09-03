package dev.poptartking.poptartcore.crucible.melting;

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

public record MeltingRecipe(
        Ingredient ingredient,
        int duration,
        FluidStack result,
        boolean blastFurnace
) implements Recipe<MeltingRecipeInput> {

    @Override
    public boolean matches(
            MeltingRecipeInput input,
            Level level
    ) {
        if (blastFurnace && !input.blastFurnace()) {
            return false;
        }

        boolean found = false;

        for (ItemStack stack : input.stacks()) {
            if (!stack.isEmpty()) {
                if (!ingredient.test(stack)) {
                    return false;
                }

                found = true;
            }
        }

        return found;
    }

    @Override
    public ItemStack assemble(
            MeltingRecipeInput input,
            HolderLookup.Provider registries
    ) {
        return ItemStack.EMPTY;
    }

    public FluidStack assembleFluid(
            MeltingRecipeInput input
    ) {
        return result.copyWithAmount(
                result.getAmount() * batchCount(input)
        );
    }

    public int batchCount(
            MeltingRecipeInput input
    ) {
        int found = 0;

        for (ItemStack stack : input.stacks()) {
            if (!stack.isEmpty() && ingredient.test(stack)) {
                found++;
            }
        }

        return found;
    }

    @Override
    public boolean canCraftInDimensions(
            int width,
            int height
    ) {
        return width * height > 0;
    }

    @Override
    public ItemStack getResultItem(
            HolderLookup.Provider registries
    ) {
        return ItemStack.EMPTY;
    }

    public int getCookingTime() {
        return duration;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PoptartCoreRecipes.CRUCIBLE_MELTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return PoptartCoreRecipes.CRUCIBLE_MELTING_TYPE.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(
                Ingredient.EMPTY,
                ingredient
        );
    }
}