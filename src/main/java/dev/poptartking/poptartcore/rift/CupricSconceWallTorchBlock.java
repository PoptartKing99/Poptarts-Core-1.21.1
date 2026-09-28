package dev.poptartking.poptartcore.rift;

import com.farcr.nomansland.common.block.torches.SconceWallTorchBlock;
import com.teamabnormals.caverns_and_chasms.core.registry.CCParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CupricSconceWallTorchBlock extends SconceWallTorchBlock {
    public CupricSconceWallTorchBlock(Properties properties) {
        super(ParticleTypes.FLAME, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(FACING).getOpposite();
        double x = pos.getX() + 0.5 + 0.2 * facing.getStepX();
        double y = pos.getY() + 0.92;
        double z = pos.getZ() + 0.5 + 0.2 * facing.getStepZ();
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        level.addParticle(CCParticleTypes.CUPRIC_FIRE_FLAME.get(), x, y, z, 0, 0, 0);
    }
}
