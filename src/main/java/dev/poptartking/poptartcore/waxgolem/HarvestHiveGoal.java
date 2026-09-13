package dev.poptartking.poptartcore.waxgolem;

import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

/* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/HarvestHiveGoal.class */
public class HarvestHiveGoal extends Goal {
    private static final int PREFERRED_REACH = 1;
    private static final int HONEYCOMB_YIELD = 2;
    private final WaxGolem golem;
    private HiveMemory target;
    private int patience;
    private int stalls;

    public HarvestHiveGoal(WaxGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean canUse() {
        if (!this.golem.awake()
                || this.golem.state() != WaxGolemState.ACTIVE
                || !this.golem.canWork()
                || !this.golem.result().isEmpty()
                || this.golem.resting()) {
            return false;
        }
        this.target = pick();
        return this.target != null;
    }

    private HiveMemory pick() {
        int score;
        HiveMemory best = null;
        int bestScore = Integer.MIN_VALUE;
        long time = this.golem.level().getGameTime();
        Set<BlockPos> claimed = this.golem.foreignClaims();
        for (HiveMemory memory : this.golem.hives()) {
            if (memory.harvestable(time)
                    && !claimed.contains(memory.pos())
                    && (score = (rank(memory) * 64)
                                    - ((int) Math.sqrt(memory.pos().distSqr(this.golem.blockPosition()))))
                            > bestScore) {
                bestScore = score;
                best = memory;
            }
        }
        return best;
    }

    private static int rank(HiveMemory memory) {
        if (memory.ripe()) {
            return 3;
        }
        if (memory.honeyLevel() == -1) {
            return 2;
        }
        return memory.honeyLevel() >= 3 ? 1 : 0;
    }

    public boolean canContinueToUse() {
        if (this.target == null
                || !this.golem.awake()
                || this.golem.state() != WaxGolemState.ACTIVE
                || this.patience <= 0) {
            return false;
        }
        return stillThere();
    }

    private boolean stillThere() {
        BlockPos pos = this.target.pos();
        if (!this.golem.level().isLoaded(pos)
                || (this.golem.level().getBlockState(pos).getBlock() instanceof BeehiveBlock)) {
            return true;
        }
        this.golem.hives().remove(this.target);
        this.target = null;
        return false;
    }

    public void start() {
        this.patience = 400;
        this.stalls = 0;
        this.golem.setClaim(this.target.pos());
        wander();
    }

    public void stop() {
        this.golem.setClaim(null);
        this.golem.getNavigation().stop();
        this.target = null;
    }

    private void wander() {
        BlockPos spot = this.golem.harvestSpotFor(this.target.pos());
        if (spot == null) {
            this.target.setReachable(false);
            this.patience = 0;
        } else {
            double slack = this.golem.blockPosition().distSqr(spot) > 36.0d ? 1.5d : 0.0d;
            double x = ((double) spot.getX()) + 0.5d + (((this.golem.getRandom().nextDouble() * 2.0d) - 1.0d) * slack);
            double z = ((double) spot.getZ()) + 0.5d + (((this.golem.getRandom().nextDouble() * 2.0d) - 1.0d) * slack);
            this.golem.getNavigation().moveTo(x, spot.getY(), z, 0.8d);
        }
    }

    public void tick() {
        this.patience--;
        BlockPos pos = this.target.pos();
        this.golem.getLookControl().setLookAt(Vec3.atCenterOf(pos));
        if (!inReach(pos)) {
            if (this.golem.getNavigation().isDone()) {
                int i = this.stalls + 1;
                this.stalls = i;
                if (i > 3) {
                    this.target.setReachable(false);
                    this.patience = 0;
                    return;
                } else {
                    wander();
                    return;
                }
            }
            return;
        }
        if (!settled(pos)) {
            return;
        }
        this.golem.getNavigation().stop();
        face(pos);
        BlockState state = this.golem.level().getBlockState(pos);
        if (!(state.getBlock() instanceof BeehiveBlock)) {
            this.golem.hives().remove(this.target);
            this.target = null;
            return;
        }
        if (!this.golem.hiveOpen(pos)) {
            this.target.setReachable(false);
            this.patience = 0;
            return;
        }
        long time = this.golem.level().getGameTime();
        int honey = ((Integer) state.getValue(BeehiveBlock.HONEY_LEVEL)).intValue();
        this.target.seen(honey, time);
        this.target.sawSmoke(smoked(pos), time);
        this.target.setReachable(this.golem.reachable(pos));
        if (honey < 5 || !smoked(pos)) {
            this.patience = 0;
        } else {
            harvest(pos, state);
        }
    }

    private void dropAtHive(BlockPos pos, ItemStack yield) {
        double x = ((double) pos.getX()) + 0.5d;
        double y = ((double) pos.getY()) + 0.5d;
        double z = ((double) pos.getZ()) + 0.5d;
        ItemEntity drop = new ItemEntity(this.golem.level(), x, y, z, yield);
        drop.setDefaultPickUpDelay();
        drop.setDeltaMovement(
                this.golem.getRandom().nextGaussian() * 0.02d,
                0.1d,
                this.golem.getRandom().nextGaussian() * 0.02d);
        this.golem.level().addFreshEntity(drop);
    }

    private void face(BlockPos pos) {
        double dx = (((double) pos.getX()) + 0.5d) - this.golem.getX();
        double dz = (((double) pos.getZ()) + 0.5d) - this.golem.getZ();
        if ((dx * dx) + (dz * dz) > 1.0E-4d) {
            this.golem.setYRot(((float) (Math.atan2(dz, dx) * 57.29577951308232d)) - 90.0f);
            this.golem.yBodyRot = this.golem.getYRot();
        }
    }

    private boolean settled(BlockPos pos) {
        if (this.golem.getNavigation().isDone()) {
            return true;
        }
        BlockPos standing = this.golem.blockPosition();
        return Math.abs(pos.getX() - standing.getX()) <= 1 && Math.abs(pos.getZ() - standing.getZ()) <= 1;
    }

    private boolean inReach(BlockPos pos) {
        return this.golem.inReachOf(pos);
    }

    private boolean smoked(BlockPos pos) {
        return CampfireBlock.isSmokeyPos(this.golem.level(), pos);
    }

    private void harvest(BlockPos pos, BlockState state) {
        ItemStack yield;
        ItemStack tool = this.golem.tool();
        if (tool.getItem() instanceof ShearsItem) {
            yield = new ItemStack(Items.HONEYCOMB, 2);
            tool.hurtAndBreak(1, this.golem, EquipmentSlot.MAINHAND);
            this.golem
                    .level()
                    .playSound((Player) null, pos, SoundEvents.BEEHIVE_SHEAR, SoundSource.NEUTRAL, 1.0f, 1.0f);
        } else if (tool.is(Items.GLASS_BOTTLE)) {
            yield = new ItemStack(Items.HONEY_BOTTLE);
            tool.shrink(1);
            this.golem.level().playSound((Player) null, pos, SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0f, 1.0f);
        } else {
            return;
        }
        dropAtHive(pos, yield);
        this.golem.level().setBlock(pos, (BlockState) state.setValue(BeehiveBlock.HONEY_LEVEL, 0), 3);
        this.target.harvested(this.golem.level().getGameTime());
        this.golem.level().gameEvent(this.golem, GameEvent.SHEAR, pos);
        this.golem.spendLife(600);
        this.golem.swing(InteractionHand.MAIN_HAND);
        if (this.golem.tool().isEmpty()) {
            this.golem.setState(WaxGolemState.IDLE);
        }
        this.golem.rest();
        this.golem.setClaim(null);
        this.target = null;
    }
}
