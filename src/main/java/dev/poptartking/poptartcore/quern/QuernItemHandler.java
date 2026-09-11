package dev.poptartking.poptartcore.quern;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public final class QuernItemHandler extends InvWrapper {
    public QuernItemHandler(QuernBlockEntity quern) {
        super(quern);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        // InvWrapper checks insertion rules, but does not check Container.canTakeItem.
        if (slot != QuernBlockEntity.OUTPUT_SLOT) {
            return ItemStack.EMPTY;
        }
        return super.extractItem(slot, amount, simulate);
    }
}
