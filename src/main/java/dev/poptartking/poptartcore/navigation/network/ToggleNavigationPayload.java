package dev.poptartking.poptartcore.navigation.network;

import dev.poptartking.poptartcore.navigation.CompassSource;
import net.minecraft.network.FriendlyByteBuf;

public record ToggleNavigationPayload(CompassSource source)
        implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<ToggleNavigationPayload> TYPE =
            new Type<>(dev.poptartking.poptartcore.PoptartCore.location("navigation/toggle_navigation"));
    public static final net.minecraft.network.codec.StreamCodec<
                    net.minecraft.network.RegistryFriendlyByteBuf, ToggleNavigationPayload>
            CODEC = net.minecraft.network.codec.StreamCodec.of(
                    ToggleNavigationPayload::write, ToggleNavigationPayload::read);

    @Override
    public Type<ToggleNavigationPayload> type() {
        return TYPE;
    }

    public static void write(FriendlyByteBuf buf, ToggleNavigationPayload packet) {
        buf.writeVarInt(packet.source().ordinal());
    }

    public static ToggleNavigationPayload read(FriendlyByteBuf buf) {
        return new ToggleNavigationPayload(CompassSource.byId(buf.readVarInt()));
    }
}
