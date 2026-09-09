package dev.poptartking.poptartcore.workbench.menu;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

public final class WorkbenchCraftingContainer implements CraftingContainer {
    private final WorkbenchMenu menu;
    private final Container backing;

    public WorkbenchCraftingContainer(WorkbenchMenu menu, Container backing) {
        this.menu = menu;
        this.backing = backing;
    }

    @Override
    public int getWidth() {
        return WorkbenchMenu.GRID_WIDTH;
    }

    @Override
    public int getHeight() {
        return WorkbenchMenu.GRID_HEIGHT;
    }

    @Override
    public int getContainerSize() {
        return WorkbenchMenu.GRID_SIZE;
    }

    @Override
    public List<ItemStack> getItems() {
        NonNullList<ItemStack> items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < getContainerSize(); slot++) {
            items.set(slot, backing.getItem(slot));
        }
        return items;
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < getContainerSize(); slot++) {
            if (!backing.getItem(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= getContainerSize() ? ItemStack.EMPTY : backing.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = backing.removeItem(slot, amount);
        if (!removed.isEmpty()) {
            menu.slotsChanged(this);
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = backing.getItem(slot);
        backing.setItem(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        backing.setItem(slot, stack);
        menu.slotsChanged(this);
    }

    @Override
    public void setChanged() {
        backing.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return backing.stillValid(player);
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < getContainerSize(); slot++) {
            backing.setItem(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public void fillStackedContents(StackedContents contents) {
        for (int slot = 0; slot < getContainerSize(); slot++) {
            contents.accountSimpleStack(backing.getItem(slot));
        }
    }
}
