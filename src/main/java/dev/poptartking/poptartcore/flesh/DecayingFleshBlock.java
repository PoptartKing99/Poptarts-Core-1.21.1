package dev.poptartking.poptartcore.flesh;

import dev.poptartking.poptartcore.registry.PoptartCorePoiTypes;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class DecayingFleshBlock extends Block {
    private static final float DECAY_CHANCE = 0.1F;
    private static final int LIFEBUD_PRESERVATION_RANGE = 16;
    private final Supplier<Block> decaysInto;

    public DecayingFleshBlock(Supplier<Block> decaysInto, Properties properties) {
        super(properties);
        this.decaysInto = decaysInto;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getPoiManager()
                .findClosest(
                        holder -> holder.is(PoptartCorePoiTypes.LIFEBUD.getKey()),
                        pos,
                        LIFEBUD_PRESERVATION_RANGE,
                        Occupancy.ANY)
                .isPresent()) {
            return;
        }

        float chance = DECAY_CHANCE;
        for (Direction direction : Direction.values()) {
            if (level.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) {
                chance /= 2.0F;
                break;
            }
        }
        if (random.nextFloat() < chance) {
            level.setBlockAndUpdate(pos, decaysInto.get().defaultBlockState());
        }
    }
}
