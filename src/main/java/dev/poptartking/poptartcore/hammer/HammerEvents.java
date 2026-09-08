package dev.poptartking.poptartcore.hammer;

import dev.poptartking.poptartcore.PoptartCore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class HammerEvents {
    private HammerEvents() {}

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
        HammerMining.endMining(event.getEntity());
    }
}
