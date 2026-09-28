package dev.poptartking.poptartcore.rift;

import com.farcr.nomansland.common.block.torches.SconceTorchBlock;
import dev.poptartking.poptartcore.registry.PoptartCoreParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RiftSconceTorchBlock extends SconceTorchBlock {
    public RiftSconceTorchBlock(Properties properties) {
        super(ParticleTypes.FLAME, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.8;
        double z = pos.getZ() + 0.5;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        level.addParticle(PoptartCoreParticles.RIFT_FIRE_FLAME.get(), x, y, z, 0, 0, 0);
    }
}
