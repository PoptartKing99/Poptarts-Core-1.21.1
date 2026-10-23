package dev.poptartking.poptartcore.treetap;

import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import dev.poptartking.poptartcore.registry.PoptartCoreFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public final class TreeTapBlockEntity extends BlockEntity {
    public static final int AMOUNT_PER_STAGE = 200;
    public static final int CAPACITY = AMOUNT_PER_STAGE * 5;
    public static final int AMOUNT_PER_DRIP = 10;
    public static final int DRIP_FRAME_COUNT = 10;
    public static final int DRIP_FRAME_TICKS = 2;
    private static final int DRIP_EVENT = 1;
    private int collectedAmount;
    private long dripStartedAt = Long.MIN_VALUE;

    // Latex exposes the common collected amount without owning a second tank.
    private final IFluidHandler latexHandler = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 && supportsLatex() && collectedAmount > 0
                    ? new FluidStack(PoptartCoreFluids.LATEX.source().get(), collectedAmount)
                    : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? CAPACITY : 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0
                    && supportsLatex()
                    && stack.is(PoptartCoreFluids.LATEX.source().get());
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!isFluidValid(0, resource)) return 0;
            int accepted = Math.min(resource.getAmount(), CAPACITY - collectedAmount);
            if (action.execute() && accepted > 0) setCollectedAmount(collectedAmount + accepted);
            return accepted;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return isFluidValid(0, resource) ? drain(resource.getAmount(), action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (!supportsLatex() || maxDrain <= 0 || collectedAmount == 0) return FluidStack.EMPTY;
            int extracted = Math.min(maxDrain, collectedAmount);
            if (action.execute()) setCollectedAmount(collectedAmount - extracted);
            return new FluidStack(PoptartCoreFluids.LATEX.source().get(), extracted);
        }
    };

    public TreeTapBlockEntity(BlockPos pos, BlockState state) {
        super(PoptartCoreBlockEntities.TREE_TAP.get(), pos, state);
        collectedAmount = legacyAmount(state);
    }

    public IFluidHandler fluidHandler() {
        return latexHandler;
    }

    public int collectedAmount() {
        return collectedAmount;
    }

    public boolean isFull() {
        return collectedAmount == CAPACITY;
    }

    public void addDrip() {
        if (!hasBucket() || isFull()) return;
        setCollectedAmount(collectedAmount + AMOUNT_PER_DRIP);
        if (level != null && !level.isClientSide) {
            dripStartedAt = level.getGameTime();
            level.blockEvent(worldPosition, getBlockState().getBlock(), DRIP_EVENT, 0);
        }
    }

    public static int frameAtAge(float age) {
        return age < 0 || age >= DRIP_FRAME_COUNT * DRIP_FRAME_TICKS ? -1 : (int) age / DRIP_FRAME_TICKS;
    }

    public int dripFrame(float partialTick) {
        if (level == null || dripStartedAt == Long.MIN_VALUE) return -1;
        return frameAtAge(level.getGameTime() - dripStartedAt + partialTick);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id != DRIP_EVENT) return super.triggerEvent(id, param);
        if (level != null && level.isClientSide) dripStartedAt = level.getGameTime();
        return true;
    }

    public boolean collectServing() {
        if (!hasBucket() || collectedAmount < AMOUNT_PER_STAGE) return false;
        setCollectedAmount(collectedAmount - AMOUNT_PER_STAGE);
        return true;
    }

    public int collectResin() {
        if (!hasBucket()
                || (getBlockState().getValue(TreeTapBlock.WOOD) != TreeTapBlock.Wood.SPRUCE
                        && getBlockState().getValue(TreeTapBlock.WOOD) != TreeTapBlock.Wood.PINE)) return 0;
        int count = collectedAmount / (AMOUNT_PER_STAGE / 2);
        if (count > 0) setCollectedAmount(collectedAmount - count * (AMOUNT_PER_STAGE / 2));
        return count;
    }

    public boolean supportsLatex() {
        return hasBucket() && getBlockState().getValue(TreeTapBlock.WOOD) == TreeTapBlock.Wood.JUNGLE;
    }

    private boolean hasBucket() {
        BlockState state = getBlockState();
        return state.getBlock() instanceof TreeTapBlock && state.getValue(TreeTapBlock.HAS_BUCKET);
    }

    private void setCollectedAmount(int amount) {
        collectedAmount = Mth.clamp(amount, 0, CAPACITY);
        setChanged();
        syncFillLevel();
    }

    private void syncFillLevel() {
        if (level == null || level.isClientSide || !hasBucket()) return;
        BlockState state = getBlockState();
        int visualLevel = (collectedAmount + AMOUNT_PER_STAGE - 1) / AMOUNT_PER_STAGE;
        boolean wasFull = state.getValue(TreeTapBlock.FULL);
        BlockState updated = state.setValue(TreeTapBlock.FILL_LEVEL, visualLevel)
                .setValue(TreeTapBlock.FULL, isFull())
                .setValue(TreeTapBlock.DRIPPING, false);
        if (updated != state) level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
        if (wasFull && !isFull()) {
            TreeTapBlock.scheduleNextFill(level, worldPosition);
        }
    }

    private static int legacyAmount(BlockState state) {
        return state.getValue(TreeTapBlock.HAS_BUCKET) ? state.getValue(TreeTapBlock.FILL_LEVEL) * AMOUNT_PER_STAGE : 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("CollectedAmount", collectedAmount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!hasBucket()) {
            collectedAmount = 0;
        } else if (tag.contains("CollectedAmount")) {
            collectedAmount = Mth.clamp(tag.getInt("CollectedAmount"), 0, CAPACITY);
        } else if (getBlockState().getValue(TreeTapBlock.WOOD) == TreeTapBlock.Wood.JUNGLE) {
            FluidStack legacy = FluidStack.parseOptional(registries, tag.getCompound("Latex"));
            collectedAmount =
                    legacy.is(PoptartCoreFluids.LATEX.source().get()) ? Math.min(CAPACITY, legacy.getAmount()) : 0;
        } else {
            collectedAmount = legacyAmount(getBlockState());
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        syncFillLevel();
    }
}
