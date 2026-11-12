package dev.poptartking.poptartcore.navigation.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record UpdateLodestonePayload(BlockPos pos, String name, String icon)
        implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<UpdateLodestonePayload> TYPE =
            new Type<>(dev.poptartking.poptartcore.PoptartCore.location("navigation/update_lodestone"));
    public static final net.minecraft.network.codec.StreamCodec<
                    net.minecraft.network.RegistryFriendlyByteBuf, UpdateLodestonePayload>
            CODEC = net.minecraft.network.codec.StreamCodec.of(
                    UpdateLodestonePayload::write, UpdateLodestonePayload::read);

    @Override
    public Type<UpdateLodestonePayload> type() {
        return TYPE;
    }

    private static final int MAX_TEXT = 256;

    public static void write(FriendlyByteBuf buf, UpdateLodestonePayload packet) {
        buf.writeBlockPos(packet.pos());
        buf.writeUtf(packet.name(), 256);
        buf.writeUtf(packet.icon(), 256);
    }

    public static UpdateLodestonePayload read(FriendlyByteBuf buf) {
        return new UpdateLodestonePayload(buf.readBlockPos(), buf.readUtf(256), buf.readUtf(256));
    }
}
