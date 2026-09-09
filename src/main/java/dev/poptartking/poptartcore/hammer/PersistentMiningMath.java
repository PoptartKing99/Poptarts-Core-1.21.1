package dev.poptartking.poptartcore.hammer;

public final class PersistentMiningMath {
    private PersistentMiningMath() {}

    public static int resumedStart(float savedFraction, float rate, int gameTicks, int currentStart) {
        if (savedFraction <= 0.0F || rate <= 0.0F) {
            return currentStart;
        }

        int earnedTicks = Math.round(Math.min(savedFraction, 1.0F) / rate);
        return gameTicks - Math.max(0, earnedTicks - 1);
    }

    public static float leastProgress(float current, float candidate) {
        return Math.min(current, candidate);
    }

    public static float record(float current, float reported) {
        return Math.min(1.0F, Math.max(current, reported));
    }

    public static float accrue(float current, float amount) {
        return Math.min(1.0F, current + amount);
    }

    public static float decay(float current, float rate, float decayRatio, float maximumPerTick) {
        return current - Math.min(rate * decayRatio, maximumPerTick * decayRatio);
    }
}
