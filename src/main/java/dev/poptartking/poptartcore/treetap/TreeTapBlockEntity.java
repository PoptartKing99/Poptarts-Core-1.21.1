package dev.poptartking.poptartcore.treetap;

import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import dev.poptartking.poptartcore.registry.PoptartCoreFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public final class TreeTapBlockEntity extends BlockEntity {
    public static final int CAPACITY = LatexBottleFluidHandler.AMOUNT * 5;

    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.is(PoptartCoreFluids.LATEX.source().get());
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return hasJungleBucket() ? super.fill(resource, action) : 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return hasJungleBucket() ? super.drain(resource, action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return hasJungleBucket() ? super.drain(maxDrain, action) : FluidStack.EMPTY;
        }

        @Override
        protected void onContentsChanged() {
            setChanged();
            syncFillLevel();
        }
    };

    public TreeTapBlockEntity(BlockPos pos, BlockState state) {
        super(PoptartCoreBlockEntities.TREE_TAP.get(), pos, state);
    }

    public IFluidHandler fluidHandler() {
        return tank;
    }

    public int latexAmount() {
        return tank.getFluidAmount();
    }

    public int collectLatexBottle() {
        return tank.drain(LatexBottleFluidHandler.AMOUNT, IFluidHandler.FluidAction.EXECUTE).getAmount();
    }

    private boolean hasJungleBucket() {
        BlockState state = getBlockState();
        return state.getBlock() instanceof TreeTapBlock
                && state.getValue(TreeTapBlock.WOOD) == TreeTapBlock.Wood.JUNGLE
                && state.getValue(TreeTapBlock.HAS_BUCKET);
    }

    private void syncFillLevel() {
        if (level == null || level.isClientSide || !hasJungleBucket()) return;
        BlockState state = getBlockState();
        int amount = tank.getFluidAmount();
        int visualLevel = Math.min(5, (amount + LatexBottleFluidHandler.AMOUNT - 1)
                / LatexBottleFluidHandler.AMOUNT);
        boolean wasFull = state.getValue(TreeTapBlock.FULL);
        BlockState updated = state.setValue(TreeTapBlock.FILL_LEVEL, visualLevel)
                .setValue(TreeTapBlock.FULL, amount == CAPACITY)
                .setValue(TreeTapBlock.DRIPPING, false);
        if (updated != state) level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
        if (wasFull && amount < CAPACITY) {
            level.scheduleTick(worldPosition, state.getBlock(), TreeTapBlock.FILL_CHECK_INTERVAL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!tank.isEmpty()) tag.put("Latex", tank.getFluid().save(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tank.setFluid(tag.contains("Latex")
                ? FluidStack.parseOptional(registries, tag.getCompound("Latex"))
                : FluidStack.EMPTY);
    }
}
