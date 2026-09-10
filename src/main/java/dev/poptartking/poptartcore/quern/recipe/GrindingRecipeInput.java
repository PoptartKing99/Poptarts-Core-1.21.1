package dev.poptartking.poptartcore.quern.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record GrindingRecipeInput(ItemStack ingredient) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        if (index != 0) {
            throw new IllegalArgumentException("Grinding recipes only have one input");
        }
        return ingredient;
    }

    @Override
    public int size() {
        return 1;
    }
}
