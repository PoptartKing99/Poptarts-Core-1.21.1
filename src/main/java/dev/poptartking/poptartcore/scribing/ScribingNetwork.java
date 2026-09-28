package dev.poptartking.poptartcore.scribing;

import dev.poptartking.poptartcore.PoptartCore;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ScribingNetwork {
    private ScribingNetwork() {}

    public static void register(IEventBus bus) {
        bus.addListener(ScribingNetwork::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(PoptartCore.MOD_ID)
                .playToServer(
                        ScribingTextPayload.TYPE,
                        ScribingTextPayload.STREAM_CODEC,
                        (payload, context) -> context.enqueueWork(() -> {
                            if (context.player().containerMenu instanceof ScribingTableMenu menu
                                    && menu.containerId == payload.containerId()) {
                                menu.setScribedText(
                                        payload.input(),
                                        payload.name(),
                                        payload.lore(),
                                        payload.editName(),
                                        payload.editLore());
                            }
                        }));
    }
}
