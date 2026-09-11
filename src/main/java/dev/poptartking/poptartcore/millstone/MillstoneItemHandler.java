package dev.poptartking.poptartcore.millstone;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class MillstoneItemHandler implements IItemHandler {
    private final MillstoneBlockEntity millstone;

    public MillstoneItemHandler(MillstoneBlockEntity millstone) {
        this.millstone = millstone;
    }

    public int getSlots() {
        return 18;
    }

    private boolean isOutputSlot(int slot) {
        if (slot < 0 || slot >= getSlots()) {
            throw new IndexOutOfBoundsException("Millstone slot " + slot);
        }
        return slot < 9;
    }

    private boolean available() {
        var level = millstone.getLevel();
        return !millstone.isRemoved()
                && level != null
                && level.hasChunkAt(millstone.getBlockPos())
                && level.getBlockEntity(millstone.getBlockPos()) == millstone;
    }

    public ItemStack getStackInSlot(int slot) {
        if (!available()) return ItemStack.EMPTY;
        return this.isOutputSlot(slot)
                ? this.millstone.getBufferStack(false, slot)
                : this.millstone.getBufferStack(true, slot - 9);
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!available()) return stack;
        return this.isOutputSlot(slot) ? stack : this.millstone.insertInput(stack, simulate);
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!available()) return ItemStack.EMPTY;
        return !this.isOutputSlot(slot) ? ItemStack.EMPTY : this.millstone.extractOutput(slot, amount, simulate);
    }

    public int getSlotLimit(int slot) {
        return 64;
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return available() && !this.isOutputSlot(slot) && this.millstone.acceptsItem(stack);
    }
}
