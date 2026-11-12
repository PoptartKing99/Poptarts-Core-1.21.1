package dev.poptartking.poptartcore.navigation;

import dev.poptartking.poptartcore.navigation.network.WaypointLocationsPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class WaypointTracking {
    private static final Map<ServerPlayer, Map<GlobalPos, Vec3>> SENT = new java.util.IdentityHashMap<>();

    public static void forget(ServerPlayer player) {
        SENT.remove(player);
    }

    private WaypointTracking() {}

    public static void tick(ServerPlayer player) {
        if (WaypointAnchors.isTracking() && player.tickCount % 10 == 0) {
            Map<GlobalPos, Vec3> located = new HashMap<>();
            Inventory inventory = player.getInventory();

            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (stack.is(Items.COMPASS)) {
                    locate(player, stack, located);
                }
            }

            locate(player, CompassAccess.carried(player, Items.COMPASS), located);
            locate(player, CompassAccess.attached(player, Items.COMPASS), located);
            Map<GlobalPos, Vec3> previous = SENT.get(player);
            if (!located.equals(previous) && (!located.isEmpty() || previous != null)) {
                SENT.put(player, located);
                List<WaypointLocationsPayload.Location> locations = new ArrayList<>(located.size());

                for (Entry<GlobalPos, Vec3> entry : located.entrySet()) {
                    locations.add(new WaypointLocationsPayload.Location(entry.getKey(), entry.getValue()));
                }

                PacketDistributor.sendToPlayer(player, new WaypointLocationsPayload(locations));
            }
        }
    }

    private static void locate(ServerPlayer player, ItemStack compass, Map<GlobalPos, Vec3> located) {
        if (compass.is(Items.COMPASS)) {
            WaypointAnchors.Tracker tracker = WaypointAnchors.getTracker();
            Level level = player.level();

            for (Waypoint waypoint : Waypoints.get(compass).waypoints()) {
                GlobalPos anchor = waypoint.pos();
                if (!located.containsKey(anchor)
                        && anchor.dimension().equals(level.dimension())
                        && tracker.isMovable(level, anchor.pos())) {
                    Vec3 position = tracker.locate(player, waypoint);
                    if (position != null) {
                        located.put(anchor, position);
                    }
                }
            }
        }
    }
}
