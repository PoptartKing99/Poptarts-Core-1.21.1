package dev.poptartking.poptartcore.barrel;

import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class FluidBarrelBlockEntity extends BlockEntity {
    public static final int CAPACITY = 8000;

    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return !stack.is(PoptartCoreTags.BARREL_BLACKLISTED_FLUIDS);
        }

        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private BlockPos formationOrigin;
    private int formationSize = 1;
    private final IFluidHandler poolHandler = new PoolFluidHandler();

    public FluidBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(PoptartCoreBlockEntities.FLUID_BARREL.get(), pos, state);
        formationOrigin = pos.immutable();
    }

    public IFluidHandler fluidHandler() {
        return poolHandler;
    }

    public BlockPos formationOrigin() {
        return formationOrigin;
    }

    public int formationSize() {
        return formationSize;
    }

    public FluidStack storedFluid() {
        return tank.getFluid();
    }

    public void setFormation(BlockPos origin, int size) {
        formationOrigin = size == 1 ? worldPosition : origin.immutable();
        formationSize = size;
        setChanged();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide)
            level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
    }

    @Nullable
    private List<FluidBarrelBlockEntity> members() {
        if (formationSize == 1 || level == null) return List.of(this);

        List<FluidBarrelBlockEntity> result = new ArrayList<>(formationSize * formationSize * formationSize);
        FluidStack stored = FluidStack.EMPTY;
        for (int x = 0; x < formationSize; x++) {
            for (int y = 0; y < formationSize; y++) {
                for (int z = 0; z < formationSize; z++) {
                    BlockPos pos = formationOrigin.offset(x, y, z);
                    if (!level.hasChunkAt(pos)
                            || !(level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity barrel)
                            || barrel.formationSize != formationSize
                            || !barrel.formationOrigin.equals(formationOrigin)) return null;
                    FluidStack fluid = barrel.tank.getFluid();
                    if (!fluid.isEmpty()) {
                        if (!stored.isEmpty() && !FluidStack.isSameFluidSameComponents(stored, fluid)) return null;
                        stored = fluid;
                    }
                    result.add(barrel);
                }
            }
        }
        return result;
    }

    private FluidStack pooledFluid(List<FluidBarrelBlockEntity> members) {
        for (FluidBarrelBlockEntity member : members) {
            if (!member.tank.isEmpty()) return member.tank.getFluid();
        }
        return FluidStack.EMPTY;
    }

    private int pooledAmount(List<FluidBarrelBlockEntity> members) {
        int total = 0;
        for (FluidBarrelBlockEntity member : members) total += member.tank.getFluidAmount();
        return total;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!tank.isEmpty()) tag.put("Fluid", tank.getFluid().save(registries));
        if (formationSize > 1) {
            tag.putInt("FormationSize", formationSize);
            tag.putLong("FormationOrigin", formationOrigin.asLong());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tank.setFluid(tag.contains("Fluid")
                ? FluidStack.parseOptional(registries, tag.getCompound("Fluid"))
                : FluidStack.EMPTY);
        int size = tag.getInt("FormationSize");
        BlockPos origin = BlockPos.of(tag.getLong("FormationOrigin"));
        if (size >= 2 && size <= 3
                && worldPosition.getX() >= origin.getX() && worldPosition.getX() < origin.getX() + size
                && worldPosition.getY() >= origin.getY() && worldPosition.getY() < origin.getY() + size
                && worldPosition.getZ() >= origin.getZ() && worldPosition.getZ() < origin.getZ() + size) {
            formationOrigin = origin;
            formationSize = size;
        } else {
            formationOrigin = worldPosition;
            formationSize = 1;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private final class PoolFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int index) {
            List<FluidBarrelBlockEntity> current = members();
            if (index != 0 || current == null) return FluidStack.EMPTY;
            FluidStack fluid = pooledFluid(current);
            return fluid.isEmpty() ? FluidStack.EMPTY : fluid.copyWithAmount(pooledAmount(current));
        }

        @Override
        public int getTankCapacity(int index) {
            return index == 0 && members() != null ? formationSize * formationSize * formationSize * CAPACITY : 0;
        }

        @Override
        public boolean isFluidValid(int index, FluidStack stack) {
            return index == 0 && tank.isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack stack, FluidAction action) {
            List<FluidBarrelBlockEntity> current = members();
            if (current == null || stack.isEmpty() || !tank.isFluidValid(stack)) return 0;
            FluidStack existing = pooledFluid(current);
            if (!existing.isEmpty() && !FluidStack.isSameFluidSameComponents(existing, stack)) return 0;

            int filled = 0;
            for (FluidBarrelBlockEntity member : current) {
                int remaining = stack.getAmount() - filled;
                if (remaining <= 0) break;
                filled += member.tank.fill(stack.copyWithAmount(remaining), action);
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack stack, FluidAction action) {
            List<FluidBarrelBlockEntity> current = members();
            if (current == null || stack.isEmpty()
                    || !FluidStack.isSameFluidSameComponents(pooledFluid(current), stack)) return FluidStack.EMPTY;
            return drain(stack.getAmount(), action);
        }

        @Override
        public FluidStack drain(int amount, FluidAction action) {
            List<FluidBarrelBlockEntity> current = members();
            if (current == null || amount <= 0) return FluidStack.EMPTY;
            FluidStack result = FluidStack.EMPTY;
            for (FluidBarrelBlockEntity member : current) {
                int remaining = amount - result.getAmount();
                if (remaining <= 0) break;
                FluidStack drained = member.tank.drain(remaining, action);
                if (drained.isEmpty()) continue;
                if (result.isEmpty()) result = drained.copy();
                else result.grow(drained.getAmount());
            }
            return result;
        }
    }
}
