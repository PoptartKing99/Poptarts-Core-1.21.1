package dev.poptartking.poptartcore.beekeeping;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class SmokedHives {
    private static final int SMOKED_DURATION = 300;
    private static final Map<BlockEntity, Long> SMOKED = new WeakHashMap<>();

    private SmokedHives() {}

    public static void mark(Level level, BlockPos pos) {
        BlockEntity hive = level.getBlockEntity(pos);
        if (hive != null) SMOKED.put(hive, level.getGameTime() + SMOKED_DURATION);
    }

    public static boolean isSmoked(Level level, BlockPos pos) {
        if (level.isClientSide) return false;
        BlockEntity hive = level.getBlockEntity(pos);
        if (hive == null) return false;

        Long expiry = SMOKED.get(hive);
        if (expiry == null) {
            return false;
        }

        if (level.getGameTime() > expiry) {
            SMOKED.remove(hive);
            return false;
        }
        return true;
    }
}
