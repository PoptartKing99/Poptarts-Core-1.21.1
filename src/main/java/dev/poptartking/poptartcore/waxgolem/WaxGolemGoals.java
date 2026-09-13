package dev.poptartking.poptartcore.waxgolem;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/WaxGolemGoals.class */
public final class WaxGolemGoals {
    private WaxGolemGoals() {}

    /* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/WaxGolemGoals$Base.class */
    public abstract static class Base extends Goal {
        protected final WaxGolem golem;

        protected Base(WaxGolem golem) {
            this.golem = golem;
        }

        protected boolean idle() {
            if (!this.golem.awake()) {
                return false;
            }
            if (this.golem.state() == WaxGolemState.IDLE) {
                return true;
            }
            return this.golem.state() == WaxGolemState.ACTIVE && !this.golem.hasWork();
        }
    }

    /* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/WaxGolemGoals$WatchWatcher.class */
    public static class WatchWatcher extends Base {
        public WatchWatcher(WaxGolem golem) {
            super(golem);
            setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return this.golem.awake() && this.golem.beingWatched();
        }

        public boolean canContinueToUse() {
            return canUse();
        }

        public void tick() {
            Player watcher = this.golem.watcher();
            if (watcher != null) {
                this.golem.getNavigation().stop();
                this.golem.getLookControl().setLookAt(watcher, 30.0f, 30.0f);
            }
        }
    }

    /* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/WaxGolemGoals$Sit.class */
    public static class Sit extends Base {
        private int duration;

        public Sit(WaxGolem golem) {
            super(golem);
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        }

        public boolean canUse() {
            return idle() && !this.golem.sitting() && this.golem.getRandom().nextInt(600) == 0;
        }

        public boolean canContinueToUse() {
            return idle() && this.duration > 0;
        }

        public void start() {
            this.duration = 200 + this.golem.getRandom().nextInt(600);
            this.golem.getNavigation().stop();
            this.golem.setSitting(true);
        }

        public void tick() {
            this.duration--;
        }

        public void stop() {
            this.golem.setSitting(false);
        }
    }

    /* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/WaxGolemGoals$StareAt.class */
    public static class StareAt extends Base {
        private final Class<? extends Entity> type;
        private final int chance;
        private Entity target;
        private int duration;

        public StareAt(WaxGolem golem, Class<? extends Entity> type, int chance) {
            super(golem);
            this.type = type;
            this.chance = chance;
            setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        public boolean canUse() {
            if (!idle() || this.golem.getRandom().nextInt(this.chance) != 0) {
                return false;
            }
            List<? extends Entity> nearby = this.golem
                    .level()
                    .getEntitiesOfClass(this.type, this.golem.getBoundingBox().inflate(10.0d), candidate -> {
                        return ((candidate instanceof Bee)
                                        || (candidate instanceof WaxGolem)
                                        || !(candidate instanceof LivingEntity))
                                ? false
                                : true;
                    });
            if (nearby.isEmpty()) {
                return false;
            }
            this.target = (Entity) nearby.get(this.golem.getRandom().nextInt(nearby.size()));
            return true;
        }

        public boolean canContinueToUse() {
            return idle() && this.target != null && this.target.isAlive() && this.duration > 0;
        }

        public void start() {
            this.duration = 60 + this.golem.getRandom().nextInt(120);
        }

        public void tick() {
            this.duration--;
            this.golem.getLookControl().setLookAt(this.target, 20.0f, 20.0f);
        }
    }

    /* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/WaxGolemGoals$StareAtBlock.class */
    public static class StareAtBlock extends Base {
        private final Kind kind;
        private final int chance;
        private BlockPos target;
        private int duration;

        /* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/WaxGolemGoals$StareAtBlock$Kind.class */
        public enum Kind {
            FLOWER,
            GLASS,
            CANDLE,
            HIVE
        }

        public StareAtBlock(WaxGolem golem, Kind kind, int chance) {
            super(golem);
            this.kind = kind;
            this.chance = chance;
            setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        public boolean canUse() {
            if (!idle() || this.golem.getRandom().nextInt(this.chance) != 0) {
                return false;
            }
            this.target = search();
            return this.target != null;
        }

        private BlockPos search() {
            BlockPos origin = this.golem.blockPosition();
            int range = this.kind == Kind.HIVE ? 12 : 8;
            BlockPos found = null;
            for (BlockPos pos :
                    BlockPos.betweenClosed(origin.offset(-range, -3, -range), origin.offset(range, 3, range))) {
                BlockState state = this.golem.level().getBlockState(pos);
                if (matches(state, pos, origin)
                        && worthVisiting(pos)
                        && (found == null || this.golem.getRandom().nextInt(4) == 0)) {
                    found = pos.immutable();
                }
            }
            return found;
        }

        private boolean matches(BlockState state, BlockPos pos, BlockPos origin) {
            return switch (this.kind) {
                case FLOWER -> state.is(BlockTags.FLOWERS);
                case GLASS ->
                    pos.getY() == origin.getY() + 1 && (state.is(BlockTags.IMPERMEABLE) || state.is(Blocks.GLASS_PANE));
                case CANDLE -> state.getBlock() instanceof CandleBlock;
                case HIVE -> state.getBlock() instanceof BeehiveBlock;
            };
        }

        public boolean canContinueToUse() {
            if (!idle() || this.target == null || this.duration <= 0) {
                return false;
            }
            if (!this.golem.level().isLoaded(this.target)
                    || matches(
                            this.golem.level().getBlockState(this.target), this.target, this.golem.blockPosition())) {
                return true;
            }
            if (this.kind == Kind.HIVE) {
                this.golem.hives().removeIf(memory -> {
                    return memory.pos().equals(this.target);
                });
            }
            this.target = null;
            return false;
        }

        public void start() {
            this.duration = 60 + this.golem.getRandom().nextInt(WaxGolem.REST_MIN);
            if (this.kind == Kind.HIVE) {
                this.golem.hive(this.target);
            }
        }

        private boolean worthVisiting(BlockPos pos) {
            return (this.kind == Kind.HIVE && !this.golem.reachable(pos) && this.golem.anyReachableDue())
                    ? false
                    : true;
        }

        public void tick() {
            this.duration--;
            this.golem.getLookControl().setLookAt(Vec3.atCenterOf(this.target));
            if (this.kind == Kind.HIVE
                    && this.duration % 20 == 0
                    && this.golem.blockPosition().distSqr(this.target) <= 64.0d) {
                this.golem.observeHive(this.target);
            }
        }
    }

