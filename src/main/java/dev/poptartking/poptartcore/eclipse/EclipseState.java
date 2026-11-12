package dev.poptartking.poptartcore.eclipse;

import net.minecraft.util.RandomSource;

/** Timed previews use elapsed game time. Natural eclipses follow the world's calendar. */
public sealed interface EclipseState {
    EclipsePhase phaseAt(long gameTime, long dayTime);

    record Off() implements EclipseState {
        @Override
        public EclipsePhase phaseAt(long gameTime, long dayTime) {
            return EclipsePhase.CLEAR;
        }
    }

    record Preview(EclipsePhase phase) implements EclipseState {
        public Preview {
            java.util.Objects.requireNonNull(phase);
        }

        @Override
        public EclipsePhase phaseAt(long gameTime, long dayTime) {
            return phase;
        }
    }

    record Running(long startedAt, int durationTicks) implements EclipseState {
        public Running {
            if (durationTicks <= 0) throw new IllegalArgumentException("Eclipse duration must be positive");
        }

        @Override
        public EclipsePhase phaseAt(long gameTime, long dayTime) {
            long elapsed = Math.max(0, gameTime - startedAt);
            return elapsed >= durationTicks
                    ? EclipsePhase.CLEAR
                    : EclipsePhase.cyclePhase((int) (elapsed * 7 / durationTicks));
        }
    }

    sealed interface Daily extends EclipseState {
        long day();

        Daily advance(RandomSource random);

        Daily withDay(long day);

        /** Waiting periods and event dates advance at midnight; coverage follows time of day. */
        static long dayAt(long dayTime) {
            return Math.floorDiv(dayTime, 24_000) + (Math.floorMod(dayTime, 24_000) >= 18_000 ? 1 : 0);
        }
    }

    record Waiting(long day, int clearDaysRemaining) implements Daily {
        public static final int MIN_CLEAR_DAYS = 20;
        public static final int MAX_CLEAR_DAYS = 30;

        public Waiting {
            if (clearDaysRemaining < 1 || clearDaysRemaining > MAX_CLEAR_DAYS) {
                throw new IllegalArgumentException("Invalid remaining clear days: " + clearDaysRemaining);
            }
        }

        public static Waiting start(long dayTime, RandomSource random) {
            return scheduled(Daily.dayAt(dayTime), random);
        }

        private static Waiting scheduled(long day, RandomSource random) {
            return new Waiting(day, MIN_CLEAR_DAYS + random.nextInt(MAX_CLEAR_DAYS - MIN_CLEAR_DAYS + 1));
        }

        @Override
        public EclipsePhase phaseAt(long gameTime, long dayTime) {
            return EclipsePhase.CLEAR;
        }

        @Override
        public Daily withDay(long day) {
            return new Waiting(day, clearDaysRemaining);
        }

        @Override
        public Daily advance(RandomSource random) {
            return clearDaysRemaining == 1 ? new Daylight(day + 1) : new Waiting(day + 1, clearDaysRemaining - 1);
        }
    }

    record Daylight(long day) implements Daily {
        private static final int START_TIME = 1_000;
        // Exclusive end times for the seven covered phases in eclipse atlas order.
        private static final int[] PHASE_END_TIMES = {2_600, 4_200, 5_800, 6_200, 8_100, 10_000, 12_000};

        @Override
        public EclipsePhase phaseAt(long gameTime, long dayTime) {
            if (Daily.dayAt(dayTime) != day) return EclipsePhase.CLEAR;
            int time = Math.floorMod(dayTime, 24_000);
            if (time < START_TIME) return EclipsePhase.CLEAR;
            for (int index = 0; index < PHASE_END_TIMES.length; index++) {
                if (time < PHASE_END_TIMES[index]) return EclipsePhase.cyclePhase(index);
            }
            return EclipsePhase.CLEAR;
        }

        @Override
        public Daily withDay(long day) {
            return new Daylight(day);
        }

        @Override
        public Daily advance(RandomSource random) {
            return Waiting.scheduled(day + 1, random);
        }
    }
}
