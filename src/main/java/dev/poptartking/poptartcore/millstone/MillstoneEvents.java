package dev.poptartking.poptartcore.millstone;

import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.api.contraption.BlockMovementChecks.CheckResult;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import java.util.function.DoubleSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = "poptartcore")
public class MillstoneEvents {
    @SubscribeEvent
    public static void registerStress(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Block rotor = PoptartCoreBlocks.MILLSTONE_ROTOR.get();
            BlockStressValues.IMPACTS.register(rotor, (DoubleSupplier) () -> 16.0);
            TooltipModifier.REGISTRY.register((PoptartCoreBlocks.MILLSTONE.get()).asItem(), new KineticStats(rotor));
        });
    }

    @SubscribeEvent
    public static void registerContraptionBehaviour(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> BlockMovementChecks.registerAttachedCheck(MillstoneEvents::isAttachedToOwnMultiblock));
    }

    private static CheckResult isAttachedToOwnMultiblock(
            BlockState state, Level level, BlockPos pos, Direction direction) {
        BlockPos master = masterOf(level, pos, state);
        if (master == null) {
            return CheckResult.PASS;
        }

        BlockPos neighbourPos = pos.relative(direction);
        BlockPos neighbourMaster = masterOf(level, neighbourPos, level.getBlockState(neighbourPos));
        return master.equals(neighbourMaster) ? CheckResult.SUCCESS : CheckResult.PASS;
    }

    @Nullable
    public static BlockPos masterOf(BlockGetter level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof MillstoneBlock) {
            return pos;
        } else if (block instanceof MillstoneStructuralBlock) {
            return MillstoneStructuralBlock.getMaster(level, pos, state);
        } else if (block instanceof MillstoneRotorBlock) {
            BlockPos below = pos.below();
            return level.getBlockState(below).getBlock() instanceof MillstoneBlock ? below : null;
        } else {
            return null;
        }
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
                ItemHandler.BLOCK,
                (level, pos, state, be, side) -> resolve(level, pos, state),
                new Block[] {PoptartCoreBlocks.MILLSTONE.get(), PoptartCoreBlocks.MILLSTONE_STRUCTURAL.get()});
    }

    @Nullable
    private static MillstoneItemHandler resolve(Level level, BlockPos pos, BlockState state) {
        BlockPos master;
        if (state.getBlock() instanceof MillstoneBlock) {
            master = pos;
        } else {
            if (!(state.getBlock() instanceof MillstoneStructuralBlock)) {
                return null;
            }

            if (state.getValue(MillstoneStructuralBlock.TOP)) {
                return null;
            }

            master = MillstoneStructuralBlock.getMaster(level, pos, state);
        }

        return master != null && level.getBlockEntity(master) instanceof MillstoneBlockEntity millstone
                ? new MillstoneItemHandler(millstone)
                : null;
    }
}
