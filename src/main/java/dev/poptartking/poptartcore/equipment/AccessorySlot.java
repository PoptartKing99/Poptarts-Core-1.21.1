package dev.poptartking.poptartcore.equipment;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class AccessorySlot extends SlotItemHandler implements AccessorySlotView {
    private final AccessoryType type;

    public AccessorySlot(Player player, int index) {
        super(new PlayerAccessoryHandler(player), index, AccessoryLayout.x(index), AccessoryLayout.y(index));
        type = AccessoryInventory.type(index);
    }

    @Override
    public AccessoryType accessoryType() {
        return type;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return AccessoryInventory.stackLimit(type, stack);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        EquipmentRing.removed(player, stack);
        super.onTake(player, stack);
    }
}
