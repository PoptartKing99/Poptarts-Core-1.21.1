package dev.poptartking.poptartcore.navigation.network;

import dev.poptartking.poptartcore.navigation.CompassSource;
import net.minecraft.network.FriendlyByteBuf;

public record HideWaypoint(int index, int fallback, CompassSource source)
        implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<HideWaypoint> TYPE =
            new Type<>(dev.poptartking.poptartcore.PoptartCore.location("navigation/hide_waypoint"));
    public static final net.minecraft.network.codec.StreamCodec<
                    net.minecraft.network.RegistryFriendlyByteBuf, HideWaypoint>
            CODEC = net.minecraft.network.codec.StreamCodec.of(HideWaypoint::write, HideWaypoint::read);

    @Override
    public Type<HideWaypoint> type() {
        return TYPE;
    }

    public static void write(FriendlyByteBuf buf, HideWaypoint packet) {
        buf.writeVarInt(packet.index());
        buf.writeVarInt(packet.fallback());
        buf.writeVarInt(packet.source().ordinal());
    }

    public static HideWaypoint read(FriendlyByteBuf buf) {
        return new HideWaypoint(buf.readVarInt(), buf.readVarInt(), CompassSource.byId(buf.readVarInt()));
    }
}
