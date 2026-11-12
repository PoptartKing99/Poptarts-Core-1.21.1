package dev.poptartking.poptartcore.navigation.network;

import dev.poptartking.poptartcore.navigation.CompassSource;
import net.minecraft.network.FriendlyByteBuf;

public record HideWaypointPayload(int index, int fallback, CompassSource source)
        implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<HideWaypointPayload> TYPE =
            new Type<>(dev.poptartking.poptartcore.PoptartCore.location("navigation/hide_waypoint"));
    public static final net.minecraft.network.codec.StreamCodec<
                    net.minecraft.network.RegistryFriendlyByteBuf, HideWaypointPayload>
            CODEC = net.minecraft.network.codec.StreamCodec.of(HideWaypointPayload::write, HideWaypointPayload::read);

    @Override
    public Type<HideWaypointPayload> type() {
        return TYPE;
    }

    public static void write(FriendlyByteBuf buf, HideWaypointPayload packet) {
        buf.writeVarInt(packet.index());
        buf.writeVarInt(packet.fallback());
        buf.writeVarInt(packet.source().ordinal());
    }

    public static HideWaypointPayload read(FriendlyByteBuf buf) {
        return new HideWaypointPayload(buf.readVarInt(), buf.readVarInt(), CompassSource.byId(buf.readVarInt()));
    }
}
