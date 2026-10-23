package dev.poptartking.poptartcore.equipment;

import java.util.List;
import java.util.function.Function;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Creative inventory wrappers must keep the same list indices as the player's inventory menu. */
public final class CreativeSlotOrder {
    private CreativeSlotOrder() {}

    /** Invisible entry that still forwards synchronization to the real slot. */
    public static final class HiddenSlot extends Slot {
        public final Slot target;

        public HiddenSlot(Slot target) {
            super(target.container, target.getContainerSlot(), -2000, -2000);
            this.target = target;
            this.index = target.index;
        }

        @Override
        public ItemStack getItem() {
            return target.getItem();
        }

        @Override
        public void set(ItemStack stack) {
            target.set(stack);
        }

        @Override
        public void setByPlayer(ItemStack stack, ItemStack previous) {
            target.setByPlayer(stack, previous);
        }

        @Override
        public void setChanged() {
            target.setChanged();
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }

    public static boolean isSatchelSlot(Slot slot) {
        String name = slot.getClass().getName();
        return name.equals("net.vercte.satchels.content.satchel.SatchelInventorySlot")
                || name.equals("net.vercte.satchels.content.satchel.SatchelEquipmentSlot");
    }

    public static void restoreMissing(
            List<Slot> creative, List<Slot> inventory, Function<Slot, Slot> target, Function<Slot, Slot> wrap) {
        for (int index = 0; index < inventory.size(); index++) {
            Slot expected = inventory.get(index);
            if (index >= creative.size() || target.apply(creative.get(index)) != expected)
                creative.add(index, wrap.apply(expected));
        }
    }
}
