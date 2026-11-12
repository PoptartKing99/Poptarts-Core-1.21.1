package dev.poptartking.poptartcore.navigation.client;

import dev.poptartking.poptartcore.navigation.WaypointAnchors;
import dev.poptartking.poptartcore.navigation.network.WaypointLocationsPayload;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class ClientWaypoints {
    private static final Map<GlobalPos, Vec3> LOCATIONS = new HashMap<>();

    public static void clear() {
        LOCATIONS.clear();
    }

    private ClientWaypoints() {}

    public static void accept(WaypointLocationsPayload packet) {
        LOCATIONS.clear();

        for (WaypointLocationsPayload.Location location : packet.locations()) {
            LOCATIONS.put(location.anchor(), location.position());
        }
    }

    @Nullable
    public static Vec3 locate(Level level, GlobalPos anchor, float partialTick) {
        BlockPos pos = anchor.pos();
        WaypointAnchors.Tracker tracker = WaypointAnchors.getTracker();
        if (!tracker.isMovable(level, pos)) {
            return Vec3.atCenterOf(pos);
        } else {
            Vec3 live = tracker.locate(level, pos, partialTick);
            return live != null ? live : LOCATIONS.get(anchor);
        }
    }

    @Nullable
    public static GlobalPos getTarget(Level level, GlobalPos anchor) {
        if (!anchor.dimension().equals(level.dimension())) {
            return anchor;
        } else {
            Vec3 position = locate(level, anchor, 1.0F);
            return position == null ? null : GlobalPos.of(anchor.dimension(), BlockPos.containing(position));
        }
    }
}
