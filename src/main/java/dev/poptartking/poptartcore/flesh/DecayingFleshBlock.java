package dev.poptartking.poptartcore.flesh;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;

public class DecayingFleshBlock extends Block {
    public static float DECAY_CHANCE = 0.1F;
    private final Supplier<Block> decaysInto;

    public DecayingFleshBlock(Supplier<Block> decaysInto, Properties properties) {
        super(properties);
        this.decaysInto = decaysInto;
    }

    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!FleshOrganisms.sustains(level, pos)) {
            float chance = DECAY_CHANCE;

            for (Direction direction : Direction.values()) {
                if (level.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) {
                    chance /= 2.0F;
                    break;
                }
            }

            if (random.nextFloat() < chance) {
                level.setBlockAndUpdate(pos, this.decaysInto.get().defaultBlockState());
                FleshGrowth.retintAround(level, pos);
            }
        }
    }
}
