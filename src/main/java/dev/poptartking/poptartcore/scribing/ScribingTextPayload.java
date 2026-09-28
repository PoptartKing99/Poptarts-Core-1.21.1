package dev.poptartking.poptartcore.scribing;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.world.item.ItemStack;

public record ScribingTextPayload(
        int containerId, ItemStack input, String name, String lore, boolean editName, boolean editLore)
        implements CustomPacketPayload {
    public static final int MAX_NAME = 50;
    public static final int MAX_LORE = 256;
    public static final Type<ScribingTextPayload> TYPE = new Type<>(PoptartCore.location("scribing_text"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ScribingTextPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ScribingTextPayload::containerId,
            ItemStack.OPTIONAL_STREAM_CODEC,
            ScribingTextPayload::input,
            ByteBufCodecs.stringUtf8(50),
            ScribingTextPayload::name,
            ByteBufCodecs.stringUtf8(256),
            ScribingTextPayload::lore,
            ByteBufCodecs.BOOL,
            ScribingTextPayload::editName,
            ByteBufCodecs.BOOL,
            ScribingTextPayload::editLore,
            ScribingTextPayload::new);

    @Override
    public Type<ScribingTextPayload> type() {
        return TYPE;
    }
}
