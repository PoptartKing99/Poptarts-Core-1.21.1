package dev.poptartking.poptartcore.waxgolem;

import com.farcr.nomansland.common.block.cauldrons.FourLayeredCauldronBlock;
import com.farcr.nomansland.common.block.cauldrons.HoneyCauldron;
import com.farcr.nomansland.common.block.tap.TapBlock;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/HoneyCauldrons.class */
public final class HoneyCauldrons {
    public static final int MAX_LEVEL = 4;

    private HoneyCauldrons() {}

    public static boolean isHoney(BlockState state) {
        return state.getBlock() instanceof HoneyCauldron;
    }

    public static boolean isEmptyCauldron(BlockState state) {
        return state.is(Blocks.CAULDRON);
    }

    public static int level(BlockState state) {
        if (state.hasProperty(FourLayeredCauldronBlock.LEVEL)) {
            return ((Integer) state.getValue(FourLayeredCauldronBlock.LEVEL)).intValue();
        }
        return 0;
    }

    public static boolean acceptsBottle(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (isEmptyCauldron(state)) {
            return true;
        }
        return isHoney(state) && level(state) < 4;
    }

    public static boolean pourBottle(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (isEmptyCauldron(state)) {
            level.setBlock(
                    pos,
                    (BlockState) ((HoneyCauldron) NMLBlocks.HONEY_CAULDRON.get())
                            .defaultBlockState()
                            .setValue(FourLayeredCauldronBlock.LEVEL, 1),
                    3);
        } else if (isHoney(state) && level(state) < 4) {
            level.setBlock(
                    pos,
                    (BlockState) state.setValue(FourLayeredCauldronBlock.LEVEL, Integer.valueOf(level(state) + 1)),
                    3);
        } else {
            return false;
        }
        level.playSound((Player) null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(level.getBlockState(pos)));
        return true;
    }

    public static ItemStack drawBottle(Level level, BlockPos pos) {
        BlockState blockStateDefaultBlockState;
        BlockState state = level.getBlockState(pos);
        if (!isHoney(state) || level(state) <= 0) {
            return ItemStack.EMPTY;
        }
        int remaining = level(state) - 1;
        if (remaining <= 0) {
            blockStateDefaultBlockState = Blocks.CAULDRON.defaultBlockState();
        } else {
            blockStateDefaultBlockState =
                    (BlockState) state.setValue(FourLayeredCauldronBlock.LEVEL, Integer.valueOf(remaining));
        }
        level.setBlock(pos, blockStateDefaultBlockState, 3);
        level.playSound((Player) null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(level.getBlockState(pos)));
        return new ItemStack(Items.HONEY_BOTTLE);
    }

    public static boolean fedByHiveTap(Level level, BlockPos cauldron) {
        BlockPos fed;
        BlockState behind;
        for (BlockPos near : BlockPos.betweenClosed(cauldron.offset(-1, 0, -1), cauldron.offset(1, 3, 1))) {
            BlockState state = level.getBlockState(near);
            if ((state.getBlock() instanceof TapBlock)
                    && (fed = TapBlock.getCauldronPos(level, near)) != null
                    && fed.equals(cauldron)
                    && (behind = TapBlock.getBlockStateBehind(level, near, state)) != null
                    && ((behind.getBlock() instanceof BeehiveBlock) || behind.is(BlockTags.BEEHIVES))) {
                return true;
            }
        }
        return false;
    }
}
