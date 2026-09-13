package dev.poptartking.poptartcore.waxgolem;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;

/* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/CollectDropsGoal.class */
public class CollectDropsGoal extends Goal {
    public static final double SEEK_RANGE = 5.0d;
    private final WaxGolem golem;
    private ItemEntity target;
    private int patience;

    public CollectDropsGoal(WaxGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean canUse() {
        if (!this.golem.awake() || this.golem.state() == WaxGolemState.DEPRESSED) {
            return false;
        }
        this.target = nearest();
        return this.target != null;
    }

    private ItemEntity nearest() {
        List<ItemEntity> drops = this.golem
                .level()
                .getEntitiesOfClass(
                        ItemEntity.class, this.golem.getBoundingBox().inflate(5.0d), item -> {
                            return item.isAlive() && !item.hasPickUpDelay() && this.golem.wantsToPickUp(item.getItem());
                        });
        return drops.stream()
                .min(Comparator.comparingDouble(this.golem::distanceToSqr))
                .orElse(null);
    }

    public boolean canContinueToUse() {
        return this.target != null
                && this.target.isAlive()
                && this.patience > 0
                && this.golem.wantsToPickUp(this.target.getItem())
                && this.golem.awake();
    }

    public void start() {
        this.patience = 120;
        this.golem.getNavigation().moveTo(this.target, 0.75d);
    }

    public void tick() {
        this.patience--;
        this.golem.getLookControl().setLookAt(this.target, 30.0f, 30.0f);
        if (this.golem.getNavigation().isDone() && this.golem.distanceToSqr(this.target) > 2.0d) {
            this.golem.getNavigation().moveTo(this.target, 0.75d);
        }
    }

    public void stop() {
        this.target = null;
        this.golem.getNavigation().stop();
    }
}
