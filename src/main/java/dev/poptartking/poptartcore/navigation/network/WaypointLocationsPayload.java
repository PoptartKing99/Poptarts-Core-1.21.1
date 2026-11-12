package dev.poptartking.poptartcore.navigation.network;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public record WaypointLocationsPayload(List<WaypointLocationsPayload.Location> locations)
        implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<WaypointLocationsPayload> TYPE =
            new Type<>(dev.poptartking.poptartcore.PoptartCore.location("navigation/waypoint_locations"));
    public static final net.minecraft.network.codec.StreamCodec<
                    net.minecraft.network.RegistryFriendlyByteBuf, WaypointLocationsPayload>
            CODEC = net.minecraft.network.codec.StreamCodec.of(
                    WaypointLocationsPayload::write, WaypointLocationsPayload::read);

    @Override
    public Type<WaypointLocationsPayload> type() {
        return TYPE;
    }

    public static void write(FriendlyByteBuf buf, WaypointLocationsPayload packet) {
        buf.writeVarInt(packet.locations().size());

        for (WaypointLocationsPayload.Location location : packet.locations()) {
            buf.writeGlobalPos(location.anchor());
            buf.writeDouble(location.position().x);
            buf.writeDouble(location.position().y);
            buf.writeDouble(location.position().z);
        }
    }

    public static WaypointLocationsPayload read(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        if (count < 0 || count > 4096) throw new IllegalArgumentException("Too many waypoint locations");
        List<WaypointLocationsPayload.Location> locations = new ArrayList<>();

        for (int index = 0; index < count; index++) {
            locations.add(new WaypointLocationsPayload.Location(
                    buf.readGlobalPos(), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())));
        }

        return new WaypointLocationsPayload(locations);
    }

    public static record Location(GlobalPos anchor, Vec3 position) {}
}
