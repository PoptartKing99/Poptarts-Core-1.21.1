package dev.poptartking.poptartcore.crucible.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class CrucibleFuelSlot extends Slot {

    public CrucibleFuelSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING) > 0;
    }
}
