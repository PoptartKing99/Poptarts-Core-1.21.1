package dev.poptartking.poptartcore.rift;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreParticles;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class RiftPortalIgnition {
    private static final int MAX_INTERIOR = 441;
    private static final int GATHER_TICKS = 26;
    private static final int RING_INTERVAL = 3;
    private static final TagKey<Structure> RUINED_PORTALS =
            TagKey.create(Registries.STRUCTURE, ResourceLocation.withDefaultNamespace("ruined_portal"));

    private static final List<Ignition> ACTIVE = new ArrayList<>();
    private static final Map<ServerLevel, Set<BlockPos>> ARMED = new IdentityHashMap<>();

    private RiftPortalIgnition() {}

    public static boolean arm(ServerLevel level, BlockPos seed) {
        PortalInterior portal = findLinedPortal(level, seed);
        if (portal == null || !isNaturalRuinedPortal(level, seed)) {
            return false;
        }
        ARMED.computeIfAbsent(level, ignored -> new HashSet<>()).addAll(portal.positions());
        return true;
    }

    public static boolean startArmed(ServerLevel level, BlockPos seed) {
        Set<BlockPos> armed = ARMED.get(level);
        if (armed == null || !armed.remove(seed)) {
            return false;
        }
        if (armed.isEmpty()) {
            ARMED.remove(level);
        }
        return start(level, seed);
    }

    public static void clear(ServerLevel level) {
        ARMED.remove(level);
        ACTIVE.removeIf(ignition -> ignition.level() == level);
    }

    private static PortalInterior findLinedPortal(ServerLevel level, BlockPos seed) {
        for (Axis axis : new Axis[] {Axis.X, Axis.Z}) {
            Set<BlockPos> positions = interior(level, seed, axis);
            if (positions != null && positions.size() >= 6 && ringComplete(level, positions, axis)) {
                return new PortalInterior(axis, positions);
            }
        }
        return null;
    }

    private static boolean ringComplete(ServerLevel level, Set<BlockPos> interior, Axis axis) {
        Direction[] plane = axis == Axis.X
                ? new Direction[] {Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN}
                : new Direction[] {Direction.SOUTH, Direction.NORTH, Direction.UP, Direction.DOWN};

        for (BlockPos pos : interior) {
            for (Direction direction : plane) {
                if (!interior.contains(pos.relative(direction))) {
                    BlockState state = level.getBlockState(pos);
                    if (!(state.getBlock() instanceof RiftSedimentBlock) || !MultifaceBlock.hasFace(state, direction)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    static boolean canStart(ServerLevel level, BlockPos seed) {
        if (!isNaturalRuinedPortal(level, seed)) {
            return false;
        }
        for (Axis axis : new Axis[] {Axis.X, Axis.Z}) {
            Set<BlockPos> positions = interior(level, seed, axis);
            if (positions != null && positions.size() >= 6) {
                return true;
            }
        }
        return false;
    }

    static boolean start(ServerLevel level, BlockPos seed) {
        if (!isNaturalRuinedPortal(level, seed)) {
            return false;
        }

        for (Axis axis : new Axis[] {Axis.X, Axis.Z}) {
            Set<BlockPos> interior = interior(level, seed, axis);
            if (interior == null || interior.size() < 6) {
                continue;
            }

            for (Ignition running : ACTIVE) {
                if (running.level() == level
                        && running.rings().getFirst().stream().anyMatch(interior::contains)) {
                    return true;
                }
            }

            List<List<BlockPos>> rings = rings(interior);
            if (!rings.isEmpty()) {
                Vec3 center = center(interior);
                ACTIVE.add(new Ignition(level, axis, rings, center));
                level.playSound(null, seed, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 0.9F, 0.45F);
                level.playSound(null, seed, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.35F, 1.8F);
                return true;
            }
        }
        return false;
    }

    private static boolean isNaturalRuinedPortal(ServerLevel level, BlockPos pos) {
        return level.dimension() == Level.OVERWORLD
                && level.structureManager()
                        .getStructureWithPieceAt(pos, RUINED_PORTALS)
                        .isValid();
    }

    private static Set<BlockPos> interior(ServerLevel level, BlockPos seed, Axis axis) {
        Direction[] plane = axis == Axis.X
                ? new Direction[] {Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN}
                : new Direction[] {Direction.SOUTH, Direction.NORTH, Direction.UP, Direction.DOWN};
        Set<BlockPos> found = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(seed.immutable());
        found.add(seed.immutable());

        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : plane) {
                BlockPos next = current.relative(direction);
                if (found.contains(next)) {
                    continue;
                }

                BlockState state = level.getBlockState(next);
                if (state.is(Blocks.OBSIDIAN)) {
                    continue;
                }
                if (!passable(state) || found.size() >= MAX_INTERIOR) {
                    return null;
                }

                BlockPos immutable = next.immutable();
                found.add(immutable);
                queue.addLast(immutable);
            }
        }
        return found;
    }

    private static boolean passable(BlockState state) {
        return state.isAir()
                || state.getBlock() instanceof RiftFireBlock
                || state.getBlock() instanceof RiftSedimentBlock
                || state.is(Blocks.NETHER_PORTAL)
                || state.is(Blocks.FIRE);
    }

    private static List<List<BlockPos>> rings(Set<BlockPos> interior) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (BlockPos pos : interior) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        int depth = 0;
        for (BlockPos pos : interior) {
            depth = Math.max(depth, ring(pos, minX, minY, minZ, maxX, maxY, maxZ));
        }

        List<List<BlockPos>> rings = new ArrayList<>();
        for (int index = 0; index <= depth; index++) {
            rings.add(new ArrayList<>());
        }
        for (BlockPos pos : interior) {
            rings.get(ring(pos, minX, minY, minZ, maxX, maxY, maxZ)).add(pos);
        }
        rings.removeIf(List::isEmpty);
        return rings;
    }

    private static int ring(BlockPos pos, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int horizontal = minX == maxX
                ? Math.min(pos.getZ() - minZ, maxZ - pos.getZ())
                : Math.min(pos.getX() - minX, maxX - pos.getX());
        return Math.min(horizontal, Math.min(pos.getY() - minY, maxY - pos.getY()));
    }

    private static Vec3 center(Set<BlockPos> interior) {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        for (BlockPos pos : interior) {
            x += pos.getX() + 0.5;
            y += pos.getY() + 0.5;
            z += pos.getZ() + 0.5;
        }
        return new Vec3(x / interior.size(), y / interior.size(), z / interior.size());
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || ACTIVE.isEmpty()) {
            return;
        }

        Iterator<Ignition> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            Ignition ignition = iterator.next();
            if (ignition.level() == level && advance(ignition)) {
                iterator.remove();
            }
        }
    }

    private static boolean advance(Ignition ignition) {
        ServerLevel level = ignition.level();
        RandomSource random = level.random;
        ignition.tick++;
        if (ignition.tick <= GATHER_TICKS) {
            gather(ignition, random);
            return false;
        }
        if ((ignition.tick - GATHER_TICKS) % RING_INTERVAL != 0) {
            return false;
        }
        if (ignition.ring >= ignition.rings().size()) {
            settle(ignition, random);
            return true;
        }

        List<BlockPos> ring = ignition.rings().get(ignition.ring++);
        BlockState portal = Blocks.NETHER_PORTAL
                .defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_AXIS, ignition.axis());
        for (BlockPos pos : ring) {
            if (!level.getBlockState(pos).is(Blocks.NETHER_PORTAL)) {
                level.setBlock(pos, portal, Block.UPDATE_CLIENTS);
            }
            for (int index = 0; index < 4; index++) {
                level.sendParticles(
                        PoptartCoreParticles.RIFT_FIRE_FLAME.get(),
                        pos.getX() + random.nextDouble(),
                        pos.getY() + random.nextDouble(),
                        pos.getZ() + random.nextDouble(),
                        0,
                        (ignition.center().x - pos.getX() - 0.5) * 0.04,
                        0.02,
                        (ignition.center().z - pos.getZ() - 0.5) * 0.04,
                        0.8);
            }
            level.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    3,
                    0.3,
                    0.3,
                    0.3,
                    0.05);
        }

        BlockPos sound = ring.get(random.nextInt(ring.size()));
        float pitch = Math.min(2.0F, 0.7F + 0.12F * ignition.ring);
        level.playSound(null, sound, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.5F, pitch);
        level.playSound(null, sound, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 0.35F, pitch);
        return false;
    }

    private static void gather(Ignition ignition, RandomSource random) {
        ServerLevel level = ignition.level();
        float progress = ignition.tick / (float) GATHER_TICKS;
        for (BlockPos pos : ignition.rings().getFirst()) {
            if (random.nextFloat() <= 0.35F * progress) {
                Vec3 from = new Vec3(
                        pos.getX() + random.nextDouble(),
                        pos.getY() + random.nextDouble(),
                        pos.getZ() + random.nextDouble());
                Vec3 pull = ignition.center().subtract(from).scale(0.05 + 0.05 * progress);
                level.sendParticles(ParticleTypes.PORTAL, from.x, from.y, from.z, 0, pull.x, pull.y, pull.z, 1.0);
            }
        }
        if (ignition.tick % 7 == 0) {
            level.playSound(
                    null,
                    BlockPos.containing(ignition.center()),
                    SoundEvents.PORTAL_AMBIENT,
                    SoundSource.BLOCKS,
                    0.25F + 0.35F * progress,
                    0.4F + 0.5F * progress);
        }
    }

    private static void settle(Ignition ignition, RandomSource random) {
        ServerLevel level = ignition.level();
        BlockPos center = BlockPos.containing(ignition.center());
        for (List<BlockPos> ring : ignition.rings()) {
            for (BlockPos pos : ring) {
                level.sendParticles(
                        ParticleTypes.REVERSE_PORTAL,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        8,
                        0.4,
                        0.4,
                        0.4,
                        0.3);
            }
        }
        level.playSound(null, center, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.6F, 1.2F);
        level.playSound(null, center, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.9F, 0.55F);
    }

    private record PortalInterior(Axis axis, Set<BlockPos> positions) {}

    private static final class Ignition {
        private final ServerLevel level;
        private final Axis axis;
        private final List<List<BlockPos>> rings;
        private final Vec3 center;
        private int tick;
        private int ring;

        private Ignition(ServerLevel level, Axis axis, List<List<BlockPos>> rings, Vec3 center) {
            this.level = level;
            this.axis = axis;
            this.rings = rings;
            this.center = center;
        }

        private ServerLevel level() {
            return level;
        }

        private Axis axis() {
            return axis;
        }

        private List<List<BlockPos>> rings() {
            return rings;
        }

        private Vec3 center() {
            return center;
        }
    }
}