    /* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/WaxGolemGoals$SitNearFriend.class */
    public static class SitNearFriend extends Base {
        private WaxGolem friend;
        private int duration;

        public SitNearFriend(WaxGolem golem) {
            super(golem);
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        public boolean canUse() {
            if (!idle() || this.golem.sitting() || this.golem.getRandom().nextInt(400) != 0) {
                return false;
            }
            List<WaxGolem> nearby = this.golem
                    .level()
                    .getEntitiesOfClass(
                            WaxGolem.class, this.golem.getBoundingBox().inflate(12.0d), other -> {
                                return other != this.golem && other.sitting();
                            });
            if (nearby.isEmpty()) {
                return false;
            }
            this.friend = nearby.get(this.golem.getRandom().nextInt(nearby.size()));
            return true;
        }

        public boolean canContinueToUse() {
            return idle() && this.friend != null && this.friend.isAlive() && this.duration > 0;
        }

        public void start() {
            this.duration = 400;
            this.golem.getNavigation().moveTo(this.friend, 0.7d);
        }

        public void tick() {
            this.duration--;
            this.golem.getLookControl().setLookAt(this.friend, 20.0f, 20.0f);
            if (this.golem.distanceToSqr(this.friend) < 6.0d) {
                this.golem.getNavigation().stop();
                this.golem.setSitting(true);
            }
        }

        public void stop() {
            this.golem.setSitting(false);
        }
    }

    /* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/WaxGolemGoals$LingerNearBees.class */
    public static class LingerNearBees extends Base {
        private static final double RANGE = 16.0d;
        private BlockPos spot;
        private int patience;

        public LingerNearBees(WaxGolem golem) {
            super(golem);
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            if (!idle() || this.golem.sitting() || this.golem.getRandom().nextInt(60) != 0) {
                return false;
            }
            this.spot = attractor();
            return this.spot != null && this.golem.blockPosition().distSqr(this.spot) > 16.0d;
        }

        private BlockPos attractor() {
            List<BlockPos> options = new ArrayList<>();
            for (HiveMemory memory : this.golem.hives()) {
                if (memory.pos().distToCenterSqr(this.golem.position()) <= 1024.0d) {
                    options.add(memory.pos());
                }
            }
            for (Entity entity : this.golem
                    .level()
                    .getEntities(this.golem, this.golem.getBoundingBox().inflate(16.0d), other -> {
                        return (other instanceof Bee) || (other instanceof WaxGolem);
                    })) {
                options.add(entity.blockPosition());
            }
            if (options.isEmpty()) {
                return null;
            }
            return options.get(this.golem.getRandom().nextInt(options.size()));
        }

        public boolean canContinueToUse() {
            return this.spot != null
                    && this.patience > 0
                    && idle()
                    && !this.golem.getNavigation().isDone();
        }

        public void start() {
            this.patience = 200;
            BlockPos stand = this.golem.standingSpotFor(this.spot.above());
            BlockPos goal = stand != null ? stand : this.spot;
            this.golem
                    .getNavigation()
                    .moveTo(((double) goal.getX()) + 0.5d, goal.getY(), ((double) goal.getZ()) + 0.5d, 0.6d);
        }

        public void tick() {
            this.patience--;
        }

        public void stop() {
            this.spot = null;
        }
    }

    /* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/WaxGolemGoals$ReturnHome.class */
    public static class ReturnHome extends Base {
        public ReturnHome(WaxGolem golem) {
            super(golem);
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return (!this.golem.awake() || this.golem.state() == WaxGolemState.DEPRESSED || this.golem.withinHome())
                    ? false
                    : true;
        }

        public boolean canContinueToUse() {
            return canUse() && !this.golem.getNavigation().isDone();
        }

        public void start() {
            BlockPos home = this.golem.home();
            this.golem
                    .getNavigation()
                    .moveTo(((double) home.getX()) + 0.5d, home.getY(), ((double) home.getZ()) + 0.5d, 0.8d);
        }
    }

    /* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/WaxGolemGoals$Sleep.class */
    public static class Sleep extends Base {
        public Sleep(WaxGolem golem) {
            super(golem);
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        public boolean canUse() {
            return this.golem.lit()
                    && (this.golem.level().isNight() || this.golem.level().isRaining())
                    && this.golem.state() != WaxGolemState.DEPRESSED;
        }

        public boolean isInterruptable() {
            return false;
        }

        public boolean canContinueToUse() {
            return canUse();
        }

        public void start() {
            this.golem.getNavigation().stop();
            this.golem.setState(WaxGolemState.SLEEPING);
            this.golem.setSitting(true);
        }

        public void tick() {
            this.golem.getLookControl().setLookAt(this.golem.getX(), this.golem.getY() - 1.0d, this.golem.getZ());
        }

        public void stop() {
            if (!this.golem.lit()) {
                return;
            }
            this.golem.setSitting(false);
            this.golem.setState(this.golem.canWork() ? WaxGolemState.ACTIVE : WaxGolemState.IDLE);
        }
    }
}
