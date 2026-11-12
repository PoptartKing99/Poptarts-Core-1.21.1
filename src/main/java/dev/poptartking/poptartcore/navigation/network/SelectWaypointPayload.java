package dev.poptartking.poptartcore.navigation.network;

import dev.poptartking.poptartcore.navigation.CompassSource;
import net.minecraft.network.FriendlyByteBuf;

public record SelectWaypointPayload(int index, CompassSource source)
        implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<SelectWaypointPayload> TYPE =
            new Type<>(dev.poptartking.poptartcore.PoptartCore.location("navigation/select_waypoint"));
    public static final net.minecraft.network.codec.StreamCodec<
                    net.minecraft.network.RegistryFriendlyByteBuf, SelectWaypointPayload>
            CODEC = net.minecraft.network.codec.StreamCodec.of(
                    SelectWaypointPayload::write, SelectWaypointPayload::read);

    @Override
    public Type<SelectWaypointPayload> type() {
        return TYPE;
    }

    public static void write(FriendlyByteBuf buf, SelectWaypointPayload packet) {
        buf.writeVarInt(packet.index());
        buf.writeVarInt(packet.source().ordinal());
    }

    public static SelectWaypointPayload read(FriendlyByteBuf buf) {
        return new SelectWaypointPayload(buf.readVarInt(), CompassSource.byId(buf.readVarInt()));
    }
}
