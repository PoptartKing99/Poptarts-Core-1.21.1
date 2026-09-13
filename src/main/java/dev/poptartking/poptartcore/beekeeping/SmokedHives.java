package dev.poptartking.poptartcore.beekeeping;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class SmokedHives {
    private static final int SMOKED_DURATION = 300;
    private static final Map<Level, Map<Long, Long>> SMOKED = new WeakHashMap<>();

    private SmokedHives() {}

    public static void mark(Level level, BlockPos pos) {
        SMOKED.computeIfAbsent(level, key -> new HashMap<>()).put(pos.asLong(), level.getGameTime() + SMOKED_DURATION);
    }

    public static boolean isSmoked(Level level, BlockPos pos) {
        Map<Long, Long> smokedHives = SMOKED.get(level);
        if (smokedHives == null) {
            return false;
        }

        Long expiry = smokedHives.get(pos.asLong());
        if (expiry == null) {
            return false;
        }

        if (level.getGameTime() > expiry) {
            smokedHives.remove(pos.asLong());
            return false;
        }
        return true;
    }
}
