package dev.poptartking.poptartcore.eclipse.network;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.eclipse.EclipsePhase;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record EclipsePhasePayload(EclipsePhase phase) implements CustomPacketPayload {
    public static final Type<EclipsePhasePayload> TYPE = new Type<>(PoptartCore.location("eclipse/phase"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EclipsePhasePayload> CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeEnum(payload.phase()),
            buf -> new EclipsePhasePayload(buf.readEnum(EclipsePhase.class)));

    @Override
    public Type<EclipsePhasePayload> type() {
        return TYPE;
    }
}
