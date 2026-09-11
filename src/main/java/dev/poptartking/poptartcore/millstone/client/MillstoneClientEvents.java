package dev.poptartking.poptartcore.millstone.client;

import dev.poptartking.poptartcore.PoptartCore;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class MillstoneClientEvents {
    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        MillstoneEffects.cleanup();
    }

    @SubscribeEvent
    public static void modelsReloaded(ModelEvent.BakingCompleted event) {
        MillstoneEffects.clearColors();
    }
}
