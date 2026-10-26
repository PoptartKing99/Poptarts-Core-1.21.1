package dev.poptartking.poptartcore.navigation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.GlobalPos;
import org.jetbrains.annotations.Nullable;

public record CompassWaypoints(
        List<Waypoint> waypoints, int selected, List<GlobalPos> hidden, boolean navigationHidden) {
    public static final CompassWaypoints EMPTY = new CompassWaypoints(List.of(), 0, List.of(), false);
    public static final Codec<CompassWaypoints> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Waypoint.CODEC.listOf().fieldOf("waypoints").forGetter(CompassWaypoints::waypoints),
                    Codec.INT.optionalFieldOf("selected", 0).forGetter(CompassWaypoints::selected),
                    GlobalPos.CODEC
                            .listOf()
                            .optionalFieldOf("hidden", List.of())
                            .forGetter(CompassWaypoints::hidden),
                    Codec.BOOL
                            .optionalFieldOf("navigation_hidden", false)
                            .forGetter(CompassWaypoints::navigationHidden))
            .apply(instance, CompassWaypoints::new));

    public CompassWaypoints {
        waypoints = List.copyOf(waypoints);
        hidden = List.copyOf(hidden);
    }

    public boolean isEmpty() {
        return this.waypoints.isEmpty();
    }

    public int size() {
        return this.waypoints.size();
    }

    public int getSelectedIndex() {
        return this.waypoints.isEmpty() ? 0 : Math.floorMod(this.selected, this.waypoints.size());
    }

    @Nullable
    public Waypoint getSelected() {
        return this.waypoints.isEmpty() ? null : this.waypoints.get(this.getSelectedIndex());
    }

    @Nullable
    public Waypoint get(int index) {
        return index >= 0 && index < this.waypoints.size() ? this.waypoints.get(index) : null;
    }

    public boolean isHidden(int index) {
        Waypoint waypoint = this.get(index);
        return waypoint != null && this.hidden.contains(waypoint.pos());
    }

    public int getFirstVisible() {
        for (int index = 0; index < this.waypoints.size(); index++) {
            if (!this.isHidden(index)) {
                return index;
            }
        }

        return 0;
    }

    public int indexOf(WaypointType type) {
        for (int index = 0; index < this.waypoints.size(); index++) {
            if (this.waypoints.get(index).type() == type) {
                return index;
            }
        }

        return -1;
    }

    public CompassWaypoints toggleHidden(int index) {
        Waypoint waypoint = this.get(index);
        if (waypoint == null) {
            return this;
        } else {
            List<GlobalPos> next = new ArrayList<>(this.hidden);
            if (!next.remove(waypoint.pos())) {
                next.add(waypoint.pos());
            }

            return new CompassWaypoints(this.waypoints, this.selected, next, this.navigationHidden);
        }
    }

    public CompassWaypoints withSelected(int index) {
        return new CompassWaypoints(this.waypoints, index, this.hidden, this.navigationHidden);
    }

    public CompassWaypoints withNavigationHidden(boolean navigationHidden) {
        return new CompassWaypoints(this.waypoints, this.selected, this.hidden, navigationHidden);
    }
}
