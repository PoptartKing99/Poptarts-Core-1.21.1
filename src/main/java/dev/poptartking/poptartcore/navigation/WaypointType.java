package dev.poptartking.poptartcore.navigation;

import com.mojang.serialization.Codec;
import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public enum WaypointType implements StringRepresentable {
    ORIGIN("origin", "origin"),
    RESPAWN("respawn", "bed"),
    LODESTONE("lodestone", "default");

    public static final Codec<WaypointType> CODEC = StringRepresentable.fromEnum(WaypointType::values);
    private final String name;
    private final ResourceLocation sprite;
    private final ResourceLocation selectedSprite;

    private WaypointType(String name, String sprite) {
        this.name = name;
        this.sprite = PoptartCore.location("waypoints/" + sprite);
        this.selectedSprite = PoptartCore.location("waypoints/" + sprite + "_selected");
    }

    public ResourceLocation getSprite(boolean selected) {
        return selected ? this.selectedSprite : this.sprite;
    }

    public String getTranslationKey() {
        return "poptartcore.waypoint." + this.name;
    }

    public String getSerializedName() {
        return this.name;
    }
}
