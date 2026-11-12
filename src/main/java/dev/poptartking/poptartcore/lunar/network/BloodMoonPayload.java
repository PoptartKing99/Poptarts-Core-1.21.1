package dev.poptartking.poptartcore.lunar.network;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.lunar.client.BloodMoonVisuals;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public record BloodMoonPayload(boolean tonight) implements CustomPacketPayload {
    public static final Type<BloodMoonPayload> TYPE = new Type<>(PoptartCore.location("lunar/blood_moon"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BloodMoonPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, BloodMoonPayload::tonight, BloodMoonPayload::new);

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(PoptartCore.MOD_ID)
                .playToClient(
                        TYPE,
                        CODEC,
                        (payload, context) -> context.enqueueWork(() -> BloodMoonVisuals.accept(payload.tonight())));
    }

    @Override
    public Type<BloodMoonPayload> type() {
        return TYPE;
    }
}
