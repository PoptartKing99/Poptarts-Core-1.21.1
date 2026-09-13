package dev.poptartking.poptartcore.beekeeping;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.animal.Bee;

public final class SmokedBees {
    private static final int SMOKED_DURATION = 300;
    private static final Map<Bee, Long> SMOKED = new WeakHashMap<>();

    private SmokedBees() {}

    public static void mark(Bee bee) {
        SMOKED.put(bee, bee.level().getGameTime() + SMOKED_DURATION);
        keepCalm(bee);
    }

    public static boolean isSmoked(Bee bee) {
        Long expiry = SMOKED.get(bee);
        if (expiry == null) {
            return false;
        }

        if (bee.level().getGameTime() > expiry) {
            SMOKED.remove(bee);
            return false;
        }
        return true;
    }

    public static void keepCalm(Bee bee) {
        bee.setRemainingPersistentAngerTime(0);
        bee.setPersistentAngerTarget(null);
        bee.setTarget(null);
        bee.setLastHurtByMob(null);
    }
}
