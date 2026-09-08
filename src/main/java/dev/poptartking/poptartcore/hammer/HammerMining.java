package dev.poptartking.poptartcore.hammer;

import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class HammerMining {
    private static final int DESTROY_DELAY_TICKS = 5;
    private static final int HAMMER_CRACK_ID_SALT = 1212239181;
    private static final Map<UUID, MiningSession> MINING_SESSIONS = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> CALCULATING_SPEED = ThreadLocal.withInitial(() -> false);

    private HammerMining() {}

    public static void beginMining(Player player, Direction face) {
        MINING_SESSIONS.put(player.getUUID(), new MiningSession(face, !player.isShiftKeyDown()));
    }

    public static void endMining(Player player) {
        MINING_SESSIONS.remove(player.getUUID());
    }

    public static int crackId(BlockPos pos) {
        return pos.hashCode() ^ HAMMER_CRACK_ID_SALT;
    }

    public static boolean isAreaMining(Player player) {
        MiningSession session = MINING_SESSIONS.get(player.getUUID());
        return session != null
                && session.areaMining()
                && player.getMainHandItem().is(PoptartCoreTags.HAMMERS);
    }

    public static List<BlockPos> findTargets(Player player, BlockGetter level, BlockPos center) {
        MiningSession session = MINING_SESSIONS.get(player.getUUID());
        if (!isAreaMining(player) || session == null) {
            return List.of();
        }

        List<BlockPos> targets = new ArrayList<>(9);
        for (int firstOffset = -1; firstOffset <= 1; firstOffset++) {
            for (int secondOffset = -1; secondOffset <= 1; secondOffset++) {
                BlockPos target = offsetInFacePlane(center, session.face(), firstOffset, secondOffset);
                BlockState state = level.getBlockState(target);
                if (isValidTarget(player, level, target, state)) {
                    targets.add(target.immutable());
                }
            }
        }
        return targets;
    }

    public static float synchronizeDestroyProgress(
            float originalProgress, Player player, BlockGetter level, BlockPos center) {
        if (CALCULATING_SPEED.get() || !isAreaMining(player)) {
            return originalProgress;
        }

        List<BlockPos> targets = findTargets(player, level, center);
        if (targets.size() <= 1) {
            return originalProgress;
        }

        CALCULATING_SPEED.set(true);
        try {
            double totalTicks = 0.0;
            for (BlockPos target : targets) {
                float progress = level.getBlockState(target).getDestroyProgress(player, level, target);
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

    private static boolean isValidTarget(Player player, BlockGetter level, BlockPos pos, BlockState state) {
        return !state.isAir()
                && state.getFluidState().isEmpty()
                && state.getDestroySpeed(level, pos) >= 0.0F
                && state.canHarvestBlock(level, pos, player);
    }

    private static BlockPos offsetInFacePlane(BlockPos center, Direction face, int first, int second) {
        return switch (face.getAxis()) {
            case X -> center.offset(0, first, second);
            case Y -> center.offset(first, 0, second);
            case Z -> center.offset(first, second, 0);
        };
    }

    private record MiningSession(Direction face, boolean areaMining) {}
}
