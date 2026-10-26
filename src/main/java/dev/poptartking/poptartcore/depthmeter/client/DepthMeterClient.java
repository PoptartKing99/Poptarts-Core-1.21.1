package dev.poptartking.poptartcore.depthmeter.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.depthmeter.DepthMeter;
import dev.poptartking.poptartcore.mixin.accessor.DepthMeterGuiAccessor;
import dev.poptartking.poptartcore.registry.PoptartToolItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class DepthMeterClient {
    private DepthMeterClient() {}

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        var level = mc.level;
        if (player == null
                || level == null
                || mc.options.hideGui
                || mc.screen != null
                || player.isSpectator()
                || ((DepthMeterGuiAccessor) mc.gui).poptartcore$overlayMessageTime() > 0) return;
        boolean compass = DepthMeter.carries(player, Items.COMPASS);
        boolean depth = player.getBlockY() >= level.getMinBuildHeight()
                && DepthMeter.carries(player, PoptartToolItems.DEPTH_METER.get());
        boolean clock = DepthMeter.carries(player, Items.CLOCK);
        var running = dev.poptartking.poptartcore.timekeeping.client.TimekeepingClient.runningClock(player);
        if (!compass && !depth && !clock) return;
        int elevation = player.getBlockY() - level.getSeaLevel();
        Component message = compass && depth
                ? Component.translatable(
                        "poptartcore.depth_meter.coordinates", player.getBlockX(), elevation, player.getBlockZ())
                : compass
                        ? Component.translatable(
                                "poptartcore.compass.coordinates", player.getBlockX(), player.getBlockZ())
                        : Component.literal(Integer.toString(Math.abs(elevation)));
        if (!compass && !depth) message = null;
        if (clock) {
            var time = Component.literal(dev.poptartking.poptartcore.timekeeping.client.ClockHud.getTime(level));
            message = message == null ? time : Component.translatable("poptartcore.readout.combined", message, time);
        }
        if (!running.isEmpty())
            message = Component.translatable(
                            "poptartcore.stopwatch.running",
                            dev.poptartking.poptartcore.timekeeping.Stopwatch.format(
                                    dev.poptartking.poptartcore.timekeeping.Stopwatch.getElapsed(level, running)))
                    .withStyle(net.minecraft.ChatFormatting.GOLD);
        var graphics = event.getGuiGraphics();
        graphics.drawCenteredString(mc.font, message, graphics.guiWidth() / 2, graphics.guiHeight() - 72, 0xFFFFFFFF);
    }

    @EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent
        public static void setup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> ItemProperties.register(
                    PoptartToolItems.DEPTH_METER.get(), PoptartCore.location("depth"), (stack, level, holder, seed) -> {
                        var carrier = holder != null ? holder : stack.getEntityRepresentation();
                        return carrier == null
                                ? 0F
                                : DepthMeter.compute(level != null ? level : carrier.level(), carrier.getBlockY());
                    }));
        }
    }
}
