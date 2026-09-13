package dev.poptartking.poptartcore.waxgolem;

import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/waxgolem/WaxGolem.class */
public class WaxGolem extends AbstractGolem {
    public static final int LIFETIME_TICKS = 48000;
    public static final int STAGES = 4;
    public static final int HARVEST_COST = 600;
    public static final int HOME_RADIUS = 32;
    public static final int LOST_PATIENCE = 6000;
    public static final int RAIN_TOLERANCE = 400;
    public static final int WAX_UNITS = 16;
    public static final int TICKS_PER_WAX = 3000;
    public static final int STARE_TICKS = 100;
    private final List<HiveMemory> hives;
    private BlockPos home;
    private int burned;
    private int outsideHome;
    private int soaked;
    private int stared;
    private int scanCooldown;
    private Player watcher;
    private BlockPos drawnFrom;
    private BlockPos claim;
    private long restUntil;
    public static final int MIN_REACH_DROP = 0;
    public static final int MAX_REACH_DROP = 2;
    public static final double LIVE_RANGE = 10.0d;
    private Vec3 wickAnchor;
    public static final int REST_MIN = 160;
    public static final int REST_SPREAD = 320;
    public static final int REACH = 3;
    public static final double WICK_FORWARD = 0.0625d;
    public static final float SIT_DROP = 0.5f;
    public static final float HEAD_PIVOT = 1.375f;
    public static final float DEAD_HEAD_PIVOT = 1.25f;
    public static final int SCAN_INTERVAL = 40;
    public static final int SCAN_RADIUS = 8;
    public static final int SCAN_HEIGHT = 4;
    public static final int MAX_REMEMBERED = 48;
    private static final EntityDimensions[] SIZES = {
        EntityDimensions.scalable(0.8f, 1.94f).withEyeHeight(1.59f),
        EntityDimensions.scalable(0.8f, 1.88f).withEyeHeight(1.56f),
        EntityDimensions.scalable(0.8f, 1.69f).withEyeHeight(1.44f),
        EntityDimensions.scalable(0.8f, 1.5f).withEyeHeight(1.25f)
    };
    private static final EntityDataAccessor<Integer> DATA_STAGE =
            SynchedEntityData.defineId(WaxGolem.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_LIT =
            SynchedEntityData.defineId(WaxGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SITTING =
            SynchedEntityData.defineId(WaxGolem.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(WaxGolem.class, EntityDataSerializers.INT);
    public static final float[] WICK_HEIGHT = {1.9375f, 1.875f, 1.6875f, 1.5f};
    private static final Predicate<BlockState> IS_HIVE = state -> {
        return state.getBlock() instanceof BeehiveBlock;
    };

    public WaxGolem(EntityType<? extends WaxGolem> type, Level level) {
        super(type, level);
        this.hives = new ArrayList();
        setPersistenceRequired();
        setCanPickUpLoot(true);
    }

    public static boolean carryable(ItemStack stack) {
        return stack.is(Items.HONEYCOMB)
                || stack.is(Items.HONEY_BOTTLE)
                || stack.is(Items.GLASS_BOTTLE)
                || (stack.getItem() instanceof ShearsItem)
                || stack.is(PoptartCoreItems.WAX.get());
    }

    private static boolean isTool(ItemStack stack) {
        return (stack.getItem() instanceof ShearsItem) || stack.is(Items.GLASS_BOTTLE);
    }

    public boolean wantsToPickUp(ItemStack stack) {
        if (!carryable(stack)) {
            return false;
        }
        if (isTool(stack)) {
            return tool().isEmpty()
                    || (ItemStack.isSameItem(tool(), stack) && tool().getCount() < tool().getMaxStackSize());
        }
        return result().isEmpty()
                || (ItemStack.isSameItem(result(), stack) && result().getCount() < result().getMaxStackSize());
    }

    protected void pickUpItem(ItemEntity item) {
        ItemStack stack = item.getItem();
        if (!wantsToPickUp(stack)) {
            return;
        }
        int originalCount = stack.getCount();
        boolean toTool = isTool(stack);
        ItemStack held = toTool ? tool() : result();
        if (held.isEmpty()) {
            int taken = Math.min(stack.getCount(), stack.getMaxStackSize());
            if (toTool) {
                setTool(stack.split(taken));
                setState(WaxGolemState.ACTIVE);
            } else {
                setResult(stack.split(taken));
            }
        } else if (ItemStack.isSameItem(held, stack)) {
            int room = Math.min(held.getMaxStackSize() - held.getCount(), stack.getCount());
            held.grow(room);
            stack.shrink(room);
        }
        onItemPickup(item);
        take(item, originalCount - stack.getCount());
        if (stack.isEmpty()) {
            item.discard();
        }
    }

    public static AttributeSupplier.Builder attributes() {
        return AbstractGolem.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0d)
                .add(Attributes.MOVEMENT_SPEED, 0.2d)
                .add(Attributes.STEP_HEIGHT, 1.0d);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new WaxGolemGoals.Sleep(this));
        this.goalSelector.addGoal(1, new WaxGolemGoals.WatchWatcher(this));
        this.goalSelector.addGoal(2, new WaxGolemGoals.ReturnHome(this));
        this.goalSelector.addGoal(2, new CollectDropsGoal(this));
        this.goalSelector.addGoal(3, new HarvestHiveGoal(this));
        this.goalSelector.addGoal(4, new DepositResultGoal(this));
        this.goalSelector.addGoal(5, new FerryHoneyGoal(this));
        this.goalSelector.addGoal(5, new WaxGolemGoals.SitNearFriend(this));
        this.goalSelector.addGoal(6, new WaxGolemGoals.Sit(this));
        this.goalSelector.addGoal(7, new WaxGolemGoals.StareAtBlock(this, WaxGolemGoals.StareAtBlock.Kind.HIVE, 300));
        this.goalSelector.addGoal(7, new WaxGolemGoals.StareAtBlock(this, WaxGolemGoals.StareAtBlock.Kind.CANDLE, 500));
        this.goalSelector.addGoal(7, new WaxGolemGoals.StareAtBlock(this, WaxGolemGoals.StareAtBlock.Kind.FLOWER, 500));
        this.goalSelector.addGoal(7, new WaxGolemGoals.StareAtBlock(this, WaxGolemGoals.StareAtBlock.Kind.GLASS, 700));
        this.goalSelector.addGoal(8, new WaxGolemGoals.StareAt(this, Animal.class, 400));
        this.goalSelector.addGoal(8, new WaxGolemGoals.LingerNearBees(this));
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 0.6d) {
            public boolean canUse() {
                return WaxGolem.this.awake() && !WaxGolem.this.sitting() && super.canUse();
            }
        });
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this) {
            public boolean canUse() {
                return WaxGolem.this.awake() && super.canUse();
            }
        });
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STAGE, 0);
        builder.define(DATA_LIT, true);
        builder.define(DATA_SITTING, false);
        builder.define(DATA_STATE, Integer.valueOf(WaxGolemState.IDLE.ordinal()));
    }

    public int stage() {
        return ((Integer) this.entityData.get(DATA_STAGE)).intValue();
    }

    public void setStage(int stage) {
        this.entityData.set(DATA_STAGE, Integer.valueOf(Math.clamp(stage, 0, 3)));
        refreshDimensions();
    }

    public EntityDimensions getDefaultDimensions(Pose pose) {
        return SIZES[Math.clamp(stage(), 0, 3)];
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_STAGE.equals(key)) {
            refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    public boolean lit() {
        return ((Boolean) this.entityData.get(DATA_LIT)).booleanValue();
    }

    public void setLit(boolean value) {
        this.entityData.set(DATA_LIT, Boolean.valueOf(value));
        if (!value) {
            setState(WaxGolemState.EXTINGUISHED);
        } else if (state() == WaxGolemState.EXTINGUISHED) {
            setState(WaxGolemState.IDLE);
        }
    }

    public boolean sitting() {
        return ((Boolean) this.entityData.get(DATA_SITTING)).booleanValue();
    }

    public void setSitting(boolean value) {
        this.entityData.set(DATA_SITTING, Boolean.valueOf(value));
    }

    public WaxGolemState state() {
        return WaxGolemState.values()[((Integer) this.entityData.get(DATA_STATE)).intValue()];
    }

    public void setState(WaxGolemState value) {
        this.entityData.set(DATA_STATE, Integer.valueOf(value.ordinal()));
    }

    public boolean awake() {
        return lit() && state() != WaxGolemState.SLEEPING;
    }

    public BlockPos home() {
        return this.home == null ? blockPosition() : this.home;
    }

    public void setHome(BlockPos pos) {
        this.home = pos;
        this.outsideHome = 0;
    }

    public boolean withinHome() {
        return blockPosition().closerThan(home(), 32.0d);
    }

    public BlockPos drawnFrom() {
        return this.drawnFrom;
    }

    public void setDrawnFrom(BlockPos pos) {
        this.drawnFrom = pos;
    }

    public List<HiveMemory> hives() {
        return this.hives;
    }

    public HiveMemory hive(BlockPos pos) {
        for (HiveMemory memory : this.hives) {
            if (memory.pos().equals(pos)) {
                return memory;
            }
        }
        HiveMemory memory2 = new HiveMemory(pos);
        memory2.setReachable(reachable(pos));
        this.hives.add(memory2);
        return memory2;
    }

    public BlockPos claim() {
        return this.claim;
    }

    public void setClaim(BlockPos pos) {
        this.claim = pos;
    }

    public Set<BlockPos> foreignClaims() {
        List<WaxGolem> others = level().getEntitiesOfClass(
                        WaxGolem.class, getBoundingBox().inflate(64.0d), other -> {
                            return (other == this || other.claim == null) ? false : true;
                        });
        if (others.isEmpty()) {
            return Set.of();
        }
        Set<BlockPos> claims = new HashSet<>();
        for (WaxGolem other2 : others) {
            claims.add(other2.claim);
        }
        return claims;
    }

    public void observeHive(BlockPos pos) {
        BlockState state = level().getBlockState(pos);
        if (!(state.getBlock() instanceof BeehiveBlock)) {
            this.hives.removeIf(memory -> {
                return memory.pos().equals(pos);
            });
            return;
        }
        long time = level().getGameTime();
        HiveMemory memory2 = hive(pos);
        memory2.seen(((Integer) state.getValue(BeehiveBlock.HONEY_LEVEL)).intValue(), time);
        memory2.sawSmoke(CampfireBlock.isSmokeyPos(level(), pos), time);
        memory2.setReachable(reachable(pos));
    }

    public boolean reachable(BlockPos hive) {
        return hiveOpen(hive)
                && standingSpotFor(hive, spot -> {
                            return true;
                        })
                        != null;
    }

    public boolean hiveOpen(BlockPos hive) {
        BlockState state = level().getBlockState(hive);
        if (!(state.getBlock() instanceof BeehiveBlock)) {
            return false;
        }
        BlockPos front = hive.relative(state.getValue(BeehiveBlock.FACING));
        return !level().getBlockState(front).isRedstoneConductor(level(), front);
    }

    public BlockPos harvestSpotFor(BlockPos hive) {
        BlockState state = level().getBlockState(hive);
        if (!(state.getBlock() instanceof BeehiveBlock)) {
            return null;
        }
        Direction facing = state.getValue(BeehiveBlock.FACING);
        BlockPos front = standingSpotFor(hive, spot -> {
            return ((spot.getX() - hive.getX()) * facing.getStepX()) + ((spot.getZ() - hive.getZ()) * facing.getStepZ())
                    > 0;
        });
        return front != null ? front : standingSpotFor(hive);
    }

    public void setWickAnchor(Vec3 anchor) {
        this.wickAnchor = anchor;
    }

    public Vec3 wickPosition() {
        if (this.wickAnchor != null) {
            return position().add(this.wickAnchor);
        }
        return analyticWickPosition();
    }

    private Vec3 analyticWickPosition() {
        int stage = Math.clamp(stage(), 0, WICK_HEIGHT.length - 1);
        float base = stage == 3 ? 1.25f : 1.375f;
        float pivot = base - (sitting() ? 0.5f : 0.0f);
        Vec3 offset = new Vec3(0.0d, WICK_HEIGHT[stage] - base, 0.0625d)
                .xRot(getXRot() * 0.017453292f)
                .yRot((-this.yHeadRot) * 0.017453292f);
        return new Vec3(getX() + offset.x, getY() + ((double) pivot) + offset.y, getZ() + offset.z);
    }

    public boolean resting() {
        return level().getGameTime() < this.restUntil;
    }

    public void rest() {
        this.restUntil = level().getGameTime() + 160 + ((long) this.random.nextInt(REST_SPREAD));
    }

    public boolean inReachOf(BlockPos pos) {
        BlockPos standing = blockPosition();
        int rise = pos.getY() - standing.getY();
        return rise >= 0
                && rise <= 2
                && Math.abs(pos.getX() - standing.getX()) <= 3
                && Math.abs(pos.getZ() - standing.getZ()) <= 3;
    }

    public void collectAroundHives() {
        for (HiveMemory memory : this.hives) {
            BlockPos pos = memory.pos();
            if (inReachOf(pos)) {
                AABB above = new AABB(pos.above());
                for (ItemEntity item : level().getEntitiesOfClass(ItemEntity.class, above, drop -> {
                    return drop.isAlive() && wantsToPickUp(drop.getItem());
                })) {
                    item.setNoPickUpDelay();
                    pickUpItem(item);
                }
            }
        }
    }

    public void refreshNearbyHives() {
        long time = level().getGameTime();
        for (HiveMemory memory : this.hives) {
            if (memory.pos().distToCenterSqr(position()) <= 100.0d) {
                BlockState state = level().getBlockState(memory.pos());
                if (state.getBlock() instanceof BeehiveBlock) {
                    memory.seen(((Integer) state.getValue(BeehiveBlock.HONEY_LEVEL)).intValue(), time);
                    memory.sawSmoke(CampfireBlock.isSmokeyPos(level(), memory.pos()), time);
                    memory.setReachable(reachable(memory.pos()));
                }
            }
        }
        this.hives.removeIf(memory2 -> {
            return memory2.pos().distToCenterSqr(position()) <= 100.0d
                    && !(level().getBlockState(memory2.pos()).getBlock() instanceof BeehiveBlock);
        });
    }

    public BlockPos standingSpotFor(BlockPos hive) {
        return standingSpotFor(hive, spot -> {
            return true;
        });
    }

    public BlockPos standingSpotFor(BlockPos hive, Predicate<BlockPos> allowed) {
        BlockPos best = null;
        double nearest = Double.MAX_VALUE;
        for (int drop = 0; drop <= 2; drop++) {
            int y = hive.getY() - drop;
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos spot = new BlockPos(hive.getX() + dx, y, hive.getZ() + dz);
                    if (standable(spot) && allowed.test(spot)) {
                        double distance = spot.distSqr(hive);
                        if (distance < nearest) {
                            nearest = distance;
                            best = spot;
                        }
                    }
                }
            }
        }
        return best;
    }

    public boolean standable(BlockPos spot) {
        BlockPos below = spot.below();
        return level().getBlockState(below).isFaceSturdy(level(), below, Direction.UP)
                && level().getBlockState(spot).getCollisionShape(level(), spot).isEmpty()
                && level().getBlockState(spot.above())
                        .getCollisionShape(level(), spot.above())
                        .isEmpty();
    }

    public boolean hasWork() {
        if (!canWork() || !result().isEmpty() || resting()) {
            return false;
        }
        long time = level().getGameTime();
        for (HiveMemory memory : this.hives) {
            if (memory.harvestable(time)) {
                return true;
            }
        }
        return false;
    }

    public boolean anyReachableDue() {
        long time = level().getGameTime();
        for (HiveMemory memory : this.hives) {
            if (memory.harvestable(time)) {
                return true;
            }
        }
        return false;
    }

    public ItemStack tool() {
        return getItemBySlot(EquipmentSlot.MAINHAND);
    }

    public ItemStack result() {
        return getItemBySlot(EquipmentSlot.OFFHAND);
    }

    public void giveBack(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack tool = tool();
        if (tool.isEmpty()) {
            setTool(stack);
            return;
        }
        if (ItemStack.isSameItemSameComponents(tool, stack)
                && tool.getCount() + stack.getCount() <= tool.getMaxStackSize()) {
            tool.grow(stack.getCount());
            return;
        }
        ItemEntity drop = new ItemEntity(level(), getX(), getY() + 0.5d, getZ(), stack);
        drop.setDefaultPickUpDelay();
        level().addFreshEntity(drop);
    }

    public void setTool(ItemStack stack) {
        setItemSlot(EquipmentSlot.MAINHAND, stack);
    }

    public void setResult(ItemStack stack) {
        setItemSlot(EquipmentSlot.OFFHAND, stack);
        if (stack.isEmpty()) {
            this.drawnFrom = null;
        }
    }

    public boolean canWork() {
        ItemStack tool = tool();
        return !tool.isEmpty() && ((tool.getItem() instanceof ShearsItem) || tool.is(Items.GLASS_BOTTLE));
    }

    public boolean spent() {
        return this.burned >= 48000;
    }

    public void spendLife(int ticks) {
        this.burned += ticks;
        int stage = Math.min(3, (this.burned * 4) / LIFETIME_TICKS);
        if (stage != stage()) {
            setStage(stage);
        }
        if (this.burned >= 48000) {
            this.burned = LIFETIME_TICKS;
            melt();
        }
    }

    private void melt() {
        extinguish();
    }

    public void feedWax(int units) {
        this.burned = Math.max(0, this.burned - (units * TICKS_PER_WAX));
        setStage(Math.min(3, (this.burned * 4) / LIFETIME_TICKS));
    }

    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (lit() && this.random.nextInt(4) == 0) {
                Vec3 wick = wickPosition();
                level().addParticle(ParticleTypes.SMALL_FLAME, wick.x, wick.y, wick.z, 0.0d, 0.0d, 0.0d);
                if (this.random.nextInt(6) == 0) {
                    level().addParticle(ParticleTypes.SMOKE, wick.x, wick.y + 0.1d, wick.z, 0.0d, 0.01d, 0.0d);
                    return;
                }
                return;
            }
            return;
        }
        if (awake()) {
            spendLife(1);
            trackWatcher();
            discoverHives();
            refreshNearbyHives();
            collectAroundHives();
        }
        trackHome();
        trackWeather();
    }

    private void discoverHives() {
        int i = this.scanCooldown - 1;
        this.scanCooldown = i;
        if (i > 0) {
            return;
        }
        this.scanCooldown = 40;
        BlockPos origin = blockPosition();
        BlockPos home = home();
        int minX = origin.getX() - 8;
        int maxX = origin.getX() + 8;
        int minZ = origin.getZ() - 8;
        int maxZ = origin.getZ() + 8;
        int minY = origin.getY() - 4;
        int maxY = origin.getY() + 4;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int chunkX = minX >> 4; chunkX <= (maxX >> 4); chunkX++) {
            for (int chunkZ = minZ >> 4; chunkZ <= (maxZ >> 4); chunkZ++) {
                if (level().hasChunk(chunkX, chunkZ)) {
                    LevelChunk chunk = level().getChunk(chunkX, chunkZ);
                    int firstSection = chunk.getSectionIndex(minY);
                    int lastSection = chunk.getSectionIndex(maxY);
                    for (int index = Math.max(0, firstSection);
                            index <= Math.min(chunk.getSections().length - 1, lastSection);
                            index++) {
                        LevelChunkSection section = chunk.getSections()[index];
                        if (section != null && !section.hasOnlyAir() && section.maybeHas(IS_HIVE)) {
                            scanSection(
                                    chunk,
                                    index,
                                    cursor,
                                    home,
                                    Math.max(minX, chunkX << 4),
                                    Math.min(maxX, (chunkX << 4) + 15),
                                    Math.max(minZ, chunkZ << 4),
                                    Math.min(maxZ, (chunkZ << 4) + 15),
                                    minY,
                                    maxY);
                        }
                    }
                }
            }
        }
        this.hives.removeIf(memory -> {
            return level().isLoaded(memory.pos())
                    && !(level().getBlockState(memory.pos()).getBlock() instanceof BeehiveBlock);
        });
    }

    private void scanSection(
            LevelChunk chunk,
            int index,
            BlockPos.MutableBlockPos cursor,
            BlockPos home,
            int fromX,
            int toX,
            int fromZ,
            int toZ,
            int minY,
            int maxY) {
        int bottom = chunk.getSectionYFromSectionIndex(index) << 4;
        int fromY = Math.max(minY, bottom);
        int toY = Math.min(maxY, bottom + 15);
        for (int y = fromY; y <= toY; y++) {
            for (int x = fromX; x <= toX; x++) {
                for (int z = fromZ; z <= toZ; z++) {
                    if (this.hives.size() >= 48) {
                        return;
                    }
                    cursor.set(x, y, z);
                    if (cursor.closerThan(home, 32.0d) && IS_HIVE.test(level().getBlockState(cursor))) {
                        hive(cursor.immutable());
                    }
                }
            }
        }
    }

    private void trackWatcher() {
        if (this.watcher != null
                && (this.watcher.isRemoved() || distanceToSqr(this.watcher) > 64.0d || !staredAtBy(this.watcher))) {
            this.watcher = null;
            this.stared = 0;
        }
        if (this.watcher == null) {
            for (Player player :
                    level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(8.0d))) {
                if (staredAtBy(player)) {
                    this.watcher = player;
                    return;
                }
            }
            return;
        }
        this.stared++;
    }

    public boolean beingWatched() {
        return this.watcher != null && this.stared >= 100;
    }

    public Player watcher() {
        return this.watcher;
    }

    private boolean staredAtBy(Player player) {
        Vec3 look = player.getViewVector(1.0f).normalize();
        Vec3 toGolem = new Vec3(getX() - player.getX(), getEyeY() - player.getEyeY(), getZ() - player.getZ());
        double distance = toGolem.length();
        return distance <= 8.0d && look.dot(toGolem.normalize()) > 0.97d;
    }

    private void trackHome() {
        if (withinHome()) {
            this.outsideHome = 0;
            if (state() == WaxGolemState.DEPRESSED) {
                setState(WaxGolemState.IDLE);
                setSitting(false);
                return;
            }
            return;
        }
        if (!awake()) {
            return;
        }
        this.outsideHome++;
        if (this.outsideHome >= 6000 && state() != WaxGolemState.DEPRESSED) {
            setState(WaxGolemState.DEPRESSED);
            setSitting(true);
            extinguish();
        }
    }

    private void trackWeather() {
        if (!lit()) {
            return;
        }
        if (level().isRainingAt(blockPosition())) {
            this.soaked++;
            if (this.soaked >= 400) {
                extinguish();
                return;
            }
            return;
        }
        this.soaked = Math.max(0, this.soaked - 2);
    }

    public boolean isEffectiveAi() {
        return super.isEffectiveAi() && lit();
    }

    private void freeze() {
        this.xxa = 0.0f;
        this.yya = 0.0f;
        this.zza = 0.0f;
        setSpeed(0.0f);
        setDeltaMovement(getDeltaMovement().multiply(0.0d, 1.0d, 0.0d));
        getNavigation().stop();
    }

    public void extinguish() {
        if (!lit()) {
            return;
        }
        setLit(false);
        setSitting(false);
        this.soaked = 0;
        freeze();
        level().playSound(
                        (Player) null, blockPosition(), SoundEvents.CANDLE_EXTINGUISH, SoundSource.NEUTRAL, 1.0f, 1.0f);
    }

    public void light() {
        if (lit() || spent()) {
            return;
        }
        setLit(true);
        if (state() == WaxGolemState.DEPRESSED) {
            setHome(blockPosition());
            setState(WaxGolemState.IDLE);
            setSitting(false);
        }
        level().playSound(
                        (Player) null, blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.NEUTRAL, 1.0f, 1.0f);
    }

    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level().isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }
        if (!lit()
                && !spent()
                && ((held.getItem() instanceof FlintAndSteelItem)
                        || held.is(Items.FIRE_CHARGE)
                        || held.is(ItemTags.CANDLES)
                        || held.is(Items.TORCH))) {
            light();
            if (!(held.getItem() instanceof FlintAndSteelItem)) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
            } else {
                held.hurtAndBreak(
                        1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
            return InteractionResult.CONSUME;
        }
        if (held.getItem() instanceof ShovelItem) {
            extinguish();
            return InteractionResult.CONSUME;
        }
        if (held.is(PoptartCoreItems.WAX.get()) && this.burned > 0) {
            held.shrink(1);
            feedWax(1);
            level().playSound(
                            (Player) null,
                            blockPosition(),
                            SoundEvents.HONEYCOMB_WAX_ON,
                            SoundSource.NEUTRAL,
                            1.0f,
                            1.0f);
            return InteractionResult.CONSUME;
        }
        if (tool().isEmpty() && ((held.getItem() instanceof ShearsItem) || held.is(Items.GLASS_BOTTLE))) {
            setTool(held.split(held.getItem() instanceof ShearsItem ? 1 : held.getCount()));
            setState(WaxGolemState.ACTIVE);
            return InteractionResult.CONSUME;
        }
        if (!tool().isEmpty() && held.isEmpty()) {
            player.setItemInHand(hand, tool().copy());
            setTool(ItemStack.EMPTY);
            setState(WaxGolemState.IDLE);
            return InteractionResult.CONSUME;
        }
        return super.mobInteract(player, hand);
    }

    public boolean hurt(DamageSource source, float amount) {
        if (state() == WaxGolemState.SLEEPING) {
            setState(WaxGolemState.IDLE);
            setSitting(false);
        }
        if (source.is(DamageTypeTags.IS_FIRE)) {
            light();
        }
        return super.hurt(source, amount);
    }

    public boolean isPushable() {
        return lit();
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Burned", this.burned);
        tag.putBoolean("Lit", lit());
        tag.putInt("Stage", stage());
        tag.putInt("State", state().ordinal());
        tag.putInt("OutsideHome", this.outsideHome);
        if (this.home != null) {
            tag.put("Home", NbtUtils.writeBlockPos(this.home));
        }
        ListTag known = new ListTag();
        for (HiveMemory memory : this.hives) {
            CompoundTag entry = new CompoundTag();
            entry.put("Pos", NbtUtils.writeBlockPos(memory.pos()));
            entry.putInt("Honey", memory.honeyLevel());
            entry.putBoolean("Reachable", memory.reachable());
            entry.putInt("Smoke", memory.smoke());
            known.add(entry);
        }
        tag.put("Hives", known);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.burned = tag.getInt("Burned");
        this.entityData.set(DATA_LIT, Boolean.valueOf(tag.getBoolean("Lit")));
        setStage(tag.getInt("Stage"));
        this.entityData.set(DATA_STATE, Integer.valueOf(tag.getInt("State")));
        this.outsideHome = tag.getInt("OutsideHome");
        NbtUtils.readBlockPos(tag, "Home").ifPresent(pos -> {
            this.home = pos;
        });
        this.hives.clear();
        ListTag known = tag.getList("Hives", 10);
        for (int index = 0; index < known.size(); index++) {
            CompoundTag entry = known.getCompound(index);
            NbtUtils.readBlockPos(entry, "Pos").ifPresent(pos2 -> {
                HiveMemory memory = new HiveMemory(pos2);
                memory.seen(entry.getInt("Honey"), 0L);
                memory.setReachable(!entry.contains("Reachable") || entry.getBoolean("Reachable"));
                memory.restoreSmoke(entry.contains("Smoke") ? entry.getInt("Smoke") : -1, 0L);
                this.hives.add(memory);
            });
        }
    }
}
