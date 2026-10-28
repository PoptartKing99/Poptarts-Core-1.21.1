package dev.poptartking.poptartcore.flesh;

import dev.poptartking.poptartcore.registry.PoptartCorePoiTypes;
import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import dev.poptartking.poptartcore.registry.PoptartFleshBlocks;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent.Detonate;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = "poptartcore")
public final class FleshOrganisms {
    private static final Map<ResourceKey<Level>, Long2ObjectOpenHashMap<FleshOrganisms.Organism>> ORGANISMS =
            new HashMap<>();

    private FleshOrganisms() {}

    @Nullable
    public static FleshOrganisms.Organism of(ServerLevel level, BlockPos bud) {
        Long2ObjectOpenHashMap<FleshOrganisms.Organism> organisms =
                ORGANISMS.computeIfAbsent(level.dimension(), keyx -> new Long2ObjectOpenHashMap());
        long key = bud.asLong();
        FleshOrganisms.Organism cached = (FleshOrganisms.Organism) organisms.get(key);
        if (cached != null && !cached.isStale(level)) {
            return cached;
        } else if (level.isLoaded(bud) && level.getBlockState(bud).is(PoptartFleshBlocks.LIFEBUD.get())) {
            FleshOrganisms.Organism built = FleshOrganisms.Organism.build(level, bud.immutable());
            organisms.put(key, built);
            return built;
        } else {
            organisms.remove(key);
            return null;
        }
    }

    @Nullable
    public static FleshOrganisms.Organism owning(ServerLevel level, BlockPos pos) {
        Long2ObjectOpenHashMap<FleshOrganisms.Organism> organisms = ORGANISMS.get(level.dimension());
        if (organisms != null) {
            ObjectIterator bud = organisms.values().iterator();

            while (bud.hasNext()) {
                FleshOrganisms.Organism organism = (FleshOrganisms.Organism) bud.next();
                if (!organism.isStale(level) && organism.contains(pos)) {
                    return organism;
                }
            }
        }

        Optional<BlockPos> bud = level.getPoiManager()
                .findClosest(
                        holder -> holder.value() == PoptartCorePoiTypes.LIFEBUD.get(),
                        candidate -> {
                            FleshOrganisms.Organism organismx = of(level, candidate);
                            return organismx != null && organismx.contains(pos);
                        },
                        pos,
                        16,
                        Occupancy.ANY);
        return bud.<FleshOrganisms.Organism>map(found -> of(level, found)).orElse(null);
    }

    public static boolean sustains(ServerLevel level, BlockPos pos) {
        return owning(level, pos) != null;
    }

    public static void changed(LevelAccessor accessor, BlockPos pos) {
        if (accessor instanceof Level level) {
            Long2ObjectOpenHashMap<FleshOrganisms.Organism> organisms = ORGANISMS.get(level.dimension());
            if (organisms != null) {
                ObjectIterator var4 = organisms.values().iterator();

                while (var4.hasNext()) {
                    FleshOrganisms.Organism organism = (FleshOrganisms.Organism) var4.next();
                    if (organism.near(pos)) {
                        organism.dirty = true;
                    }
                }
            }
        }
    }

