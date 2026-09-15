package dev.poptartking.poptartcore.rift;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreParticles;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class RiftBearing {
    private static final int SEARCH_RADIUS_CHUNKS = 64;
    private static final long CACHE_TICKS = 5L * 60L * 20L;
    private static final double CACHE_REUSE_DISTANCE_SQUARED = 128.0 * 128.0;
    private static final double CHAIN_DISTANCE_SQUARED = 64.0 * 64.0;
    private static final int DURATION_TICKS = 30;
    private static final double FALLOFF_BLOCKS = 900.0;
    private static final double DRAUGHT = 0.16;
    private static final TagKey<Structure> RUINED_PORTALS =
            TagKey.create(Registries.STRUCTURE, ResourceLocation.withDefaultNamespace("ruined_portal"));

    private static final List<Pulse> ACTIVE = new ArrayList<>();
    private static final Map<ServerLevel, List<CachedSearch>> SEARCH_CACHE = new IdentityHashMap<>();

    private RiftBearing() {}

    public static void prime(ServerLevel level, BlockPos pos) {
        if (findCached(level, pos, CACHE_REUSE_DISTANCE_SQUARED) != null) {
            return;
        }

        BlockPos target = level.findNearestMapStructure(RUINED_PORTALS, pos, SEARCH_RADIUS_CHUNKS, false);
        SEARCH_CACHE
                .computeIfAbsent(level, ignored -> new ArrayList<>())
                .add(new CachedSearch(
                        pos.immutable(),
                        target == null ? null : target.immutable(),
                        level.getGameTime() + CACHE_TICKS));
    }

    public static void pulse(ServerLevel level, BlockPos pos) {
        CachedSearch cached = findCached(level, pos, CHAIN_DISTANCE_SQUARED);
        Vec3 origin = Vec3.atCenterOf(pos).add(0.0, -0.35, 0.0);
        Vec3 bearing = Vec3.ZERO;
        float closeness = 0.0F;
        boolean found = cached != null && cached.target() != null;
        if (found) {
            Vec3 flat = new Vec3(
                    cached.target().getX() + 0.5 - origin.x,
                    0.0,
                    cached.target().getZ() + 0.5 - origin.z);
            double distance = Math.sqrt(flat.lengthSqr());
            if (distance > 0.01) {
                bearing = flat.scale(1.0 / distance);
            }
            closeness = (float) Math.max(0.0, 1.0 - distance / FALLOFF_BLOCKS);
        }

        ACTIVE.add(new Pulse(level, origin, bearing, closeness, found));
        RandomSource random = level.random;
        if (found) {
            for (int index = 0; index < 7; index++) {
                level.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        origin.x,
                        origin.y + 0.2,
                        origin.z,
                        0,
                        (random.nextDouble() - 0.5) * 0.22,
                        0.14 + random.nextDouble() * 0.14,
                        (random.nextDouble() - 0.5) * 0.22,
                        1.0);
            }
        }
        level.playSound(
                null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.45F, 1.5F + random.nextFloat() * 0.3F);
    }

    @Nullable
    private static CachedSearch findCached(ServerLevel level, BlockPos pos, double maximumDistanceSquared) {
        List<CachedSearch> searches = SEARCH_CACHE.get(level);
        if (searches == null) {
            return null;
        }

        long gameTime = level.getGameTime();
        searches.removeIf(search -> search.expiresAt() <= gameTime);
        if (searches.isEmpty()) {
            SEARCH_CACHE.remove(level);
            return null;
        }

        CachedSearch nearest = null;
        double nearestDistance = maximumDistanceSquared;
        for (CachedSearch search : searches) {
            double distance = search.origin().distSqr(pos);
            if (distance <= nearestDistance) {
                nearest = search;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || ACTIVE.isEmpty()) {
            return;
        }

        Iterator<Pulse> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            Pulse pulse = iterator.next();
            if (pulse.level() == level && advance(pulse)) {
                iterator.remove();
            }
        }
    }

    private static boolean advance(Pulse pulse) {
        pulse.tick++;
        if (pulse.tick > DURATION_TICKS) {
            return true;
        }

        ServerLevel level = pulse.level();
        RandomSource random = level.random;
        double age = pulse.tick / (double) DURATION_TICKS;
        double strength = pulse.found() ? pulse.closeness() : 0.0;
        double lateral = DRAUGHT * strength;
        double rise = 0.06 * (1.0 - 0.55 * strength);
        int smokeCount = pulse.tick < 6 ? 3 : 1;
        for (int index = 0; index < smokeCount; index++) {
            double wobble = (random.nextDouble() - 0.5) * 0.02;
            double spin = (random.nextDouble() - 0.5) * 0.03;
            Vec3 drift = pulse.bearing().scale(lateral * (0.35 + age));
            level.sendParticles(
                    pulse.tick < 4 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.SMOKE,
                    pulse.origin().x + (random.nextDouble() - 0.5) * 0.35,
                    pulse.origin().y + 0.18 + age * 0.25,
                    pulse.origin().z + (random.nextDouble() - 0.5) * 0.35,
                    0,
                    drift.x + wobble,
                    rise + random.nextDouble() * 0.02,
                    drift.z + spin,
                    1.0);
        }

        if (pulse.found() && pulse.tick < 14 && random.nextInt(2) == 0) {
            double lift = 0.16 + random.nextDouble() * 0.12;
            Vec3 fling = pulse.bearing().scale(lateral * (1.6 + random.nextDouble()));
            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    pulse.origin().x + (random.nextDouble() - 0.5) * 0.25,
                    pulse.origin().y + 0.2,
                    pulse.origin().z + (random.nextDouble() - 0.5) * 0.25,
                    0,
                    fling.x + (random.nextDouble() - 0.5) * 0.06,
                    lift - 0.09 * strength,
                    fling.z + (random.nextDouble() - 0.5) * 0.06,
                    1.0);
        }

        if (pulse.found() && pulse.tick < 8 && strength > 0.0 && random.nextInt(3) == 0) {
            Vec3 ember = pulse.bearing().scale(lateral * 2.1);
            level.sendParticles(
                    PoptartCoreParticles.RIFT_FIRE_FLAME.get(),
                    pulse.origin().x,
                    pulse.origin().y + 0.15,
                    pulse.origin().z,
                    0,
                    ember.x,
                    0.02,
                    ember.z,
                    0.7);
        }

        if (pulse.found() && pulse.tick == 10 && strength > 0.45) {
            level.playSound(
                    null,
                    BlockPos.containing(pulse.origin()),
                    SoundEvents.CANDLE_EXTINGUISH,
                    SoundSource.BLOCKS,
                    0.5F * (float) strength,
                    0.6F);
        }
        return false;
    }

    public static void clear(ServerLevel level) {
        SEARCH_CACHE.remove(level);
        ACTIVE.removeIf(pulse -> pulse.level() == level);
    }

    private record CachedSearch(BlockPos origin, @Nullable BlockPos target, long expiresAt) {}

    private static final class Pulse {
        private final ServerLevel level;
        private final Vec3 origin;
        private final Vec3 bearing;
        private final float closeness;
        private final boolean found;
        private int tick;

        private Pulse(ServerLevel level, Vec3 origin, Vec3 bearing, float closeness, boolean found) {
            this.level = level;
            this.origin = origin;
            this.bearing = bearing;
            this.closeness = closeness;
            this.found = found;
        }

        private ServerLevel level() {
            return level;
        }

        private Vec3 origin() {
            return origin;
        }

        private Vec3 bearing() {
            return bearing;
        }

        private float closeness() {
            return closeness;
        }

        private boolean found() {
            return found;
        }
    }
}
