package dev.poptartking.poptartcore.hammer;

import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class HammerMining {
    private static final int DESTROY_DELAY_TICKS = 5;
    private static final Map<UUID, MiningSession> CLIENT_MINING_SESSIONS = new ConcurrentHashMap<>();
    private static final Map<UUID, MiningSession> SERVER_MINING_SESSIONS = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> CALCULATING_SPEED = ThreadLocal.withInitial(() -> false);

    private HammerMining() {}

    public static void beginMining(Player player, BlockPos center, Direction face) {
        boolean areaMining =
                !player.isShiftKeyDown() && player.getMainHandItem().is(PoptartCoreTags.HAMMERS);
        List<HammerTarget> targets = new ArrayList<>(9);
        Level level = player.level();
        if (areaMining && level.isLoaded(center) && isValidTarget(player, level, center, level.getBlockState(center))) {
            for (int firstOffset = -1; firstOffset <= 1; firstOffset++) {
                for (int secondOffset = -1; secondOffset <= 1; secondOffset++) {
                    BlockPos target = offsetInFacePlane(center, face, firstOffset, secondOffset);
                    if (!level.isLoaded(target)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(target);
                    if (isValidTarget(player, level, target, state)) {
                        targets.add(new HammerTarget(target.immutable(), state));
                    }
                }
            }
        }
        sessionsFor(player)
                .put(player.getUUID(), new MiningSession(center.immutable(), areaMining, List.copyOf(targets)));
    }

    public static void endMining(Player player) {
        sessionsFor(player).remove(player.getUUID());
    }

    public static boolean isAreaMining(Player player) {
        MiningSession session = sessionsFor(player).get(player.getUUID());
        return session != null
                && session.areaMining()
                && player.getMainHandItem().is(PoptartCoreTags.HAMMERS);
    }

    public static List<BlockPos> findTargets(Player player, BlockPos center) {
        MiningSession session = sessionsFor(player).get(player.getUUID());
        if (!isAreaMining(player) || session == null || !session.center().equals(center)) {
            return List.of();
        }
        if (!player.level().isLoaded(center) || !hasOriginalCenter(player, session)) {
            return List.of();
        }

        List<BlockPos> targets = new ArrayList<>(9);
        for (HammerTarget target : session.targets()) {
            if (canBreakTarget(player, target)) {
                targets.add(target.pos());
            }
        }
        return targets;
    }

    public static float synchronizeDestroyProgress(
            float originalProgress, Player player, BlockGetter level, BlockPos center) {
        if (originalProgress <= 0.0F || CALCULATING_SPEED.get() || !isAreaMining(player)) {
            return originalProgress;
        }

        MiningSession session = sessionsFor(player).get(player.getUUID());
        if (session == null
                || !session.center().equals(center)
                || session.targets().size() <= 1) {
            return originalProgress;
        }
        List<HammerTarget> targets = session.targets();

        CALCULATING_SPEED.set(true);
        try {
            double totalTicks = 0.0;
            // Keep the original workload even if a neighbor disappears. Otherwise elapsed
            // mining time would suddenly be multiplied by a faster rate.
            for (HammerTarget target : targets) {
                float progress = target.state().getDestroyProgress(player, level, target.pos());
                if (progress > 0.0F && progress < 1.0F) {
                    totalTicks += Math.ceil(1.0 / progress);
                }
            }
            totalTicks += (double) DESTROY_DELAY_TICKS * (targets.size() - 1);
            return totalTicks > 0.0 ? (float) (1.0 / totalTicks) : originalProgress;
        } finally {
            CALCULATING_SPEED.set(false);
        }
    }

    public static List<HammerTarget> targetsForBreak(Player player, BlockPos center) {
        MiningSession session = sessionsFor(player).get(player.getUUID());
        return isAreaMining(player)
                        && session != null
                        && session.center().equals(center)
                        && hasOriginalCenter(player, session)
                ? session.targets()
                : List.of();
    }

    private static boolean hasOriginalCenter(Player player, MiningSession session) {
        for (HammerTarget target : session.targets()) {
            if (target.pos().equals(session.center())) {
                return canBreakTarget(player, target);
            }
        }
        return false;
    }

    public static boolean canBreakTarget(Player player, HammerTarget target) {
        Level level = player.level();
        if (!target.isValid() || !level.isLoaded(target.pos())) {
            return false;
        }
        BlockState current = level.getBlockState(target.pos());
        if (current != target.state()) {
            target.invalidate();
            return false;
        }
        return level.getWorldBorder().isWithinBounds(target.pos())
                && (!(level instanceof ServerLevel serverLevel) || serverLevel.mayInteract(player, target.pos()))
                && isValidTarget(player, level, target.pos(), current);
    }

    public static void onBlockChanged(Level level, BlockPos pos) {
        Map<UUID, MiningSession> sessions = level.isClientSide ? CLIENT_MINING_SESSIONS : SERVER_MINING_SESSIONS;
        if (sessions.isEmpty()) {
            return;
        }
        for (Player player : level.players()) {
            MiningSession session = sessions.get(player.getUUID());
            if (session != null) {
                for (HammerTarget target : session.targets()) {
                    if (target.pos().equals(pos)) {
                        target.invalidate();
                    }
                }
            }
        }
    }

    private static boolean isValidTarget(Player player, BlockGetter level, BlockPos pos, BlockState state) {
        return !state.isAir()
                && state.getFluidState().isEmpty()
                && state.getDestroySpeed(level, pos) >= 0.0F
                && !state.is(PoptartCoreTags.HAMMER_NO_SPREAD)
                && state.canHarvestBlock(level, pos, player);
    }

    private static BlockPos offsetInFacePlane(BlockPos center, Direction face, int first, int second) {
        return switch (face.getAxis()) {
            case X -> center.offset(0, first, second);
            case Y -> center.offset(first, 0, second);
            case Z -> center.offset(first, second, 0);
        };
    }

    private static Map<UUID, MiningSession> sessionsFor(Player player) {
        return player.level().isClientSide ? CLIENT_MINING_SESSIONS : SERVER_MINING_SESSIONS;
    }

    private record MiningSession(BlockPos center, boolean areaMining, List<HammerTarget> targets) {}
}
