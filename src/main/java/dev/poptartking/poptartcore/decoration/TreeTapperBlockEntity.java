package dev.poptartking.poptartcore.decoration;

import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import dev.poptartking.poptartcore.registry.PoptartCoreFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public final class TreeTapperBlockEntity extends BlockEntity {
    public static final int CAPACITY = 1000;
    public static final int FILL_AMOUNT = 10;
    private int remainingTicks;
    private int nextDripTicks;
    private final FluidTank tank =
            new FluidTank(
                    CAPACITY,
                    stack -> stack.is(PoptartCoreFluids.RESIN.source().get())
                            || stack.is(PoptartCoreFluids.MAPLE_SYRUP.source().get())) {
                @Override
                protected void onContentsChanged() {
                    sync();
                }
            };
    // Collectors expose drainage, never external filling or fluid mixing.
    private final IFluidHandler drainHandler = new IFluidHandler() {
        public int getTanks() {
            return 1;
        }

        public FluidStack getFluidInTank(int index) {
            return index == 0 ? tank.getFluid().copy() : FluidStack.EMPTY;
        }

        public int getTankCapacity(int index) {
            return index == 0 ? CAPACITY : 0;
        }

        public boolean isFluidValid(int index, FluidStack stack) {
            return false;
        }

        public int fill(FluidStack stack, FluidAction action) {
            return 0;
        }

        public FluidStack drain(FluidStack stack, FluidAction action) {
            return tank.drain(stack, action);
        }

        public FluidStack drain(int amount, FluidAction action) {
            return tank.drain(amount, action);
        }
    };

    public TreeTapperBlockEntity(BlockPos pos, BlockState state) {
        super(PoptartCoreBlockEntities.TREE_TAPPER.get(), pos, state);
    }

    public IFluidHandler drainHandler() {
        return drainHandler;
    }

    public FluidStack storedFluid() {
        return tank.getFluid().copy();
    }

    public int remainingTicks() {
        return remainingTicks;
    }

    public static int nextInterval(RandomSource random) {
        return 180 + random.nextInt(121);
    }

    public static float surfaceHeight(int amount) {
        return (2.01F + 11.49F * net.minecraft.util.Mth.clamp(amount / (float) CAPACITY, 0, 1)) / 16;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TreeTapperBlockEntity entity) {
        if (!state.getValue(WoodenBucketBlock.OPEN) || entity.tank.getFluidAmount() >= CAPACITY) return;
        entity.dripWhileCollecting(level, pos, state);
        if (entity.remainingTicks <= 0) {
            entity.remainingTicks = nextInterval(level.random);
            entity.setChanged();
        }
        if (--entity.remainingTicks > 0) return;
        var fluid = TapperTreeRules.fluidAt(level, pos.above().relative(state.getValue(WoodenBucketBlock.FACING)));
        if (fluid != Fluids.EMPTY) {
            entity.tank.fill(new FluidStack(fluid, FILL_AMOUNT), IFluidHandler.FluidAction.EXECUTE);
        }
        entity.remainingTicks = nextInterval(level.random);
        entity.setChanged();
    }

    private void dripWhileCollecting(Level level, BlockPos pos, BlockState state) {
        // Cosmetic timing is independent of the 10 mB production timer.
        if (nextDripTicks <= 0) nextDripTicks = TapperDripShape.FORM_TICKS + 15 + level.random.nextInt(41);
        if (--nextDripTicks > 0) return;
        nextDripTicks = TapperDripShape.FORM_TICKS + 15 + level.random.nextInt(41);
        var facing = state.getValue(WoodenBucketBlock.FACING);
        var fluid = TapperTreeRules.fluidAt(level, pos.above().relative(facing));
        if (fluid == Fluids.EMPTY
                || tank.fill(new FluidStack(fluid, FILL_AMOUNT), IFluidHandler.FluidAction.SIMULATE) == 0) return;
        if (level instanceof net.minecraft.server.level.ServerLevel server) {
            var particle = fluid == PoptartCoreFluids.RESIN.source().get()
                    ? dev.poptartking.poptartcore.registry.PoptartCoreParticles.TAPPER_RESIN_DRIP.get()
                    : dev.poptartking.poptartcore.registry.PoptartCoreParticles.TAPPER_SYRUP_DRIP.get();
            server.sendParticles(
                    particle,
                    pos.getX() + .5 + facing.getStepX() * .1875,
                    pos.getY() + .9995,
                    pos.getZ() + .5 + facing.getStepZ() * .1875,
                    1,
                    0,
                    0,
                    0,
                    0);
        }
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!tank.isEmpty()) tag.put("Fluid", tank.getFluid().save(registries));
        tag.putInt("RemainingTicks", remainingTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        var fluid = FluidStack.parseOptional(registries, tag.getCompound("Fluid"));
        tank.setFluid(
                !fluid.isEmpty() && tank.isFluidValid(fluid)
                        ? fluid.copyWithAmount(Math.min(CAPACITY, fluid.getAmount()))
                        : FluidStack.EMPTY);
        remainingTicks = net.minecraft.util.Mth.clamp(tag.getInt("RemainingTicks"), 0, 300);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        var tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
