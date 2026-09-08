package dev.poptartking.poptartcore.clinker;

import net.minecraft.util.StringRepresentable;

public enum PillarSegment implements StringRepresentable {
    SHORT("short"),
    BASE("base"),
    SHAFT("shaft"),
    CAPITAL("capital");

    private final String name;

    PillarSegment(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
