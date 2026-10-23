package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public record UtilityInputPayload(boolean held) implements CustomPacketPayload {
    public static final Type<UtilityInputPayload> TYPE = new Type<>(PoptartCore.location("utility_input"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UtilityInputPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, UtilityInputPayload::held, UtilityInputPayload::new);

    @Override
    public Type<UtilityInputPayload> type() {
        return TYPE;
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(PoptartCore.MOD_ID)
                .playToServer(
                        TYPE,
                        STREAM_CODEC,
                        (payload, context) ->
                                context.enqueueWork(() -> UtilityWield.input(context.player(), payload.held())));
    }
}