    public static void grew(ServerLevel level, BlockPos pos) {
        Long2ObjectOpenHashMap<FleshOrganisms.Organism> organisms = ORGANISMS.get(level.dimension());
        if (organisms != null) {
            ObjectIterator var3 = organisms.values().iterator();

            while (var3.hasNext()) {
                FleshOrganisms.Organism organism = (FleshOrganisms.Organism) var3.next();
                if (organism.touches(pos)) {
                    organism.add(pos);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBreak(BreakEvent event) {
        if (isPart(event.getState())) {
            changed(event.getLevel(), event.getPos());
        }
    }

    @SubscribeEvent
    public static void onPlace(EntityPlaceEvent event) {
        if (isPart(event.getPlacedBlock()) || isPart(event.getBlockSnapshot().getState())) {
            changed(event.getLevel(), event.getPos());
        }
    }

    @SubscribeEvent
    public static void onExplosion(Detonate event) {
        if (ORGANISMS.containsKey(event.getLevel().dimension())) {
            for (BlockPos pos : event.getAffectedBlocks()) {
                changed(event.getLevel(), pos);
            }
        }
    }

    @SubscribeEvent
    public static void onUnload(Unload event) {
        if (event.getLevel() instanceof Level level) {
            ORGANISMS.remove(level.dimension());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ORGANISMS.clear();
    }

    private static boolean isPart(BlockState state) {
        return state.is(PoptartCoreTags.FLESH) || state.is(PoptartFleshBlocks.LIFEBUD.get());
    }

    public static final class Organism {
        private final BlockPos bud;
        private final LongOpenHashSet members = new LongOpenHashSet();
        private final LongArrayList order = new LongArrayList();
        private final long builtAt;
        private double sumX;
        private double sumY;
        private double sumZ;
        private int minX = Integer.MAX_VALUE;
        private int minY = Integer.MAX_VALUE;
        private int minZ = Integer.MAX_VALUE;
        private int maxX = Integer.MIN_VALUE;
        private int maxY = Integer.MIN_VALUE;
        private int maxZ = Integer.MIN_VALUE;
        private boolean dirty;

        private Organism(BlockPos bud, long builtAt) {
            this.bud = bud;
            this.builtAt = builtAt;
        }

        private static FleshOrganisms.Organism build(ServerLevel level, BlockPos bud) {
            FleshOrganisms.Organism organism = new FleshOrganisms.Organism(bud, level.getGameTime());
            LongOpenHashSet seen = new LongOpenHashSet();
            LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
            seen.add(bud.asLong());
            queue.enqueue(bud.asLong());
            MutableBlockPos cursor = new MutableBlockPos();

            while (!queue.isEmpty() && organism.members.size() < FleshGrowth.geodeLimit()) {
                BlockPos from = BlockPos.of(queue.dequeueLong());

                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            cursor.setWithOffset(from, x, y, z);
                            long key = cursor.asLong();
                            if (seen.add(key)
                                    && FleshGrowth.inGeodeReach(bud, cursor)
                                    && level.isLoaded(cursor)
                                    && level.getBlockState(cursor).is(PoptartCoreTags.FLESH)) {
                                organism.add(cursor);
                                queue.enqueue(key);
                            }
                        }
                    }
                }
            }

            return organism;
        }

        private boolean isStale(ServerLevel level) {
            return this.dirty || level.getGameTime() - this.builtAt >= 600L;
        }

        private boolean near(BlockPos pos) {
            return pos.getX() >= this.minX - 1
                            && pos.getX() <= this.maxX + 1
                            && pos.getY() >= this.minY - 1
                            && pos.getY() <= this.maxY + 1
                            && pos.getZ() >= this.minZ - 1
                            && pos.getZ() <= this.maxZ + 1
                    || pos.distManhattan(this.bud) <= 2;
        }

        private boolean touches(BlockPos pos) {
            if (this.near(pos) && !this.members.contains(pos.asLong())) {
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            if (this.members.contains(
                                    BlockPos.asLong(pos.getX() + x, pos.getY() + y, pos.getZ() + z))) {
                                return true;
                            }
                        }
                    }
                }

                return false;
            } else {
                return false;
            }
        }

        private void add(BlockPos pos) {
            if (this.members.add(pos.asLong())) {
                this.order.add(pos.asLong());
                this.sumX = this.sumX + (double) pos.getX() + 0.5;
                this.sumY = this.sumY + (double) pos.getY() + 0.5;
                this.sumZ = this.sumZ + (double) pos.getZ() + 0.5;
                this.minX = Math.min(this.minX, pos.getX());
                this.minY = Math.min(this.minY, pos.getY());
                this.minZ = Math.min(this.minZ, pos.getZ());
                this.maxX = Math.max(this.maxX, pos.getX());
                this.maxY = Math.max(this.maxY, pos.getY());
                this.maxZ = Math.max(this.maxZ, pos.getZ());
            }
        }

        public boolean contains(BlockPos pos) {
            return this.members.contains(pos.asLong());
        }

        public BlockPos getBud() {
            return this.bud;
        }

        public int size() {
            return this.members.size();
        }

        @Nullable
        public BlockPos randomMember(RandomSource random) {
            return this.order.isEmpty() ? null : BlockPos.of(this.order.getLong(random.nextInt(this.order.size())));
        }

        public LongIterator iterator() {
            return this.members.iterator();
        }

        public double imbalanceWith(BlockPos added) {
            int count = this.members.size() + 1;
            double x = (this.sumX + (double) added.getX() + 0.5) / (double) count - ((double) this.bud.getX() + 0.5);
            double y = (this.sumY + (double) added.getY() + 0.5) / (double) count - ((double) this.bud.getY() + 0.5);
            double z = (this.sumZ + (double) added.getZ() + 0.5) / (double) count - ((double) this.bud.getZ() + 0.5);
            return x * x + y * y + z * z;
        }

        public Vec3 getCentre() {
            if (this.members.isEmpty()) {
                return Vec3.atCenterOf(this.bud);
            } else {
                int count = this.members.size();
                return new Vec3(this.sumX / (double) count, this.sumY / (double) count, this.sumZ / (double) count);
            }
        }
    }
}
