package dev.poptartking.poptartcore.bloomery;

import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BloomeryBlockEntity extends BlockEntity implements WorldlyContainer {
    public static final int IRON_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int MAX_FUEL = 9;
    private static final int MINUTE = 1200;
    private static final int[] NO_SLOTS = new int[0];

    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private int smeltingTime;
    private int smeltingProgress;

    public BloomeryBlockEntity(BlockPos pos, BlockState state) {
        super(PoptartCoreBlockEntities.BLOOMERY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BloomeryBlockEntity bloomery) {
        if (!bloomery.hasStarted()) {
            if (state.getValue(BloomeryBlock.LIT)) {
                bloomery.light(level.getRandom());
            }
            return;
        }

        if (!bloomery.isDone()) {
            bloomery.smeltingProgress++;
            bloomery.setChanged();
        }
        if (!bloomery.isDone()) {
            return;
        }

        boolean hadFuel = !bloomery.items.get(FUEL_SLOT).isEmpty();
        if (hadFuel) {
            bloomery.items.set(FUEL_SLOT, ItemStack.EMPTY);
        }
        if (state.getValue(BloomeryBlock.LIT)) {
            level.setBlockAndUpdate(pos, state.setValue(BloomeryBlock.LIT, false));
        }
        if (hadFuel) {
            bloomery.sync();
        }
    }

    public void light(RandomSource random) {
        smeltingTime = random.nextInt(4 * MINUTE, 6 * MINUTE);
        smeltingProgress = 0;
        sync();
    }

    public boolean canLight() {
        return !hasStarted() && !getItem(IRON_SLOT).isEmpty();
    }

    public boolean hasStarted() {
        return smeltingTime > 0;
    }

    public boolean isDone() {
        return hasStarted() && smeltingProgress >= smeltingTime;
    }

    public int bloomCount() {
        return getItem(IRON_SLOT).getCount();
    }

    private int slotFor(ItemStack stack) {
        if (stack.is(Items.RAW_IRON)) {
            return IRON_SLOT;
        }
        ItemStack fuel = getItem(FUEL_SLOT);
        return fuel.isEmpty()
                ? (stack.is(ItemTags.COALS) ? FUEL_SLOT : -1)
                : (stack.is(fuel.getItem()) ? FUEL_SLOT : -1);
    }

    public int placeableAmount(ItemStack stack) {
        if (hasStarted() || stack.isEmpty()) {
            return 0;
        }

        int slot = slotFor(stack);
        if (slot == -1) {
            return 0;
        }
        ItemStack existing = getItem(slot);
        if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) {
            return 0;
        }

        int maximum = slot == IRON_SLOT ? getItem(FUEL_SLOT).getCount() : MAX_FUEL;
        return Mth.clamp(maximum - existing.getCount(), 0, stack.getCount());
    }

    public boolean place(ItemStack stack) {
        int amount = placeableAmount(stack);
        if (amount <= 0) {
            return false;
        }

        int slot = slotFor(stack);
        ItemStack existing = getItem(slot);
        if (existing.isEmpty()) {
            items.set(slot, stack.copyWithCount(amount));
        } else {
            existing.grow(amount);
        }
        stack.shrink(amount);
        sync();
        return true;
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("SmeltingTime", smeltingTime);
        tag.putInt("SmeltingProgress", smeltingProgress);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        smeltingTime = tag.getInt("SmeltingTime");
        smeltingProgress = tag.getInt("SmeltingProgress");
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            sync();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        sync();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return false;
    }
}
