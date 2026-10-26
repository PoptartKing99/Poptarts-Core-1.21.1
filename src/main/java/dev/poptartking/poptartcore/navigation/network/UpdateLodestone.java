package dev.poptartking.poptartcore.navigation.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record UpdateLodestone(BlockPos pos, String name, String icon)
        implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {
    public static final Type<UpdateLodestone> TYPE =
            new Type<>(dev.poptartking.poptartcore.PoptartCore.location("navigation/update_lodestone"));
    public static final net.minecraft.network.codec.StreamCodec<
                    net.minecraft.network.RegistryFriendlyByteBuf, UpdateLodestone>
            CODEC = net.minecraft.network.codec.StreamCodec.of(UpdateLodestone::write, UpdateLodestone::read);

    @Override
    public Type<UpdateLodestone> type() {
        return TYPE;
    }

    private static final int MAX_TEXT = 256;

    public static void write(FriendlyByteBuf buf, UpdateLodestone packet) {
        buf.writeBlockPos(packet.pos());
        buf.writeUtf(packet.name(), 256);
        buf.writeUtf(packet.icon(), 256);
    }

    public static UpdateLodestone read(FriendlyByteBuf buf) {
        return new UpdateLodestone(buf.readBlockPos(), buf.readUtf(256), buf.readUtf(256));
    }
}
