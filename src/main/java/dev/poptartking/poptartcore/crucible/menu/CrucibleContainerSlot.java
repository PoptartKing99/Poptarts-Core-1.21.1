package dev.poptartking.poptartcore.crucible.menu;

import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CrucibleContainerSlot extends Slot {

    public CrucibleContainerSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(PoptartCoreItems.INGOT_MOULD.get());
    }
}
