package dev.poptartking.poptartcore.crucible.alloying;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

public record AlloyingRecipeInput(List<ItemStack> items, FluidStack fluid, boolean blastFurnace)
        implements RecipeInput {

    public AlloyingRecipeInput(List<ItemStack> items, FluidStack fluid) {
        this(items, fluid, false);
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= items.size()) {
            throw new IllegalArgumentException("No item for index " + slot);
        }

        return items.get(slot);
    }

    @Override
    public int size() {
        return items.size();
    }
}
