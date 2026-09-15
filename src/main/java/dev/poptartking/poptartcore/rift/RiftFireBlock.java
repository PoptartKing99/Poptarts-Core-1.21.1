package dev.poptartking.poptartcore.rift;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RiftFireBlock extends BaseFireBlock {
    public static final MapCodec<RiftFireBlock> CODEC = simpleCodec(RiftFireBlock::new);
    private static final int CHARGE_TICKS = 55;

    public RiftFireBlock(Properties properties) {
        super(properties, 1.0F);
    }

    @Override
    protected MapCodec<? extends BaseFireBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean canBurn(BlockState state) {
        return false;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.is(state.getBlock())) {
            return;
        }
        if (!state.canSurvive(level, pos)) {
            level.removeBlock(pos, false);
            return;
        }
        if (level instanceof ServerLevel serverLevel && RiftPortalIgnition.canStart(serverLevel, pos)) {
            level.scheduleTick(pos, this, CHARGE_TICKS);
            level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 0.9F, 0.5F);
            level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.4F, 1.7F);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (RiftPortalIgnition.start(level, pos)) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.4;
        double z = pos.getZ() + 0.5;

        for (int index = 0; index < 2; index++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = 0.55 + random.nextDouble() * 0.45;
            double particleX = x + Math.cos(angle) * radius;
            double particleZ = z + Math.sin(angle) * radius;
            double particleY = y + random.nextDouble() * 1.1;
            double inward = 0.05;
            level.addParticle(
                    ParticleTypes.REVERSE_PORTAL,
                    particleX,
                    particleY,
                    particleZ,
                    (x - particleX) * inward,
                    0.01,
                    (z - particleZ) * inward);
        }

        level.addParticle(
                dev.poptartking.poptartcore.registry.PoptartCoreParticles.RIFT_FIRE_FLAME.get(),
                x + (random.nextDouble() - 0.5) * 0.5,
                y - 0.2 + random.nextDouble() * 0.4,
                z + (random.nextDouble() - 0.5) * 0.5,
                0.0,
                0.02 + random.nextDouble() * 0.03,
                0.0);
        if (random.nextInt(10) == 0) {
            level.addParticle(
                    ParticleTypes.PORTAL,
                    x,
                    y + 0.3,
                    z,
                    (random.nextDouble() - 0.5) * 0.6,
                    random.nextDouble() * 0.3,
                    (random.nextDouble() - 0.5) * 0.6);
        }
        if (random.nextInt(12) == 0) {
            level.playLocalSound(x, y, z, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.3F, 1.5F, false);
        }
        if (random.nextInt(24) == 0) {
            level.playLocalSound(
                    x,
                    y,
                    z,
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.BLOCKS,
                    0.6F,
                    0.5F + random.nextFloat() * 0.3F,
                    false);
        }
    }
}
