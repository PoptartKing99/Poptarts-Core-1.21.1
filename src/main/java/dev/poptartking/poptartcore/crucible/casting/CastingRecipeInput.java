package dev.poptartking.poptartcore.crucible.casting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

public record CastingRecipeInput(
        ItemStack stack,
        FluidStack fluid
) implements RecipeInput {

    @Override
    public ItemStack getItem(int slot) {
        if (slot > 0) {
            throw new IllegalArgumentException(
                    "No item for index " + slot
            );
        }

        return stack;
    }

    @Override
    public int size() {
        return 1;
    }
}