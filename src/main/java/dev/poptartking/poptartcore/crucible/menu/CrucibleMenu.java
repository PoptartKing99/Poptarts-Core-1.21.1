package dev.poptartking.poptartcore.crucible.menu;

import dev.poptartking.poptartcore.crucible.CrucibleBlockEntity;
import dev.poptartking.poptartcore.registry.PoptartCoreMenus;
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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;

public class CrucibleMenu extends AbstractContainerMenu {

    private final Container container;
    private final ContainerData data;

    public CrucibleMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(6),
                new SimpleContainerData(6)
        );
    }

    public CrucibleMenu(
            int containerId,
            Inventory playerInventory,
            Container container,
            ContainerData data
    ) {
        super(PoptartCoreMenus.CRUCIBLE.get(), containerId);

        checkContainerSize(container, 6);
        checkContainerDataCount(data, 6);

        this.container = container;
        this.data = data;

        addDataSlots(data);

        for (int i = 0; i < 3; i++) {
            addSlot(
                    new Slot(
                            container,
                            i,
                            17 + i * 18,
                            1
                    )
            );
        }

        addSlot(
                new CrucibleFuelSlot(
                        container,
                        3,
                        35,
                        37
                )
        );

        addSlot(
                new CrucibleContainerSlot(
                        container,
                        4,
                        105,
                        37
                )
        );

        addSlot(
                new CrucibleResultSlot(
                        container,
                        5,
                        141,
                        20
                )
        );

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(
                        new Slot(
                                playerInventory,
                                column + row * 9 + 9,
                                8 + column * 18,
                                84 + row * 18
                        )
                );
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(
                    new Slot(
                            playerInventory,
                            column,
                            8 + column * 18,
                            142
                    )
            );
        }
    }

    public boolean isBurning() {
        return data.get(0) > 0;
    }

    public float getLitProgress() {
        int burnTime = data.get(0);
        int burnDuration = data.get(1);

        if (burnDuration == 0) {
            burnDuration = 200;
        }

        return Mth.clamp(
                (float) burnTime / burnDuration,
                0.0F,
                1.0F
        );
    }

    public float getCookProgress() {
        int cookTime = data.get(2);
        int cookTimeTotal = data.get(3);

        if (cookTimeTotal == 0) {
            return 0.0F;
        }

        return Mth.clamp(
                (float) cookTime / cookTimeTotal,
                0.0F,
                1.0F
        );
    }

    public Fluid getFluid() {
        return BuiltInRegistries.FLUID.byId(
                data.get(4)
        );
    }

    public int getFluidAmount() {
        return data.get(5);
    }

    public float getFluidProgress() {
        return Mth.clamp(
                (float) getFluidAmount()
                        / CrucibleBlockEntity.TANK_CAPACITY,
                0.0F,
                1.0F
        );
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(
            Player player,
            int index
    ) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (!slot.hasItem()) {
            return copy;
        }

        ItemStack stack = slot.getItem();
        copy = stack.copy();

        int playerInventoryStart = 6;
        int playerInventoryEnd = 42;

        if (index < 6) {
            if (!moveItemStackTo(
                    stack,
                    playerInventoryStart,
                    playerInventoryEnd,
                    true
            )) {
                return ItemStack.EMPTY;
            }
        } else {
            if (isFuel(stack)) {
                if (!moveItemStackTo(
                        stack,
                        3,
                        4,
                        false
                )) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!moveItemStackTo(
                        stack,
                        0,
                        3,
                        false
                )) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);

        return copy;
    }

    protected boolean isFuel(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING) > 0;
    }
}