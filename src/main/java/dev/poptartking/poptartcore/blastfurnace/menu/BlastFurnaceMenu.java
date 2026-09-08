package dev.poptartking.poptartcore.blastfurnace.menu;

import dev.poptartking.poptartcore.crucible.menu.CrucibleContainerSlot;
import dev.poptartking.poptartcore.crucible.menu.CrucibleResultSlot;
import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import dev.poptartking.poptartcore.registry.PoptartCoreMenus;
import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

public class BlastFurnaceMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOTS = 9;
    private final Container container;
    private final ContainerData data;

    public BlastFurnaceMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(MACHINE_SLOTS), new SimpleContainerData(8));
    }

    public BlastFurnaceMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(PoptartCoreMenus.BLAST_FURNACE.get(), id);
        checkContainerSize(container, MACHINE_SLOTS);
        checkContainerDataCount(data, 8);
        this.container = container;
        this.data = data;
        addDataSlots(data);
        for (int row = 0; row < 2; row++)
            for (int col = 0; col < 3; col++) addSlot(new Slot(container, row * 3 + col, 17 + col * 18, 15 + row * 18));
        addSlot(new BlastFurnaceFuelSlot(container, 6, 35, 69));
        addSlot(new CrucibleContainerSlot(container, 7, 105, 61));
        addSlot(new CrucibleResultSlot(container, 8, 141, 44));
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 108 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 166));
    }

    public boolean isBurning() {
        return data.get(0) > 0;
    }

    public float getLitProgress() {
        int total = data.get(1);
        return Mth.clamp((float) data.get(0) / (total == 0 ? 200 : total), 0, 1);
    }

    public float getCookProgress() {
        int total = data.get(3);
        return total == 0 ? 0 : Mth.clamp((float) data.get(2) / total, 0, 1);
    }

    public Fluid getFluid() {
        return BuiltInRegistries.FLUID.byId(data.get(4));
    }

    public int getFluidAmount() {
        return data.get(5);
    }

    public ItemStack getMould() {
        return container.getItem(7);
    }

    public float getCastingProgress() {
        int total = data.get(7);
        return total == 0 ? 0 : Mth.clamp((float) data.get(6) / total, 0, 1);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < MACHINE_SLOTS) {
            if (!moveItemStackTo(stack, MACHINE_SLOTS, MACHINE_SLOTS + 36, true)) return ItemStack.EMPTY;
        } else if (PoptartCoreTags.isBlastFurnaceFuel(stack)) {
            if (!moveItemStackTo(stack, 6, 7, false)) return ItemStack.EMPTY;
        } else if (PoptartCoreItems.isMould(stack)) {
            if (!moveItemStackTo(stack, 7, 8, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, 6, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }
}
