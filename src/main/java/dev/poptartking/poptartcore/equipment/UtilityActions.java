package dev.poptartking.poptartcore.equipment;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Storage and eligibility for the existing utility slot. */
public final class UtilityActions {
    private UtilityActions() {}

    public static ItemStack stack(Player player) {
        return AccessoryInventory.get(player).getStackInSlot(AccessoryInventory.UTILITY_SLOT);
    }

    public static void setStack(Player player, ItemStack stack) {
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.UTILITY_SLOT, stack);
    }

    public static boolean canWield(Player player) {
        var stack = stack(player);
        return player.isAlive()
                && !player.isSpectator()
                && AccessoryType.UTILITY.accepts(stack)
                && !stack.is(Items.TOTEM_OF_UNDYING);
    }
}
