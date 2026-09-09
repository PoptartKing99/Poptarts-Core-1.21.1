package dev.poptartking.poptartcore.workbench;

import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import dev.poptartking.poptartcore.workbench.menu.WorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WorkbenchBlockEntity extends BaseContainerBlockEntity {
    public static final int GRID_SIZE = 9;
    public static final int STORAGE_SIZE = 16;
    public static final int CONTAINER_SIZE = GRID_SIZE + STORAGE_SIZE;

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

    public WorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(PoptartCoreBlockEntities.WORKBENCH.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.poptartcore.workbench");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return new WorkbenchMenu(id, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }
}
