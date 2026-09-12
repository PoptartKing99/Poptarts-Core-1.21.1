package dev.poptartking.poptartcore.quern;

import dev.poptartking.poptartcore.quern.recipe.GrindingRecipe;
import dev.poptartking.poptartcore.quern.recipe.GrindingRecipeInput;
import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import dev.poptartking.poptartcore.registry.PoptartCoreRecipes;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class QuernBlockEntity extends BlockEntity implements Container {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    private static final int CONTAINER_SIZE = 2;
    private static final int TICKS_PER_RECIPE_CRANK = 16;

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private int processingTicksRemaining;
    private int processingDuration;
    private int crankSequence;
    private int powderColor = GrindingRecipe.DEFAULT_POWDER_COLOR;
    private final RecipeManager.CachedCheck<GrindingRecipeInput, GrindingRecipe> recipeCheck =
            RecipeManager.createCheck(PoptartCoreRecipes.GRINDING_TYPE.get());

    private int observedCrankSequence = -1;
    private final QuernRotation rotation = new QuernRotation();

    public QuernBlockEntity(BlockPos pos, BlockState state) {
        super(PoptartCoreBlockEntities.QUERN.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, QuernBlockEntity quern) {
        if (level.isClientSide) {
            quern.rotation.tick();
            return;
        }
        if (quern.processingTicksRemaining <= 0) {
            return;
        }
        quern.processingTicksRemaining--;
        quern.markProcessingChanged();
        if (quern.processingTicksRemaining == 0) {
            quern.finishProcessing();
        }
    }

    public float rotation(float partialTick) {
        return rotation.angle(partialTick);
    }

    public boolean isRotating() {
        return rotation.isRotating();
    }

    public float flourFill() {
        return Math.min(1.0F, (float) getItem(OUTPUT_SLOT).getCount() / 64.0F);
    }

    public int powderColor() {
        return powderColor;
    }

    public boolean hasStoredItems() {
        return !getItem(OUTPUT_SLOT).isEmpty() || !getItem(INPUT_SLOT).isEmpty();
    }

    public boolean canInsert(ItemStack stack) {
        if (stack.isEmpty() || level == null || findRecipe(stack).isEmpty()) {
            return false;
        }
        ItemStack input = getItem(INPUT_SLOT);
        return input.isEmpty()
                || (ItemStack.isSameItemSameComponents(input, stack) && input.getCount() < input.getMaxStackSize());
    }

    public void insert(ItemStack heldStack, boolean creative) {
        if (!canInsert(heldStack)) {
            return;
        }
        ItemStack input = getItem(INPUT_SLOT);
        int room = (input.isEmpty() ? heldStack.getMaxStackSize() : input.getMaxStackSize() - input.getCount());
        int amount = Math.min(room, heldStack.getCount());
        if (input.isEmpty()) {
            stopProcessing();
            items.set(INPUT_SLOT, heldStack.copyWithCount(amount));
        } else {
            input.grow(amount);
        }
        if (!creative) {
            heldStack.shrink(amount);
        }
        setChanged();
    }

    public boolean canCrank() {
        if (level == null || processingTicksRemaining > 0) {
            return false;
        }
        Optional<RecipeHolder<GrindingRecipe>> recipe = findRecipe(getItem(INPUT_SLOT));
        return recipe.isPresent() && canFit(recipe.get().value().result());
    }

    public boolean crank() {
        if (level == null || level.isClientSide) {
            return false;
        }
        Optional<RecipeHolder<GrindingRecipe>> match = findRecipe(getItem(INPUT_SLOT));
        if (match.isEmpty() || !canFit(match.get().value().result())) {
            return false;
        }
        GrindingRecipe recipe = match.get().value();
        processingDuration = recipe.cranks() * TICKS_PER_RECIPE_CRANK;
        processingTicksRemaining = processingDuration;
        crankSequence++;
        setChanged();
        return true;
    }

    private void finishProcessing() {
        Optional<RecipeHolder<GrindingRecipe>> match = findRecipe(getItem(INPUT_SLOT));
        if (match.isPresent() && canFit(match.get().value().result())) {
            complete(match.get().value());
        } else {
            processingDuration = 0;
        }
        setChanged();
    }

    private void markProcessingChanged() {
        super.setChanged();
    }

    private void stopProcessing() {
        processingTicksRemaining = 0;
        processingDuration = 0;
    }

    private void complete(GrindingRecipe recipe) {
        ItemStack result = recipe.result().copy();
        ItemStack output = getItem(OUTPUT_SLOT);
        if (output.isEmpty()) {
            items.set(OUTPUT_SLOT, result);
        } else {
            output.grow(result.getCount());
        }
        powderColor = recipe.powderColor();
        getItem(INPUT_SLOT).shrink(1);
        processingDuration = 0;
    }

    private boolean canFit(ItemStack result) {
        ItemStack output = getItem(OUTPUT_SLOT);
        return output.isEmpty()
                ? result.getCount() <= result.getMaxStackSize()
                : ItemStack.isSameItemSameComponents(output, result)
                        && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private Optional<RecipeHolder<GrindingRecipe>> findRecipe(ItemStack stack) {
        return stack.isEmpty() || level == null
                ? Optional.empty()
                : recipeCheck.getRecipeFor(new GrindingRecipeInput(stack), level);
    }

    public boolean extractTo(Player player) {
        int slot = getItem(OUTPUT_SLOT).isEmpty() ? INPUT_SLOT : OUTPUT_SLOT;
        ItemStack stack = removeItemNoUpdate(slot);
        if (stack.isEmpty()) {
            return false;
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        setChanged();
        return true;
    }

    @Override
    public void setChanged() {
        super.setChanged();
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
        tag.putInt("ProcessingTicksRemaining", processingTicksRemaining);
        tag.putInt("ProcessingDuration", processingDuration);
        tag.putInt("CrankSequence", crankSequence);
        tag.putInt("PowderColor", powderColor);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        processingTicksRemaining = tag.getInt("ProcessingTicksRemaining");
        processingDuration = tag.getInt("ProcessingDuration");
        powderColor = tag.contains("PowderColor") ? tag.getInt("PowderColor") : GrindingRecipe.DEFAULT_POWDER_COLOR;
        int loadedSequence = tag.getInt("CrankSequence");
        if (level != null && level.isClientSide && processingTicksRemaining > 0) {
            if (observedCrankSequence >= 0 && loadedSequence > observedCrankSequence) {
                rotation.start(processingDuration);
            } else if (observedCrankSequence < 0) {
                rotation.resume(processingTicksRemaining, processingDuration);
            }
        }
        observedCrankSequence = loadedSequence;
        crankSequence = loadedSequence;
        items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
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
            if (slot == INPUT_SLOT && getItem(INPUT_SLOT).isEmpty()) {
                stopProcessing();
            }
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot == INPUT_SLOT) {
            stopProcessing();
        }
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == INPUT_SLOT && (stack.isEmpty() || !ItemStack.isSameItemSameComponents(getItem(slot), stack))) {
            stopProcessing();
        }
        stack.limitSize(getMaxStackSize(stack));
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == INPUT_SLOT && !stack.isEmpty() && findRecipe(stack).isPresent();
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return slot == OUTPUT_SLOT;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
        stopProcessing();
        setChanged();
    }
}
