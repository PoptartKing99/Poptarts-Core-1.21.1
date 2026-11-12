package dev.poptartking.poptartcore.fruit;

import com.mojang.serialization.MapCodec;
import dev.poptartking.poptartcore.registry.PoptartFruitBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;

public final class FloweringLemonLeavesBlock extends LeavesBlock {
    public static final MapCodec<FloweringLemonLeavesBlock> CODEC = simpleCodec(FloweringLemonLeavesBlock::new);

    public FloweringLemonLeavesBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<FloweringLemonLeavesBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (decaying(state)) {
            super.randomTick(state, level, pos, random);
            return;
        }

        BlockPos fruitPos = pos.below();
        if (level.isOutsideBuildHeight(fruitPos) || level.getRawBrightness(fruitPos, 0) < 9) return;

        LemonFruitBlock fruit = PoptartFruitBlocks.LEMON_FRUIT.get();
        BlockState previous = level.getBlockState(fruitPos);
        BlockState next;
        if (previous.isAir()) {
            next = fruit.defaultBlockState();
        } else if (previous.is(fruit) && !LemonFruitBlock.isRipe(previous)) {
            next = previous.cycle(LemonFruitBlock.AGE);
        } else {
            return;
        }

        // As with No Man's Land's apple leaves, only one in seven random ticks succeeds.
        if (CommonHooks.canCropGrow(level, fruitPos, previous, random.nextInt(7) == 0)
                && level.setBlock(fruitPos, next, Block.UPDATE_ALL)) {
            CommonHooks.fireCropGrowPost(level, fruitPos, previous);
        }
    }
}
