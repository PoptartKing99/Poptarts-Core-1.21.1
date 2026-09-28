package dev.poptartking.poptartcore.scribing;

import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ScribingTableBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOT_TOOLS = 0;
    public static final int CONTAINER_SIZE = 1;
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    public ScribingTableBlockEntity(BlockPos pos, BlockState state) {
        super(PoptartCoreBlockEntities.SCRIBING_TABLE.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("menu.title.poptartcore.scribing_table")
                .withStyle(style -> style.withColor(3022617));
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && stack.getItem() instanceof ScribingToolsItem;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            boolean hasTools = !this.getItem(0).isEmpty();
            BlockState state = this.getBlockState();
            if (state.hasProperty(ScribingTableBlock.TOOLS) && state.getValue(ScribingTableBlock.TOOLS) != hasTools) {
                this.level.setBlock(
                        this.worldPosition, state.setValue(ScribingTableBlock.TOOLS, Boolean.valueOf(hasTools)), 3);
            }
        }
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return new ScribingTableMenu(id, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
    }
}
