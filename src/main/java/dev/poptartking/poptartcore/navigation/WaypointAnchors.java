package dev.poptartking.poptartcore.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class WaypointAnchors {
    private static final WaypointAnchors.Tracker FIXED = new WaypointAnchors.Tracker() {};
    private static WaypointAnchors.Tracker tracker = FIXED;

    private WaypointAnchors() {}

    public static void setTracker(WaypointAnchors.Tracker tracker) {
        WaypointAnchors.tracker = tracker;
    }

    public static WaypointAnchors.Tracker getTracker() {
        return tracker;
    }

    public static boolean isTracking() {
        return tracker != FIXED;
    }

    public static Vec3 getWorldPosition(Level level, BlockPos anchor) {
        Vec3 located = tracker.isMovable(level, anchor) ? tracker.locate(level, anchor, 1.0F) : null;
        return located != null ? located : Vec3.atCenterOf(anchor);
    }

    @Nullable
    public static Vec3 locate(ServerLevel level, Waypoint waypoint) {
        BlockPos anchor = waypoint.pos().pos();
        return tracker.isMovable(level, anchor) ? tracker.locate(level, waypoint) : Vec3.atCenterOf(anchor);
    }

    public interface Tracker {
        default boolean isMovable(Level level, BlockPos anchor) {
            return false;
        }

        @Nullable
        default Vec3 locate(Level level, BlockPos anchor, float partialTick) {
            return null;
        }

        @Nullable
        default Vec3 locate(ServerPlayer player, Waypoint waypoint) {
            return null;
        }

        @Nullable
        default Vec3 locate(ServerLevel level, Waypoint waypoint) {
            return null;
        }
    }
}
