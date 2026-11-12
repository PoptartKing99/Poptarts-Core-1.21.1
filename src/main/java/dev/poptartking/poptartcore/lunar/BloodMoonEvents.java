package dev.poptartking.poptartcore.lunar;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.lunar.network.BloodMoonPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class BloodMoonEvents {
    private BloodMoonEvents() {}

    @SubscribeEvent
    public static void tick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) sync(level);
    }

    static void sync(ServerLevel level) {
        var data = BloodMoonData.get(level);
        long time = level.getDayTime();
        boolean selected = data.update(time, level.getRandom());
        boolean tonight = data.isBloodMoon(time);
        Boolean previous = data.publish(tonight);
        if (previous != null && previous == tonight) return;
        var message = Component.translatable(
                tonight ? "message.poptartcore.blood_moon.rises" : "message.poptartcore.blood_moon.sets");
        if (tonight) message.withStyle(ChatFormatting.RED);
        for (ServerPlayer player : level.players()) {
            PacketDistributor.sendToPlayer(player, new BloodMoonPayload(tonight));
            if (previous != null || selected) player.sendSystemMessage(message);
        }
    }

    private static void syncPlayer(PlayerEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var level = player.serverLevel();
            boolean tonight = level.dimension() == Level.OVERWORLD
                    && BloodMoonData.get(level).isBloodMoon(level.getDayTime());
            PacketDistributor.sendToPlayer(player, new BloodMoonPayload(tonight));
        }
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        syncPlayer(event);
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        syncPlayer(event);
    }

    @SubscribeEvent
    public static void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncPlayer(event);
    }

    @SubscribeEvent
    public static void preventSleep(CanPlayerSleepEvent event) {
        if ((event.getProblem() == null || event.getProblem() == BedSleepingProblem.NOT_SAFE)
                && BloodMoonData.active(event.getLevel())) {
            event.setProblem(BedSleepingProblem.OTHER_PROBLEM);
            event.getEntity()
                    .displayClientMessage(Component.translatable("message.poptartcore.blood_moon.sleep"), true);
        }
    }

    @SubscribeEvent
    public static void wakeSleepers(CanContinueSleepingEvent event) {
        if (event.mayContinueSleeping()
                && BloodMoonData.active(event.getEntity().level())) {
            event.setContinueSleeping(false);
        }
    }
}
