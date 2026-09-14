package dev.poptartking.poptartcore.spider;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class TemporaryCobwebBlock extends WebBlock {
    public static final int LIFETIME_TICKS = 100;
    public static final MapCodec<WebBlock> CODEC = simpleCodec(TemporaryCobwebBlock::new);

    public TemporaryCobwebBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<WebBlock> codec() {
        return CODEC;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, LIFETIME_TICKS);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof Spider)) {
            super.entityInside(state, level, pos, entity);
        }
    }
}
