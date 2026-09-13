package dev.poptartking.poptartcore.waxgolem;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/FerryHoneyGoal.class */
public class FerryHoneyGoal extends Goal {
    private static final int SEARCH = 12;
    private final WaxGolem golem;
    private BlockPos source;
    private int patience;

    public FerryHoneyGoal(WaxGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean canUse() {
        if (!this.golem.awake()
                || !this.golem.result().isEmpty()
                || this.golem.resting()
                || this.golem.state() == WaxGolemState.DEPRESSED
                || !this.golem.tool().is(Items.GLASS_BOTTLE)) {
            return false;
        }
        this.source = findTapped();
        return this.source != null;
    }

    private BlockPos findTapped() {
        BlockPos origin = this.golem.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-12, -4, -12), origin.offset(SEARCH, 4, SEARCH))) {
            if (HoneyCauldrons.isHoney(this.golem.level().getBlockState(pos))
                    && HoneyCauldrons.level(this.golem.level().getBlockState(pos)) > 0
                    && HoneyCauldrons.fedByHiveTap(this.golem.level(), pos)) {
                return pos.immutable();
            }
        }
        return null;
    }

    public boolean canContinueToUse() {
        if (this.source == null
                || !this.golem.awake()
                || this.patience <= 0
                || !this.golem.result().isEmpty()) {
            return false;
        }
        if (!this.golem.level().isLoaded(this.source)) {
            return true;
        }
        boolean intact = HoneyCauldrons.isHoney(this.golem.level().getBlockState(this.source))
                && HoneyCauldrons.level(this.golem.level().getBlockState(this.source)) > 0;
        if (!intact) {
            this.source = null;
        }
        return intact;
    }

    public void start() {
        this.patience = 300;
        this.golem
                .getNavigation()
                .moveTo(
                        ((double) this.source.getX()) + 0.5d,
                        this.source.getY(),
                        ((double) this.source.getZ()) + 0.5d,
                        0.6d);
    }

    public void tick() {
        this.patience--;
        this.golem.getLookControl().setLookAt(Vec3.atCenterOf(this.source));
        if (this.golem.blockPosition().distSqr(this.source) > 6.0d) {
            return;
        }
        if (!this.golem.tool().is(Items.GLASS_BOTTLE)) {
            this.patience = 0;
            return;
        }
        if (DepositSites.bestCauldron(this.golem, this.source) == null) {
            this.patience = 0;
            return;
        }
        ItemStack drawn = HoneyCauldrons.drawBottle(this.golem.level(), this.source);
        if (!drawn.isEmpty()) {
            this.golem.tool().shrink(1);
            this.golem.setDrawnFrom(this.source);
            this.golem.setResult(drawn);
            this.golem.swing(InteractionHand.OFF_HAND);
        }
        this.patience = 0;
    }
}
