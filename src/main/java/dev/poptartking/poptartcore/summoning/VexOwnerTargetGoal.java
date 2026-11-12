package dev.poptartking.poptartcore.summoning;

import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class VexOwnerTargetGoal extends TargetGoal {
    private static final double RANGE = 16.0;
    private final Vex vex;

    @Nullable
    private LivingEntity chosen;

    public VexOwnerTargetGoal(Vex vex) {
        super(vex, false);
        this.vex = vex;
    }

    @Nullable
    public static LivingEntity resolveOwner(Vex vex) {
        if (vex.level() instanceof ServerLevel server) {
            UUID ownerUuid = ((PixieData) vex.getData(PoptartCoreAttachments.VEX_DATA)).getOwnerUuid();
            if (ownerUuid == null) {
                return null;
            } else {
                Entity owner = PixieSummons.findEntity(server, ownerUuid);
                return owner instanceof LivingEntity living && owner.level() == server && living.isAlive()
                        ? living
                        : null;
            }
        } else {
            return null;
        }
    }

    @Nullable
    public static LivingEntity findTarget(Vex vex, LivingEntity owner) {
        LivingEntity ownerTarget = owner instanceof Mob ownerMob ? ownerMob.getTarget() : null;
        LivingEntity ownerVictim = owner.getLastHurtMob();
        AABB box = vex.getBoundingBox().inflate(RANGE);
        List<LivingEntity> candidates = vex.level()
                .getEntitiesOfClass(LivingEntity.class, box, e -> isValid(e, owner, ownerTarget, ownerVictim));
        LivingEntity best = null;
        boolean bestPriority = false;
        double bestDist = Double.MAX_VALUE;

        for (LivingEntity candidate : candidates) {
            boolean var17;
            label56:
            {
                label43:
                if (candidate != ownerTarget && candidate != ownerVictim) {
                    if (candidate instanceof Mob mob && mob.getTarget() == owner) {
                        break label43;
                    }

                    var17 = false;
                    break label56;
                }

                var17 = true;
            }

            boolean priority = var17;
            double dist = vex.distanceToSqr(candidate);
            if (best == null || priority && !bestPriority || priority == bestPriority && dist < bestDist) {
                best = candidate;
                bestPriority = priority;
                bestDist = dist;
            }
        }

        return best;
    }

    private static boolean isValid(
            LivingEntity entity,
            LivingEntity owner,
            @Nullable LivingEntity ownerTarget,
            @Nullable LivingEntity ownerVictim) {
        if (entity != owner && entity.isAlive()) {
            UUID allyOwner = pixieOwner(entity);
            if (allyOwner != null && allyOwner.equals(owner.getUUID())) {
                return false;
            } else if (entity != ownerTarget && entity != ownerVictim) {
                if (entity instanceof Mob mob && mob.getTarget() == owner) {
                    return true;
                }

                if (entity instanceof Player && !(owner instanceof Player)) {
                    return true;
                } else if (entity instanceof Enemy) {
                    return true;
                } else {
                    if (entity instanceof NeutralMob neutral && neutral.isAngryAt(owner)) {
                        return true;
                    }

                    return false;
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @Nullable
    private static UUID pixieOwner(LivingEntity entity) {
        if (entity instanceof Vex other) {
            return ((PixieData) other.getData(PoptartCoreAttachments.VEX_DATA)).getOwnerUuid();
        } else {
            return entity instanceof Allay other
                    ? ((AllayData) other.getData(PoptartCoreAttachments.ALLAY_DATA)).getOwnerUuid()
                    : null;
        }
    }

    public boolean canUse() {
        LivingEntity owner = resolveOwner(this.vex);
        if (owner == null) {
            return false;
        } else {
            this.chosen = findTarget(this.vex, owner);
            return this.chosen != null;
        }
    }

    public void start() {
        this.vex.setTarget(this.chosen);
        super.start();
    }

    public boolean canContinueToUse() {
        LivingEntity target = this.vex.getTarget();
        if (target != null && target.isAlive()) {
            LivingEntity owner = resolveOwner(this.vex);
            return owner != null && target != owner;
        } else {
            return false;
        }
    }

    public void stop() {
        this.chosen = null;
        super.stop();
    }
}
