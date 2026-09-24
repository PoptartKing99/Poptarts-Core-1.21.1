package dev.poptartking.poptartcore.ingotpile;

import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class IngotPileBlockEntity extends BlockEntity {
    private final List<ItemStack> ingots = new ArrayList<>(64);

    public IngotPileBlockEntity(BlockPos pos, BlockState state) {
        super(PoptartCoreBlockEntities.INGOT_PILE.get(), pos, state);
    }

    public List<ItemStack> ingots() {
        return List.copyOf(ingots);
    }

    public int count() {
        return ingots.size();
    }

    public boolean add(ItemStack stack) {
        if (ingots.size() == 64 || !IngotPileMaterials.supports(stack)) return false;
        ingots.add(stack.copyWithCount(1));
        updateCount();
        return true;
    }

    public ItemStack removeLast() {
        if (ingots.isEmpty()) return ItemStack.EMPTY;
        ItemStack removed = ingots.removeLast();
        updateCount();
        return removed;
    }

    public List<ItemStack> takeAll() {
        List<ItemStack> removed = new ArrayList<>(ingots);
        ingots.clear();
        setChanged();
        return removed;
    }

    private void updateCount() {
        if (level != null && !level.isClientSide && !ingots.isEmpty()) {
            level.setBlock(worldPosition, getBlockState().setValue(IngotPileBlock.COUNT, ingots.size()), 3);
        }
        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (ItemStack stack : ingots) list.add(stack.save(registries));
        tag.put("Ingots", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ingots.clear();
        ListTag list = tag.getList("Ingots", 10);
        for (int i = 0; i < list.size() && ingots.size() < 64; i++) {
            ItemStack stack = ItemStack.parseOptional(registries, list.getCompound(i));
            if (IngotPileMaterials.supports(stack)) ingots.add(stack.copyWithCount(1));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
