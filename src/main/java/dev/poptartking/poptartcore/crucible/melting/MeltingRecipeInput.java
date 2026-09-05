package dev.poptartking.poptartcore.crucible.melting;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record MeltingRecipeInput(List<ItemStack> stacks, boolean blastFurnace) implements RecipeInput {

    public MeltingRecipeInput(List<ItemStack> stacks) {
        this(stacks, false);
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= stacks.size()) {
            throw new IllegalArgumentException("No item for index " + slot);
        }

        return stacks.get(slot);
    }

    @Override
    public int size() {
        return stacks.size();
    }
}
