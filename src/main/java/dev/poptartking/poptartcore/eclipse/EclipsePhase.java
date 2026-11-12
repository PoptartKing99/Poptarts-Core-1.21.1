package dev.poptartking.poptartcore.eclipse;

import java.util.Arrays;
import java.util.Optional;

/** Eclipse atlas phases and an explicit vanilla-sun state, without the separate Old Sun phase. */
public enum EclipsePhase {
    CLEAR("clear", 0, 0, 15),
    WANING_GIBBOUS("waning_gibbous", 1, 1, 13),
    WANING_HALF("waning_half", 2, 2, 10),
    WANING_CRESCENT("waning_crescent", 3, 3, 7),
    TOTAL("total", 4, 4, 2),
    WAXING_CRESCENT("waxing_crescent", 3, 5, 7),
    WAXING_HALF("waxing_half", 2, 6, 10),
    WAXING_GIBBOUS("waxing_gibbous", 1, 7, 13),
    NEW("new", 0, 0, 14);

    private final String id;
    private final int coverage;
    private final int frame;
    private final int skylight;

    EclipsePhase(String id, int coverage, int frame, int skylight) {
        this.id = id;
        this.coverage = coverage;
        this.frame = frame;
        this.skylight = skylight;
    }

    public String id() {
        return id;
    }

    public int coverage() {
        return coverage;
    }

    public int frame() {
        return frame;
    }

    public int skylight() {
        return skylight;
    }

    public static Optional<EclipsePhase> byName(String name) {
        return Arrays.stream(values()).filter(phase -> phase.id.equals(name)).findFirst();
    }

    static EclipsePhase cyclePhase(int index) {
        return values()[index + 1];
    }
}
