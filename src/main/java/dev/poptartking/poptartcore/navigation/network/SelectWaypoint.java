package dev.poptartking.poptartcore.navigation.network;

import dev.poptartking.poptartcore.navigation.CompassSource;
import net.minecraft.network.FriendlyByteBuf;

public record SelectWaypoint(int index, CompassSource source)
        implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<SelectWaypoint> TYPE =
            new Type<>(dev.poptartking.poptartcore.PoptartCore.location("navigation/select_waypoint"));
    public static final net.minecraft.network.codec.StreamCodec<
                    net.minecraft.network.RegistryFriendlyByteBuf, SelectWaypoint>
            CODEC = net.minecraft.network.codec.StreamCodec.of(SelectWaypoint::write, SelectWaypoint::read);

    @Override
    public Type<SelectWaypoint> type() {
        return TYPE;
    }

    public static void write(FriendlyByteBuf buf, SelectWaypoint packet) {
        buf.writeVarInt(packet.index());
        buf.writeVarInt(packet.source().ordinal());
    }

    public static SelectWaypoint read(FriendlyByteBuf buf) {
        return new SelectWaypoint(buf.readVarInt(), CompassSource.byId(buf.readVarInt()));
    }
}
