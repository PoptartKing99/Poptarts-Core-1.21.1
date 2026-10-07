package dev.poptartking.poptartcore.conduit;

/** Timing for the vanilla heartbeat track and the filtered single-pair clip. */
public final class ConduitHeartbeatTiming {
    public static final int TICKS_PER_SECOND = 20;
    public static final long NANOS_PER_TICK = 50_000_000L;
    public static final int FULL_TRACK_INTERVAL_TICKS = 4 * TICKS_PER_SECOND;
    public static final int FULL_TRACK_DURATION_TICKS = 91;
    public static final int SINGLE_PAIR_DURATION_TICKS = 16;
    public static final int SINGLE_PAIR_PAUSE_TICKS = TICKS_PER_SECOND;
    public static final int SINGLE_PAIR_INTERVAL_TICKS =
            SINGLE_PAIR_DURATION_TICKS + SINGLE_PAIR_PAUSE_TICKS;
    public static final double HALF_PULSE_DURATION_TICKS = 4.0;
    public static final long PLAYBACK_CLEANUP_INTERVAL_NANOS = 1_000_000_000L;

    // Measured RMS peaks from vanilla block/conduit/ambient.ogg.
    private static final double[] BEAT_PEAK_TICKS = {
        1.4, 8.0, 21.4, 27.6, 41.4, 47.6, 61.2, 67.8
    };

    private ConduitHeartbeatTiming() {}

    public static int beatCount(boolean singlePair) {
        return singlePair ? 2 : BEAT_PEAK_TICKS.length;
    }

    public static double beatPeakTicks(int index) {
        return BEAT_PEAK_TICKS[index];
    }

    public static long playbackDurationNanos(boolean singlePair) {
        return (long) (singlePair ? SINGLE_PAIR_DURATION_TICKS : FULL_TRACK_DURATION_TICKS)
                * NANOS_PER_TICK;
    }
}
