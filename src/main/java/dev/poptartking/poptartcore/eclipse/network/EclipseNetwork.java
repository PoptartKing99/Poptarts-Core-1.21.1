package dev.poptartking.poptartcore.eclipse.network;

import dev.poptartking.poptartcore.PoptartCore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class EclipseNetwork {
    private EclipseNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(PoptartCore.MOD_ID)
                .playToClient(
                        EclipsePhasePayload.TYPE,
                        EclipsePhasePayload.CODEC,
                        (payload, context) -> context.enqueueWork(() ->
                                dev.poptartking.poptartcore.eclipse.client.EclipseVisuals.accept(payload.phase())));
    }
}
