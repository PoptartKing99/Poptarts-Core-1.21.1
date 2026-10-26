package dev.poptartking.poptartcore.navigation;

public enum CompassSource {
    HELD,
    CARRIED,
    ATTACHED;

    private static final CompassSource[] VALUES = values();

    public static CompassSource byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : HELD;
    }
}
