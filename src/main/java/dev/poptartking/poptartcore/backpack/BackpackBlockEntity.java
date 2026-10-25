package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
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

public final class BackpackBlockEntity extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items;
    private net.minecraft.world.item.component.DyedItemColor dye;
    private Integer bodyDye;
    private net.minecraft.world.item.component.ItemContainerContents attachments =
            net.minecraft.world.item.component.ItemContainerContents.EMPTY;

    public net.minecraft.world.item.component.ItemContainerContents attachments() {
        return attachments;
    }

    public void setAttachments(net.minecraft.world.item.component.ItemContainerContents contents) {
        attachments = contents;
        setChanged();
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

    public int dyeColor(int index) {
        return index == 1 ? (bodyDye == null ? 0xFFFFFF : bodyDye) : dyeColor();
    }

    public void setColor(BackpackColors.Part part, int rgb) {
        if (part == BackpackColors.Part.BODY) bodyDye = rgb;
        else dye = new net.minecraft.world.item.component.DyedItemColor(rgb, false);
        setChanged();
        if (level != null) {
            var old = getBlockState();
            var state =
                    old.setValue(BackpackBlock.DYED, dye != null).setValue(BackpackBlock.BODY_DYED, bodyDye != null);
            level.setBlock(worldPosition, state, 3);
            level.sendBlockUpdated(worldPosition, old, state, 3);
        }
    }

    public int dyeColor() {
        return dye == null ? 0xFFFFFF : dye.rgb();
    }

    @Override
    protected void applyImplicitComponents(
            net.minecraft.world.level.block.entity.BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        dye = input.get(net.minecraft.core.component.DataComponents.DYED_COLOR);
        bodyDye = input.get(BackpackColors.BODY.get());
        attachments = input.getOrDefault(
                BackpackAttachments.CONTENTS.get(), net.minecraft.world.item.component.ItemContainerContents.EMPTY);
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

    @Override
    protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (dye != null) builder.set(net.minecraft.core.component.DataComponents.DYED_COLOR, dye);
        if (bodyDye != null) builder.set(BackpackColors.BODY.get(), bodyDye);
        builder.set(BackpackAttachments.CONTENTS.get(), attachments);
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var tag = new CompoundTag();
        if (dye != null) tag.putInt("FlapDye", dye.rgb());
        if (bodyDye != null) tag.putInt("BodyDye", bodyDye);
        saveAttachments(tag, registries);
        return tag;
    }

    @Override
    public void onDataPacket(
            net.minecraft.network.Connection connection,
            net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket packet,
            HolderLookup.Provider registries) {
        handleUpdateTag(packet.getTag(), registries);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadColors(tag);
        loadAttachments(tag, registries);
    }

    private void saveAttachments(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put(
                "Attachments",
                net.minecraft.world.item.component.ItemContainerContents.CODEC
                        .encodeStart(
                                registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), attachments)
                        .getOrThrow());
    }

    private void loadAttachments(CompoundTag tag, HolderLookup.Provider registries) {
        attachments = tag.contains("Attachments")
                ? net.minecraft.world.item.component.ItemContainerContents.CODEC
                        .parse(
                                registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),
                                tag.get("Attachments"))
                        .result()
                        .orElse(net.minecraft.world.item.component.ItemContainerContents.EMPTY)
                : net.minecraft.world.item.component.ItemContainerContents.EMPTY;
    }

    private void loadColors(CompoundTag tag) {
        bodyDye = tag.contains("BodyDye", net.minecraft.nbt.Tag.TAG_INT) ? tag.getInt("BodyDye") : null;
        dye = tag.contains("FlapDye", net.minecraft.nbt.Tag.TAG_INT)
                ? new net.minecraft.world.item.component.DyedItemColor(tag.getInt("FlapDye"), false)
                : null;
    }

    public BackpackBlockEntity(BlockPos pos, BlockState state) {
        super(PoptartCoreBlockEntities.BACKPACK.get(), pos, state);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    protected Component getDefaultName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public int getContainerSize() {
        return ((BackpackBlock) getBlockState().getBlock()).rows() * 9;
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
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return BackpackMenu.canStore(stack);
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new BackpackMenu(id, inventory, this, ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        saveAttachments(tag, registries);
        if (dye != null) tag.putInt("FlapDye", dye.rgb());
        if (bodyDye != null) tag.putInt("BodyDye", bodyDye);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        loadAttachments(tag, registries);
        loadColors(tag);
    }
}
