package dev.poptartking.poptartcore.navigation.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record OpenLodestonePayload(BlockPos pos, String name, String icon)
        implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<OpenLodestonePayload> TYPE =
            new Type<>(dev.poptartking.poptartcore.PoptartCore.location("navigation/open_lodestone"));
    public static final net.minecraft.network.codec.StreamCodec<
                    net.minecraft.network.RegistryFriendlyByteBuf, OpenLodestonePayload>
            CODEC = net.minecraft.network.codec.StreamCodec.of(OpenLodestonePayload::write, OpenLodestonePayload::read);

    @Override
    public Type<OpenLodestonePayload> type() {
        return TYPE;
    }

    private static final int MAX_TEXT = 256;

    public static void write(FriendlyByteBuf buf, OpenLodestonePayload packet) {
        buf.writeBlockPos(packet.pos());
        buf.writeUtf(packet.name(), 256);
        buf.writeUtf(packet.icon(), 256);
    }

    public static OpenLodestonePayload read(FriendlyByteBuf buf) {
        return new OpenLodestonePayload(buf.readBlockPos(), buf.readUtf(256), buf.readUtf(256));
    }
}
