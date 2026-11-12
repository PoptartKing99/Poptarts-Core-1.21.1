package dev.poptartking.poptartcore.eclipse;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.eclipse.network.EclipsePhasePayload;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.MinecraftServer;
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
public final class EclipseEvents {
    private EclipseEvents() {}

    public static void sync(MinecraftServer server) {
        EclipseData data = EclipseData.get(server);
        var level = server.overworld();
        if (data.update(level.getGameTime(), level.getDayTime(), level.getRandom())) {
            keepWeatherClear(level);
            server.overworld().updateSkyBrightness();
            PacketDistributor.sendToAllPlayers(
                    new EclipsePhasePayload(data.phaseAt(level.getGameTime(), level.getDayTime())));
        }
    }

    public static boolean keepWeatherClear(ServerLevel level) {
        if (EclipseLighting.phase(level).coverage() == 0) return false;

        var weather = level.getLevelData();
        if (weather.isRaining() || weather.isThundering()) {
            level.setWeatherParameters(0, 0, false, false);
        }
        level.setThunderLevel(0);
        if (level.getRainLevel(1) > 0 || level.getRainLevel(0) > 0) {
            level.setRainLevel(0);
            level.getServer()
                    .getPlayerList()
                    .broadcastAll(
                            new ClientboundBundlePacket(List.of(
                                    new ClientboundGameEventPacket(ClientboundGameEventPacket.STOP_RAINING, 0),
                                    new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 0),
                                    new ClientboundGameEventPacket(
                                            ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, 0))),
                            level.dimension());
        }
        return true;
    }

    @SubscribeEvent
    public static void tick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) {
            sync(level.getServer());
        }
    }

    private static void syncPlayer(PlayerEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var server = player.server;
            PacketDistributor.sendToPlayer(
                    player,
                    new EclipsePhasePayload(EclipseData.get(server)
                            .phaseAt(
                                    server.overworld().getGameTime(),
                                    server.overworld().getDayTime())));
        }
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        syncPlayer(event);
    }

    @SubscribeEvent
    public static void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncPlayer(event);
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        syncPlayer(event);
    }

    @SubscribeEvent
    public static void keepNormalBedtime(CanPlayerSleepEvent event) {
        if (event.getProblem() == null && EclipseLighting.keepsNormalBedtime(event.getLevel())) {
            event.setProblem(BedSleepingProblem.NOT_POSSIBLE_NOW);
        }
    }

    @SubscribeEvent
    public static void wakeAtNormalDaylight(CanContinueSleepingEvent event) {
        if (event.mayContinueSleeping()
                && event.getEntity() instanceof ServerPlayer player
                && EclipseLighting.keepsNormalBedtime(player.level())) {
            event.setContinueSleeping(false);
        }
    }
}
