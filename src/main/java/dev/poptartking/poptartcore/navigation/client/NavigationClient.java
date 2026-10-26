package dev.poptartking.poptartcore.navigation.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.navigation.client.hud.CompassHud;
import dev.poptartking.poptartcore.navigation.client.hud.HudTime;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class NavigationClient {
    private NavigationClient() {}

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        var timer = Minecraft.getInstance().getTimer();
        CompassHud.render(
                event.getGuiGraphics(),
                new HudTime(timer.getGameTimeDeltaPartialTick(false), timer.getRealtimeDeltaTicks()));
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        CompassHud.reset();
        ClientWaypoints.clear();
    }
}
