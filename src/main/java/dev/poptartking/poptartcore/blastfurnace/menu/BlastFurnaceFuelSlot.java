package dev.poptartking.poptartcore.blastfurnace.menu;

import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BlastFurnaceFuelSlot extends Slot {
    public BlastFurnaceFuelSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(PoptartCoreTags.BLAST_FURNACE_ALLOWED);
    }
}
