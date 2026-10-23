package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class AccessoryInventory extends ItemStackHandler {
    public static final int SIZE = 7;
    public static final int UTILITY_SLOT = 4;
    public static final int BACK_SLOT = 6;

    public static AccessoryType type(int index) {
        return switch (index) {
            case 0, 1 -> AccessoryType.RING;
            case 2 -> AccessoryType.CAPE;
            case 3 -> AccessoryType.NECKLACE;
            case 4 -> AccessoryType.UTILITY;
            case 5 -> AccessoryType.BELT;
            case 6 -> AccessoryType.BACK;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    public AccessoryInventory() {
        super(SIZE);
    }

    @Override
    public void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider, net.minecraft.nbt.CompoundTag tag) {
        var resized = tag.copy();
        resized.putInt("Size", SIZE);
        super.deserializeNBT(provider, resized);
    }

    public static AccessoryInventory get(Player player) {
        return player.getData(PoptartCoreAttachments.ACCESSORIES);
    }

    @Override
    public int getSlotLimit(int slot) {
        return type(slot) == AccessoryType.UTILITY ? net.minecraft.world.item.Item.ABSOLUTE_MAX_STACK_SIZE : 1;
    }

    public static int stackLimit(AccessoryType type, ItemStack stack) {
        return type == AccessoryType.UTILITY && stack.getItem() instanceof com.farcr.nomansland.common.item.BandageItem
                ? stack.getMaxStackSize()
                : 1;
    }

    @Override
    protected int getStackLimit(int slot, ItemStack stack) {
        return stackLimit(type(slot), stack);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return type(slot).accepts(stack);
    }

    public static void copy(Player from, Player to) {
        for (int i = 0; i < SIZE; i++)
            get(to).setStackInSlot(i, get(from).getStackInSlot(i).copy());
    }

    public static void dropAll(Player player) {
        var inventory = get(player);
        for (int i = 0; i < SIZE; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            inventory.setStackInSlot(i, ItemStack.EMPTY);
            if (!stack.isEmpty()) player.drop(stack, true, false);
        }
    }
}
