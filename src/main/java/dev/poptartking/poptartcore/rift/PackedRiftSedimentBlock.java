package dev.poptartking.poptartcore.rift;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PackedRiftSedimentBlock extends Block {
    public static final MapCodec<PackedRiftSedimentBlock> CODEC = simpleCodec(PackedRiftSedimentBlock::new);

    public PackedRiftSedimentBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(14) == 0) {
            level.addParticle(
                    ParticleTypes.PORTAL,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + 1.02,
                    pos.getZ() + random.nextDouble(),
                    (random.nextDouble() - 0.5) * 0.3,
                    random.nextDouble() * 0.08,
                    (random.nextDouble() - 0.5) * 0.3);
        }
    }
}
