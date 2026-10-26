package dev.poptartking.poptartcore.navigation.network;

import dev.poptartking.poptartcore.navigation.CompassSource;
import net.minecraft.network.FriendlyByteBuf;

public record ToggleNavigation(CompassSource source)
        implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<ToggleNavigation> TYPE =
            new Type<>(dev.poptartking.poptartcore.PoptartCore.location("navigation/toggle_navigation"));
    public static final net.minecraft.network.codec.StreamCodec<
                    net.minecraft.network.RegistryFriendlyByteBuf, ToggleNavigation>
            CODEC = net.minecraft.network.codec.StreamCodec.of(ToggleNavigation::write, ToggleNavigation::read);

    @Override
    public Type<ToggleNavigation> type() {
        return TYPE;
    }

    public static void write(FriendlyByteBuf buf, ToggleNavigation packet) {
        buf.writeVarInt(packet.source().ordinal());
    }

    public static ToggleNavigation read(FriendlyByteBuf buf) {
        return new ToggleNavigation(CompassSource.byId(buf.readVarInt()));
    }
}
