package dev.poptartking.poptartcore.navigation.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record OpenLodestone(BlockPos pos, String name, String icon)
        implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<OpenLodestone> TYPE =
            new Type<>(dev.poptartking.poptartcore.PoptartCore.location("navigation/open_lodestone"));
    public static final net.minecraft.network.codec.StreamCodec<
                    net.minecraft.network.RegistryFriendlyByteBuf, OpenLodestone>
            CODEC = net.minecraft.network.codec.StreamCodec.of(OpenLodestone::write, OpenLodestone::read);

    @Override
    public Type<OpenLodestone> type() {
        return TYPE;
    }

    private static final int MAX_TEXT = 256;

    public static void write(FriendlyByteBuf buf, OpenLodestone packet) {
        buf.writeBlockPos(packet.pos());
        buf.writeUtf(packet.name(), 256);
        buf.writeUtf(packet.icon(), 256);
    }

    public static OpenLodestone read(FriendlyByteBuf buf) {
        return new OpenLodestone(buf.readBlockPos(), buf.readUtf(256), buf.readUtf(256));
    }
}
