package dev.poptartking.poptartcore.spider;

import dev.poptartking.poptartcore.mixin.spider.WallClimberNavigationAccessor;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import dev.poptartking.poptartcore.registry.PoptartCoreSounds;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.block.Blocks;

public final class SpiderWebAttackGoal extends Goal {
    private static final int WINDUP_TICKS = 60;
    private static final int SHOT_INTERVAL_TICKS = 60;
    // Dungeons Mobs uses squared distances, not distances of 90 and 180 blocks.
    private static final double STOP_CHASING_DISTANCE_SQUARED = 90;
    private static final double RESUME_CHASING_DISTANCE_SQUARED = 180;
    private final Spider spider;
    private final WebShootingSpider animation;
    private int windup;
    private long nextShotTime;
    private boolean chasing;
    private UUID preparingTarget;

    public SpiderWebAttackGoal(Spider spider) {
        this.spider = spider;
        animation = (WebShootingSpider) spider;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = spider.getTarget();
        if (spider.getType() != EntityType.SPIDER || spider.isVehicle() || target == null || !target.isAlive()) {
            return false;
        }
        double distance = spider.distanceToSqr(target);
        return distance > 9
                && !animation.poptartcore$isPursuingWebbedTarget(target)
                && target.level()
                        .getBlockStates(target.getBoundingBox().deflate(0.001))
                        .noneMatch(
                                state -> state.is(Blocks.COBWEB) || state.is(PoptartCoreBlocks.TEMPORARY_COBWEB.get()));
    }

    @Override
    public boolean canContinueToUse() {
        // Preserve the vanilla spider's chance to lose interest in bright light.
        if (spider.getLightLevelDependentMagicValue() >= 0.5F
                && spider.getRandom().nextInt(100) == 0) {
            spider.setTarget(null);
            return false;
        }
        return canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        chasing = true;
        LivingEntity target = spider.getTarget();
        preparingTarget = target == null ? null : target.getUUID();
        animation.poptartcore$setWebShooting(true);
        spider.playSound(PoptartCoreSounds.SPIDER_PREPARE_SHOOT.get(), 1, 1);
        windup = 0;
    }

    @Override
    public void stop() {
        animation.poptartcore$setWebShooting(false);
        windup = 0;
        preparingTarget = null;
    }

    @Override
    public void tick() {
        LivingEntity target = spider.getTarget();
        if (target == null || !canUse()) {
            stop();
            return;
        }
        if (!target.getUUID().equals(preparingTarget)) {
            preparingTarget = target.getUUID();
            windup = 0;
            // A new target gets a full warning; the existing shot cooldown stays intact.
            spider.playSound(PoptartCoreSounds.SPIDER_PREPARE_SHOOT.get(), 1, 1);
        }
        spider.getLookControl().setLookAt(target, 30, 30);
        double distance = spider.distanceToSqr(target);
        if (distance <= STOP_CHASING_DISTANCE_SQUARED) {
            chasing = false;
        } else if (distance >= RESUME_CHASING_DISTANCE_SQUARED) {
            chasing = true;
        }
        // Keep the previous movement decision between the two thresholds.
        if (chasing) {
            spider.getNavigation().moveTo(target, 1);
        } else {
            stopChasing();
        }
        if (++windup < WINDUP_TICKS || spider.level().getGameTime() < nextShotTime) {
            return;
        }
        windup = 0;
        nextShotTime = spider.level().getGameTime() + SHOT_INTERVAL_TICKS;
        if (!spider.getSensing().hasLineOfSight(target)) {
            return;
        }
        WebProjectile projectile = new WebProjectile(spider.level(), spider);
        float angle = spider.yBodyRot * Mth.DEG_TO_RAD;
        projectile.setPos(
                spider.getX() + Mth.sin(angle) * 0.75, spider.getY() + 1, spider.getZ() - Mth.cos(angle) * 0.75);
        double dx = target.getX() - projectile.getX();
        double dz = target.getZ() - projectile.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        projectile.shoot(
                dx,
                target.getY(1.0 / 3.0) - projectile.getY() + horizontal * 0.2,
                dz,
                Mth.clamp((float) horizontal * 0.25F, 1, 1.5F),
                2);
        spider.level().addFreshEntity(projectile);
        spider.playSound(PoptartCoreSounds.SPIDER_SHOOT.get(), 1, 1);
    }

    private void stopChasing() {
        spider.getNavigation().stop();
        // Wall-climbing navigation can keep chasing its fallback target even without a path.
        if (spider.getNavigation() instanceof WallClimberNavigationAccessor navigation) {
            navigation.poptartcore$setClimbingTarget(null);
        }
        spider.getMoveControl().setWantedPosition(spider.getX(), spider.getY(), spider.getZ(), 0);
        spider.setSpeed(0);
        spider.setXxa(0);
        spider.setZza(0);
    }
}
