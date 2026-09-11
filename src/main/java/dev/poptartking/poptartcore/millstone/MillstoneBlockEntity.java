package dev.poptartking.poptartcore.millstone;

import dev.poptartking.poptartcore.millstone.client.MillstoneEffects;
import dev.poptartking.poptartcore.millstone.recipe.MillingRecipe;
import dev.poptartking.poptartcore.millstone.recipe.MillingRecipeInput;
import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import dev.poptartking.poptartcore.registry.PoptartCoreRecipes;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeManager.CachedCheck;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MillstoneBlockEntity extends BlockEntity {
    public static final int SLOTS_PER_BUFFER = 9;
    public static final int BUFFER_CAPACITY = 64;
    public static final float BASE_SPEED = 256.0F;
    private static final int SYNC_INTERVAL = 40;
    private static final int TRUST_TICKS = 60;
    private NonNullList<ItemStack> input = NonNullList.withSize(SLOTS_PER_BUFFER, ItemStack.EMPTY);
    private NonNullList<ItemStack> output = NonNullList.withSize(SLOTS_PER_BUFFER, ItemStack.EMPTY);
    private float progress;
    private ItemStack grindingStack = ItemStack.EMPTY;
    private long grindingSyncedAt = Long.MIN_VALUE;
    private final CachedCheck<MillingRecipeInput, MillingRecipe> quickCheck =
            RecipeManager.createCheck(PoptartCoreRecipes.MILLING_TYPE.get());

    public ItemStack getGrindingStack() {
        return this.grindingStack;
    }

    private void setGrindingStack(ItemStack stack) {
        if (!ItemStack.isSameItemSameComponents(this.grindingStack, stack)) {
            this.grindingStack = stack;
            this.setChanged();
            if (this.level != null && !this.level.isClientSide) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
    }

    public MillstoneBlockEntity(BlockPos pos, BlockState state) {
        super(PoptartCoreBlockEntities.MILLSTONE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MillstoneBlockEntity millstone) {
        if (!MillstoneStructure.isLoaded(level, pos)) {
            millstone.setGrindingStack(ItemStack.EMPTY);
            millstone.progress = 0;
            return;
        }
        float speed = millstone.rotorSpeed();
        if (speed > 0.0F && speed <= MillstoneRotorBlockEntity.SPEED_LIMIT) {
            int workSlot = -1;
            RecipeHolder<MillingRecipe> recipe = null;

            for (int slot = 0; slot < 9; slot++) {
                ItemStack stack = millstone.input.get(slot);
                if (!stack.isEmpty()) {
                    Optional<RecipeHolder<MillingRecipe>> match =
                            millstone.quickCheck.getRecipeFor(new MillingRecipeInput(stack), level);
                    if (match.isPresent() && millstone.canFitResult(match.get().value())) {
                        workSlot = slot;
                        recipe = match.get();
                        break;
                    }
                }
            }

            if (recipe == null) {
                millstone.setGrindingStack(ItemStack.EMPTY);
                if (millstone.progress != 0.0F) {
                    millstone.progress = 0.0F;
                    millstone.setChanged();
                }
            } else {
                ItemStack working = millstone.input.get(workSlot).copyWithCount(1);
                if (!ItemStack.isSameItemSameComponents(millstone.grindingStack, working)) {
                    millstone.progress = 0;
                }
                millstone.setGrindingStack(working);
                if (Math.floorMod(level.getGameTime() + pos.hashCode(), SYNC_INTERVAL) == 0) {
                    level.sendBlockUpdated(pos, state, state, 3);
                }

                millstone.progress += speed / BASE_SPEED;
                if (millstone.progress >= recipe.value().getGrindingTime()) {
                    millstone.complete(level, pos, workSlot, recipe.value());
                    millstone.progress = 0.0F;
                }

                millstone.setChanged();
            }
        } else {
            millstone.setGrindingStack(ItemStack.EMPTY);
            if (millstone.progress != 0.0F) {
                millstone.progress = 0.0F;
                millstone.setChanged();
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, MillstoneBlockEntity millstone) {
        BlockPos rotorPos = pos.offset(MillstoneStructure.ROTOR_OFFSET);
        if (level.getBlockEntity(rotorPos) instanceof MillstoneRotorBlockEntity rotor) {
            float speed = Math.abs(rotor.getSpeed());
            if (speed != 0.0F && !rotor.isOverspeed()) {
                MillstoneEffects.tick(level, pos, millstone.freshGrindingStack(level), speed);
            }
        }
    }

    private ItemStack freshGrindingStack(Level level) {
        if (this.grindingStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (this.grindingSyncedAt == Long.MIN_VALUE) {
            this.grindingSyncedAt = level.getGameTime();
        }

        return level.getGameTime() - this.grindingSyncedAt > TRUST_TICKS ? ItemStack.EMPTY : this.grindingStack;
    }

    private void complete(Level level, BlockPos pos, int slot, MillingRecipe recipe) {
        ItemStack result = recipe.result().copy();
        if (recipe.bonusCount() > 0
                && recipe.bonusChance() > 0.0F
                && level.getRandom().nextFloat() < recipe.bonusChance()) {
            result.grow(recipe.bonusCount());
        }

        (this.input.get(slot)).shrink(recipe.inputCount());
        this.insertResult(result);
    }

    public CompoundTag getUpdateTag(Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private float rotorSpeed() {
        if (this.level == null) {
            return 0.0F;
        }

        BlockPos rotorPos = this.worldPosition.offset(MillstoneStructure.ROTOR_OFFSET);
        return this.level.getBlockEntity(rotorPos) instanceof MillstoneRotorBlockEntity rotor
                ? Math.abs(rotor.getSpeed())
                : 0.0F;
    }

    public boolean acceptsItem(ItemStack stack) {
        if (this.level != null && !stack.isEmpty()) {
            ItemStack probe = stack.copyWithCount(stack.getMaxStackSize());
            return this.quickCheck
                    .getRecipeFor(new MillingRecipeInput(probe), this.level)
                    .isPresent();
        } else {
            return false;
        }
    }

    public int totalCount(boolean isInput) {
        NonNullList<ItemStack> buffer = isInput ? this.input : this.output;
        int total = 0;

        for (ItemStack stack : buffer) {
            total += stack.getCount();
        }

        return total;
    }

    public ItemStack getBufferStack(boolean isInput, int slot) {
        return (isInput ? this.input : this.output).get(slot);
    }

    public ItemStack insertInput(ItemStack stack, boolean simulate) {
        if (!this.acceptsItem(stack)) {
            return stack;
        }

        int room = BUFFER_CAPACITY - this.totalCount(true);
        if (room <= 0) {
            return stack;
        }

        int toInsert = Math.min(stack.getCount(), room);
        int remaining = toInsert;

        for (int slot = 0; slot < 9 && remaining > 0; slot++) {
            ItemStack existing = this.input.get(slot);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, stack)) {
                int space = Math.min(existing.getMaxStackSize(), 64) - existing.getCount();
                int moved = Math.min(space, remaining);
                if (moved > 0) {
                    if (!simulate) {
                        existing.grow(moved);
                    }

                    remaining -= moved;
                }
            }
        }

        for (int slot = 0; slot < 9 && remaining > 0; slot++) {
            if ((this.input.get(slot)).isEmpty()) {
                int moved = Math.min(stack.getMaxStackSize(), remaining);
                if (!simulate) {
                    this.input.set(slot, stack.copyWithCount(moved));
                }

                remaining -= moved;
            }
        }

        int inserted = toInsert - remaining;
        if (inserted <= 0) {
            return stack;
        }

        if (!simulate) {
            this.setChanged();
        }

        return stack.copyWithCount(stack.getCount() - inserted);
    }

    public ItemStack extractOutput(int slot, int amount, boolean simulate) {
        ItemStack existing = this.output.get(slot);
        if (!existing.isEmpty() && amount > 0) {
            int taken = Math.min(amount, existing.getCount());
            ItemStack result = existing.copyWithCount(taken);
            if (!simulate) {
                existing.shrink(taken);
                this.setChanged();
            }

            return result;
        } else {
            return ItemStack.EMPTY;
        }
    }

    private boolean canFitResult(MillingRecipe recipe) {
        int amount = recipe.maxResultCount();
        if (this.totalCount(false) + amount > BUFFER_CAPACITY) {
            return false;
        }

        ItemStack result = recipe.result();
        int remaining = amount;

        for (ItemStack existing : this.output) {
            if (remaining <= 0) {
                break;
            }

            if (existing.isEmpty()) {
                remaining -= Math.min(result.getMaxStackSize(), remaining);
            } else if (ItemStack.isSameItemSameComponents(existing, result)) {
                remaining -= Math.min(existing.getMaxStackSize() - existing.getCount(), remaining);
            }
        }

        return remaining <= 0;
    }

    private void insertResult(ItemStack result) {
        for (int slot = 0; slot < 9 && !result.isEmpty(); slot++) {
            ItemStack existing = this.output.get(slot);
            if (existing.isEmpty()) {
                int moved = Math.min(result.getMaxStackSize(), result.getCount());
                this.output.set(slot, result.split(moved));
            } else if (ItemStack.isSameItemSameComponents(existing, result)) {
                int moved = Math.min(existing.getMaxStackSize() - existing.getCount(), result.getCount());
                existing.grow(moved);
                result.shrink(moved);
            }
        }

        this.setChanged();
    }

    public ItemInteractionResult insertByHand(Player player, InteractionHand hand, ItemStack stack) {
        if (this.level == null || !this.acceptsItem(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (this.level.isClientSide) {
            return ItemInteractionResult.sidedSuccess(true);
        }

        ItemStack leftover = this.insertInput(stack, false);
        if (leftover.getCount() == stack.getCount()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!player.isCreative()) {
            player.setItemInHand(hand, leftover);
        }
        return ItemInteractionResult.sidedSuccess(false);
    }

    public InteractionResult extractByHand(Player player) {
        if (this.level == null) {
            return InteractionResult.PASS;
        }

        boolean hasOutput = this.totalCount(false) > 0;
        if (this.level.isClientSide) {
            return hasOutput ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        for (int slot = 8; slot >= 0; slot--) {
            ItemStack existing = this.output.get(slot);
            if (!existing.isEmpty()) {
                ItemStack taken = existing.copy();
                this.output.set(slot, ItemStack.EMPTY);
                this.setChanged();
                player.getInventory().placeItemBackInInventory(taken);
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    public void dropBuffers() {
        if (this.level != null) {
            for (ItemStack stack : this.input) {
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(
                            this.level,
                            this.worldPosition.getX() + 0.5,
                            this.worldPosition.getY() + 1.0,
                            this.worldPosition.getZ() + 0.5,
                            stack);
                }
            }

            for (ItemStack stack : this.output) {
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(
                            this.level,
                            this.worldPosition.getX() + 0.5,
                            this.worldPosition.getY() + 1.0,
                            this.worldPosition.getZ() + 0.5,
                            stack);
                }
            }

            this.input = NonNullList.withSize(9, ItemStack.EMPTY);
            this.output = NonNullList.withSize(9, ItemStack.EMPTY);
        }
    }

    protected void saveAdditional(CompoundTag tag, Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("Progress", this.progress);
        CompoundTag inputTag = new CompoundTag();
        ContainerHelper.saveAllItems(inputTag, this.input, registries);
        tag.put("Input", inputTag);
        CompoundTag outputTag = new CompoundTag();
        ContainerHelper.saveAllItems(outputTag, this.output, registries);
        tag.put("Output", outputTag);
        if (!this.grindingStack.isEmpty()) {
            tag.put("Grinding", this.grindingStack.save(registries));
        }
    }

    protected void loadAdditional(CompoundTag tag, Provider registries) {
        super.loadAdditional(tag, registries);
        this.progress = tag.getFloat("Progress");
        this.input = NonNullList.withSize(9, ItemStack.EMPTY);
        this.output = NonNullList.withSize(9, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag.getCompound("Input"), this.input, registries);
        ContainerHelper.loadAllItems(tag.getCompound("Output"), this.output, registries);
        ItemStack incoming = tag.contains("Grinding")
                ? ItemStack.parseOptional(registries, tag.getCompound("Grinding"))
                : ItemStack.EMPTY;
        this.grindingSyncedAt = this.level != null ? this.level.getGameTime() : Long.MIN_VALUE;

        this.grindingStack = incoming;
    }
}
