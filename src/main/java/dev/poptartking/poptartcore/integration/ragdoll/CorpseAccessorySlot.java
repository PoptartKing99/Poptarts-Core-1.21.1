package dev.poptartking.poptartcore.integration.ragdoll;

import com.farcr.ragdoll.screen.CorpseSlot;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.equipment.AccessorySlotView;
import dev.poptartking.poptartcore.equipment.AccessoryType;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class CorpseAccessorySlot extends CorpseSlot implements AccessorySlotView {
    private final AccessoryType type;

    public CorpseAccessorySlot(
            Container container, int base, int index, boolean corpse, boolean record, Player player) {
        super(
                container,
                base + index,
                CorpseInventoryLayout.accessoryX(index),
                CorpseInventoryLayout.accessoryY(index),
                corpse,
                record,
                player);
        type = AccessoryInventory.type(index);
    }

    @Override
    public int getMaxStackSize() {
        return type == AccessoryType.UTILITY ? net.minecraft.world.item.Item.ABSOLUTE_MAX_STACK_SIZE : 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return AccessoryInventory.stackLimit(type, stack);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return type.accepts(stack) && super.mayPlace(stack);
    }

    @Override
    public AccessoryType accessoryType() {
        return type;
    }
}
