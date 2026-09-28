package dev.poptartking.poptartcore.rift;

import com.farcr.nomansland.common.block.torches.SconceWallTorchBlock;
import dev.poptartking.poptartcore.registry.PoptartCoreParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class RiftSconceWallTorchBlock extends SconceWallTorchBlock {
    public RiftSconceWallTorchBlock(Properties properties) {
        super(ParticleTypes.FLAME, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(FACING).getOpposite();
        double x = pos.getX() + 0.5 + 0.2 * facing.getStepX();
        double y = pos.getY() + 0.92;
        double z = pos.getZ() + 0.5 + 0.2 * facing.getStepZ();
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        level.addParticle(PoptartCoreParticles.RIFT_FIRE_FLAME.get(), x, y, z, 0, 0, 0);
    }
}
