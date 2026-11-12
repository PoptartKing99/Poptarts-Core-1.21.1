package dev.poptartking.poptartcore.summoning;

import dev.poptartking.poptartcore.registry.PoptartCoreSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class PhasingMover {
    private static final double ARRIVE_SQR = 0.36;
    private static final double MAX_MARCH = 6.0;
    private static final int PHASE_DELAY = 6;
    public static final Vector3f ALLAY_DUST_FROM = new Vector3f(0.42F, 0.79F, 0.93F);
    public static final Vector3f ALLAY_DUST_TO = new Vector3f(0.85F, 0.97F, 1.0F);
    public static final Vector3f VEX_DUST_FROM = new Vector3f(0.55F, 0.06F, 0.1F);
    public static final Vector3f VEX_DUST_TO = new Vector3f(0.16F, 0.0F, 0.02F);
    private static final DustColorTransitionOptions ALLAY_DUST =
            new DustColorTransitionOptions(ALLAY_DUST_FROM, ALLAY_DUST_TO, 1.2F);
    private static final DustColorTransitionOptions VEX_DUST =
            new DustColorTransitionOptions(VEX_DUST_FROM, VEX_DUST_TO, 1.2F);

    public static void moveToward(
            ServerLevel level, Mob mob, PhaseState phase, Vec3 target, double speed, PixieType type) {
        int state = phase.getState();
        if (state == 0) {
            PhasingMover.PhaseResult plan = planPhase(level, mob.position(), target);
            if (!plan.hasSolid()) {
                mob.noPhysics = false;
                mob.getMoveControl().setWantedPosition(target.x, target.y, target.z, speed);
            } else {
                phase.remember(mob);
                phase.setState(PhaseState.ENTERING);
                phase.setEntry(plan.entry());
                phase.setExit(plan.exit());
                phase.setDelay(PHASE_DELAY);
                mob.noPhysics = true;
                mob.getMoveControl().setWantedPosition(plan.entry().x, plan.entry().y, plan.entry().z, speed);
            }
        } else if (state != 1) {
            freeze(mob);
            int delay = phase.getDelay() - 1;
            phase.setDelay(delay);
            if (delay <= 0) {
                Vec3 exit = phase.getExit();
                if (exit != null) {
                    mob.moveTo(exit.x, exit.y, exit.z, mob.getYRot(), mob.getXRot());
                    mob.setDeltaMovement(Vec3.ZERO);
                    level.getChunkSource().broadcastAndSend(mob, new ClientboundTeleportEntityPacket(mob));
                    pixieBurst(level, exit.x, exit.y, exit.z, 14, type);
                    phaseSound(level, mob, exit.x, exit.y, exit.z, type);
                }

                endPhase(mob, phase);
                phase.setState(0);
                phase.setEntry(null);
                phase.setExit(null);
            }
        } else {
            mob.noPhysics = true;
            Vec3 entry = phase.getEntry();
            if (entry != null && !(mob.position().distanceToSqr(entry) < ARRIVE_SQR)) {
                mob.getMoveControl().setWantedPosition(entry.x, entry.y, entry.z, speed);
            } else {
                phase.setState(PhaseState.PHASING);
                Vec3 at = entry != null ? entry : mob.position();
                beginPhase(level, mob, at, type);
            }
        }
    }

    private static void beginPhase(ServerLevel level, Mob mob, Vec3 at, PixieType type) {
        pixieBurst(level, at.x, at.y, at.z, 14, type);
        phaseSound(level, mob, at.x, at.y, at.z, type);
        mob.setInvisible(true);
        mob.setInvulnerable(true);
        freeze(mob);
    }

    private static void freeze(Mob mob) {
        mob.noPhysics = true;
        mob.setNoGravity(true);
        mob.setDeltaMovement(Vec3.ZERO);
        mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0);
        mob.getNavigation().stop();
    }

    private static void endPhase(Mob mob, PhaseState phase) {
        phase.restore(mob);
    }

    public static void clearPhase(Mob mob, PhaseState phase) {
        if (phase.getState() != PhaseState.IDLE) phase.restore(mob);
    }

    public static PhasingMover.PhaseResult planPhase(BlockGetter level, Vec3 from, Vec3 to) {
        BlockHitResult clip =
                level.clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, CollisionContext.empty()));
        if (clip.getType() == Type.MISS) {
            return PhasingMover.PhaseResult.CLEAR;
        } else {
            Vec3 entry = clip.getLocation();
            Vec3 dir = to.subtract(from);
            if (dir.lengthSqr() < 1.0E-6) {
                return PhasingMover.PhaseResult.CLEAR;
            } else {
                dir = dir.normalize();
                boolean wasSolid = false;

                for (double t = 0.0; t <= MAX_MARCH; t += 0.25) {
                    Vec3 p = entry.add(dir.scale(t));
                    BlockPos bp = BlockPos.containing(p);
                    boolean solid = !level.getBlockState(bp)
                            .getCollisionShape(level, bp)
                            .isEmpty();
                    if (solid) {
                        wasSolid = true;
                    } else if (wasSolid) {
                        return new PhasingMover.PhaseResult(true, entry, Vec3.atCenterOf(bp));
                    }
                }

                return PhasingMover.PhaseResult.CLEAR;
            }
        }
    }

    public static void pixieBurst(ServerLevel level, double x, double y, double z, int count, PixieType type) {
        level.sendParticles(dust(type), x, y, z, count, 0.2, 0.2, 0.2, 0.0);
    }

    private static void phaseSound(ServerLevel level, Mob mob, double x, double y, double z, PixieType type) {
        float pitch = (type == PixieType.VEX ? 0.6F : 1.4F) + (mob.getRandom().nextFloat() - 0.5F) * 0.2F;
        level.playSound(null, x, y, z, PoptartCoreSounds.PIXIE_PHASE.get(), SoundSource.NEUTRAL, 0.5F, pitch);
    }

    private static DustColorTransitionOptions dust(PixieType type) {
        return type == PixieType.VEX ? VEX_DUST : ALLAY_DUST;
    }

    public static record PhaseResult(
            boolean hasSolid,
            @Nullable Vec3 entry,
            @Nullable Vec3 exit) {
        public static final PhasingMover.PhaseResult CLEAR = new PhasingMover.PhaseResult(false, null, null);
    }
}
