package dev.poptartking.poptartcore.navigation.network;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public record WaypointLocations(List<WaypointLocations.Location> locations)
        implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<WaypointLocations> TYPE =
            new Type<>(dev.poptartking.poptartcore.PoptartCore.location("navigation/waypoint_locations"));
    public static final net.minecraft.network.codec.StreamCodec<
                    net.minecraft.network.RegistryFriendlyByteBuf, WaypointLocations>
            CODEC = net.minecraft.network.codec.StreamCodec.of(WaypointLocations::write, WaypointLocations::read);

    @Override
    public Type<WaypointLocations> type() {
        return TYPE;
    }

    public static void write(FriendlyByteBuf buf, WaypointLocations packet) {
        buf.writeVarInt(packet.locations().size());

        for (WaypointLocations.Location location : packet.locations()) {
            buf.writeGlobalPos(location.anchor());
            buf.writeDouble(location.position().x);
            buf.writeDouble(location.position().y);
            buf.writeDouble(location.position().z);
        }
    }

    public static WaypointLocations read(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        if (count < 0 || count > 4096) throw new IllegalArgumentException("Too many waypoint locations");
        List<WaypointLocations.Location> locations = new ArrayList<>();

        for (int index = 0; index < count; index++) {
            locations.add(new WaypointLocations.Location(
                    buf.readGlobalPos(), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())));
        }

        return new WaypointLocations(locations);
    }

    public static record Location(GlobalPos anchor, Vec3 position) {}
}
