package dev.poptartking.poptartcore.rift;

import dev.poptartking.poptartcore.registry.PoptartCoreParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RiftWallTorchBlock extends WallTorchBlock {
    public RiftWallTorchBlock(Properties properties) {
        super(ParticleTypes.FLAME, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction direction = state.getValue(FACING).getOpposite();
        double x = pos.getX() + 0.5 + 0.27 * direction.getStepX();
        double y = pos.getY() + 0.92;
        double z = pos.getZ() + 0.5 + 0.27 * direction.getStepZ();
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        level.addParticle(PoptartCoreParticles.RIFT_FIRE_FLAME.get(), x, y, z, 0, 0, 0);
    }
}
