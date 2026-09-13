package dev.poptartking.poptartcore.waxgolem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/ContainerAccess.class */
public final class ContainerAccess {
    public static final int OPEN_DELAY = 30;
    public static final int FACE_REACH = 2;

    private ContainerAccess() {}

    public static boolean isContainer(Level level, BlockPos pos) {
        return container(level, pos) != null;
    }

    public static Container container(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof ChestBlock chest) {
            return ChestBlock.getContainer(chest, state, level, pos, false);
        }
        if (!(block instanceof BarrelBlock)) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof Container)) {
            return null;
        }
        return (Container) blockEntity;
    }

    public static Direction accessSide(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BarrelBlock) {
            return state.getValue(BarrelBlock.FACING);
        }
        return Direction.UP;
    }

    public static Direction approachSide(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock) {
            return state.getValue(ChestBlock.FACING);
        }
        return accessSide(level, pos);
    }

    public static boolean unobstructed(Level level, BlockPos pos) {
        return clear(level, pos.relative(accessSide(level, pos)))
                && clear(level, pos.relative(approachSide(level, pos)));
    }

    private static boolean clear(Level level, BlockPos pos) {
        return !level.getBlockState(pos).isRedstoneConductor(level, pos) && container(level, pos) == null;
    }

    public static BlockPos standingSpotFor(WaxGolem golem, BlockPos pos) {
        if (!unobstructed(golem.level(), pos)) {
            return null;
        }
        Direction side = approachSide(golem.level(), pos);
        if (side == Direction.DOWN) {
            BlockPos spot = pos.below();
            if (golem.standable(spot)) {
                return spot;
            }
            return null;
        }
        if (side == Direction.UP) {
            return golem.standingSpotFor(pos, spot2 -> {
                return !spot2.equals(pos);
            });
        }
        return golem.standingSpotFor(pos, spot3 -> {
            return inFrontOf(golem, pos, spot3, side);
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean inFrontOf(WaxGolem golem, BlockPos pos, BlockPos spot, Direction side) {
        int iAbs;
        int along = ((spot.getX() - pos.getX()) * side.getStepX()) + ((spot.getZ() - pos.getZ()) * side.getStepZ());
        if (along < 1 || along > 2) {
            return false;
        }
        if (side.getAxis() == Direction.Axis.X) {
            iAbs = Math.abs(spot.getZ() - pos.getZ());
        } else {
            iAbs = Math.abs(spot.getX() - pos.getX());
        }
        int drift = iAbs;
        if (drift > 2) {
            return false;
        }
        BlockPos front = pos.relative(side);
        return !golem.level().getBlockState(front).isRedstoneConductor(golem.level(), front);
    }

    public static boolean reachable(WaxGolem golem, BlockPos pos) {
        return standingSpotFor(golem, pos) != null;
    }

    public static void setOpen(Level level, BlockPos pos, boolean opening) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if ((block instanceof BarrelBlock) && ((Boolean) state.getValue(BarrelBlock.OPEN)).booleanValue() != opening) {
            level.setBlock(pos, (BlockState) state.setValue(BarrelBlock.OPEN, Boolean.valueOf(opening)), 3);
        }
        level.blockEvent(pos, block, 1, opening ? 1 : 0);
        SoundEvent sound = sound(block, opening);
        if (sound != null) {
            level.playSound(
                    (Player) null,
                    ((double) pos.getX()) + 0.5d,
                    ((double) pos.getY()) + 0.5d,
                    ((double) pos.getZ()) + 0.5d,
                    sound,
                    SoundSource.BLOCKS,
                    0.5f,
                    (level.getRandom().nextFloat() * 0.1f) + 0.9f);
        }
    }

    private static SoundEvent sound(Block block, boolean opening) {
        if (block instanceof BarrelBlock) {
            return opening ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE;
        }
        if (block instanceof ChestBlock) {
            return opening ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE;
        }
        return null;
    }
}
