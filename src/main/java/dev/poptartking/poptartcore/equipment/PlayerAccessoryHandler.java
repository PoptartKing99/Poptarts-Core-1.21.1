package dev.poptartking.poptartcore.equipment;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/** Resolves storage lazily because InventoryMenu is constructed inside the Player constructor. */
public record PlayerAccessoryHandler(Player player) implements IItemHandlerModifiable {
    @Override
    public int getSlots() {
        return AccessoryInventory.SIZE;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return AccessoryInventory.get(player).getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        AccessoryInventory.get(player).setStackInSlot(slot, stack);
        EquipmentRing.reconcile(player);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!isItemValid(slot, stack)) return stack;
        var remainder = AccessoryInventory.get(player).insertItem(slot, stack, simulate);
        if (!simulate) EquipmentRing.reconcile(player);
        return remainder;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        var extracted = AccessoryInventory.get(player).extractItem(slot, amount, simulate);
        if (!simulate) EquipmentRing.reconcile(player);
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        return AccessoryInventory.get(player).getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return AccessoryInventory.type(slot).accepts(stack)
                && EquipmentRing.canEquip(player, slot, stack)
                && (slot != AccessoryInventory.BACK_SLOT
                        || (dev.poptartking.poptartcore.integration.create.BackSlotBacktank.canEquipBack(player, stack)
                                && dev.poptartking.poptartcore.integration.reliablebackpacks.BackSlotBackpack
                                        .canEquipBack(player, stack)));
    }
}
