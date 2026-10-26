package dev.poptartking.poptartcore.navigation;

import dev.poptartking.poptartcore.backpack.BackpackAttachments;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.equipment.UtilityWield;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CompassAccess {
    private CompassAccess() {}

    public static ItemStack carried(Player player, Item item) {
        var inv = AccessoryInventory.get(player);
        for (int i = 0; i < inv.getSlots(); i++) if (inv.getStackInSlot(i).is(item)) return inv.getStackInSlot(i);
        return ItemStack.EMPTY;
    }

    public static ItemStack attached(Player player, Item item) {
        var bag = BackpackAttachments.worn(player);
        for (int i = 1; i < BackpackAttachments.slots(BackpackAttachments.rucksack(bag)); i++) {
            var stack = BackpackAttachments.get(bag, i);
            if (stack.is(item)) return stack;
        }
        return ItemStack.EMPTY;
    }

    public static void mutate(ServerPlayer player, CompassSource source, Consumer<ItemStack> mutation) {
        // During G/Z use the working compass is authoritative. Committing its data here also updates the lease base.
        var working = UtilityWield.hand(player);
        boolean virtual = working != null
                && working.is(Items.COMPASS)
                && (source == CompassSource.ATTACHED && UtilityWield.isAttachmentWielding(player)
                        || source == CompassSource.CARRIED && !UtilityWield.isAttachmentWielding(player));
        if (virtual) {
            mutation.accept(working);
            UtilityWield.commit(player);
            return;
        }
        switch (source) {
            case HELD -> {
                var main = player.getMainHandItem();
                var held = main.is(Items.COMPASS) ? main : player.getOffhandItem();
                if (held.is(Items.COMPASS)) mutation.accept(held);
            }
            case CARRIED -> {
                var stack = carried(player, Items.COMPASS);
                if (!stack.isEmpty()) mutation.accept(stack);
            }
            case ATTACHED -> {
                var bag = BackpackAttachments.worn(player);
                int slot = BackpackAttachments.find(bag, "minecraft:compass");
                if (slot >= 1) {
                    var copy = BackpackAttachments.get(bag, slot).copy();
                    mutation.accept(copy);
                    BackpackAttachments.set(bag, slot, copy);
                }
            }
        }
    }

    /** Visit physical compasses and the active working compass once, committing nested item edits. */
    public static void mutateAll(ServerPlayer player, Consumer<ItemStack> mutation) {
        var working = UtilityWield.hand(player);
        boolean activeCompass = working != null && working.is(Items.COMPASS);
        if (activeCompass) {
            mutation.accept(working);
            UtilityWield.commit(player);
        }
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            var stack = player.getInventory().getItem(slot);
            if (stack.is(Items.COMPASS)) mutation.accept(stack);
        }
        var accessories = AccessoryInventory.get(player);
        for (int slot = 0; slot < accessories.getSlots(); slot++) {
            if (activeCompass && !UtilityWield.isAttachmentWielding(player) && slot == AccessoryInventory.UTILITY_SLOT)
                continue;
            var stack = accessories.getStackInSlot(slot);
            if (stack.is(Items.COMPASS)) mutation.accept(stack);
        }
        var bag = BackpackAttachments.worn(player);
        for (int slot = 1; slot < BackpackAttachments.slots(BackpackAttachments.rucksack(bag)); slot++) {
            if (activeCompass && UtilityWield.isAttachmentWielding(player) && slot == 1) continue;
            var stack = BackpackAttachments.get(bag, slot);
            if (!stack.is(Items.COMPASS)) continue;
            var copy = stack.copy();
            mutation.accept(copy);
            if (!ItemStack.matches(copy, stack)) BackpackAttachments.set(bag, slot, copy);
        }
    }

    public static void refreshCarried(ServerPlayer player) {
        var inv = AccessoryInventory.get(player);
        for (int i = 0; i < inv.getSlots(); i++)
            if (inv.getStackInSlot(i).is(Items.COMPASS)) {
                int slot = i;
                if (UtilityWield.isWielding(player)
                        && !UtilityWield.isAttachmentWielding(player)
                        && i == AccessoryInventory.UTILITY_SLOT)
                    mutate(player, CompassSource.CARRIED, c -> Waypoints.refresh(c, player));
                else Waypoints.refresh(inv.getStackInSlot(slot), player);
            }
    }

    public static void refreshAttached(ServerPlayer player) {
        var bag = BackpackAttachments.worn(player);
        for (int i = 1; i < BackpackAttachments.slots(BackpackAttachments.rucksack(bag)); i++) {
            var stack = BackpackAttachments.get(bag, i);
            if (!stack.is(Items.COMPASS)) continue;
            if (i == 1 && UtilityWield.isAttachmentWielding(player))
                mutate(player, CompassSource.ATTACHED, c -> Waypoints.refresh(c, player));
            else {
                var copy = stack.copy();
                if (Waypoints.refresh(copy, player)) BackpackAttachments.set(bag, i, copy);
            }
        }
    }
}
