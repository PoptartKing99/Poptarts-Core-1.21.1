package dev.poptartking.poptartcore.integration.create;

import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class BeltConnectorCost {
    private BeltConnectorCost() {}

    public static int length(BlockPos first, BlockPos last) {
        return Math.max(Math.max(Math.abs(first.getX() - last.getX()),
                Math.abs(first.getY() - last.getY())), Math.abs(first.getZ() - last.getZ())) + 1;
    }

    public static int available(Player player) {
        int count = 0;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof BeltConnectorItem) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static void consume(Player player, ItemStack held, int amount) {
        int fromHeld = Math.min(amount, held.getCount());
        held.shrink(fromHeld);
        int remaining = amount - fromHeld;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack != held && stack.getItem() instanceof BeltConnectorItem) {
                int taken = Math.min(remaining, stack.getCount());
                stack.shrink(taken);
                remaining -= taken;
            }
        }
    }
}
