package dev.poptartking.poptartcore.waxgolem;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/DepositResultGoal.class */
public class DepositResultGoal extends Goal {
    private static final int SEARCH = 16;
    private final WaxGolem golem;
    private BlockPos target;
    private BlockPos spot;
    private int patience;
    private int opened;
    private boolean open;

    public DepositResultGoal(WaxGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean canUse() {
        if (!this.golem.awake() || this.golem.result().isEmpty()) {
            return false;
        }
        this.target = DepositSites.best(this.golem, this.golem.result(), 16);
        if (this.target == null) {
            return false;
        }
        this.spot = standingSpot();
        return this.spot != null;
    }

    private BlockPos standingSpot() {
        if (HoneyCauldrons.acceptsBottle(this.golem.level(), this.target)) {
            return this.golem.standingSpotFor(this.target, place -> {
                return !place.equals(this.target);
            });
        }
        return ContainerAccess.standingSpotFor(this.golem, this.target);
    }

    public boolean canContinueToUse() {
        if (this.target == null || this.golem.result().isEmpty() || !this.golem.awake() || this.patience <= 0) {
            return false;
        }
        if (!this.golem.level().isLoaded(this.target)) {
            return true;
        }
        boolean intact = ContainerAccess.isContainer(this.golem.level(), this.target)
                || HoneyCauldrons.acceptsBottle(this.golem.level(), this.target);
        if (!intact) {
            this.target = null;
        }
        return intact;
    }

    public void start() {
        this.patience = 400;
        this.opened = 0;
        this.open = false;
        walk();
    }

    private void walk() {
        this.golem
                .getNavigation()
                .moveTo(
                        ((double) this.spot.getX()) + 0.5d,
                        this.spot.getY(),
                        ((double) this.spot.getZ()) + 0.5d,
                        0.65d);
    }

    public void stop() {
        close();
        this.target = null;
        this.spot = null;
        this.golem.getNavigation().stop();
    }

    private void close() {
        if (this.open) {
            ContainerAccess.setOpen(this.golem.level(), this.target, false);
            this.open = false;
        }
    }

    public void tick() {
        this.patience--;
        this.golem.getLookControl().setLookAt(Vec3.atCenterOf(this.target));
        if (!arrived()) {
            if (this.golem.getNavigation().isDone()) {
                walk();
                return;
            }
            return;
        }
        this.golem.getNavigation().stop();
        face();
        if (!this.open) {
            ContainerAccess.setOpen(this.golem.level(), this.target, true);
            this.open = true;
            this.opened = 30;
        } else {
            int i = this.opened - 1;
            this.opened = i;
            if (i > 0) {
                return;
            }
            deposit();
        }
    }

    private boolean arrived() {
        BlockPos standing = this.golem.blockPosition();
        return Math.abs(standing.getY() - this.spot.getY()) <= 1
                && Math.abs(standing.getX() - this.spot.getX()) <= 1
                && Math.abs(standing.getZ() - this.spot.getZ()) <= 1;
    }

    private void face() {
        Vec3 middle = Vec3.atCenterOf(this.target);
        double dx = middle.x - this.golem.getX();
        double dz = middle.z - this.golem.getZ();
        if ((dx * dx) + (dz * dz) > 1.0E-4d) {
            this.golem.setYRot(((float) (Math.atan2(dz, dx) * 57.29577951308232d)) - 90.0f);
            this.golem.yBodyRot = this.golem.getYRot();
        }
    }

    private void deposit() {
        ItemStack result = this.golem.result();
        this.golem.swing(InteractionHand.MAIN_HAND);
        if (DepositSites.insert(this.golem, this.target, result) && result.isEmpty()) {
            this.golem.setResult(ItemStack.EMPTY);
            this.golem.rest();
        }
        close();
        this.patience = 0;
    }
}
