package dev.poptartking.poptartcore.millstone.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record MillingRecipeInput(ItemStack ingredient) implements RecipeInput {
    public ItemStack getItem(int slot) {
        if (slot != 0) {
            throw new IllegalArgumentException("No item for index " + slot);
        } else {
            return this.ingredient;
        }
    }

    public int size() {
        return 1;
    }
}
