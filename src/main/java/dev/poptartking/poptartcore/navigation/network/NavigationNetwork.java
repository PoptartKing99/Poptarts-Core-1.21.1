package dev.poptartking.poptartcore.navigation.network;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.navigation.Lodestones;
import dev.poptartking.poptartcore.navigation.Waypoints;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class NavigationNetwork {
    private NavigationNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(PoptartCore.MOD_ID);
        registrar.playToServer(
                UpdateLodestonePayload.TYPE,
                UpdateLodestonePayload.CODEC,
                (p, c) -> c.enqueueWork(() -> Lodestones.onUpdate(p, (ServerPlayer) c.player())));
        registrar.playToServer(
                SelectWaypointPayload.TYPE,
                SelectWaypointPayload.CODEC,
                (p, c) -> c.enqueueWork(() -> Waypoints.onSelect(p, (ServerPlayer) c.player())));
        registrar.playToServer(
                HideWaypointPayload.TYPE,
                HideWaypointPayload.CODEC,
                (p, c) -> c.enqueueWork(() -> Waypoints.onHide(p, (ServerPlayer) c.player())));
        registrar.playToServer(
                ToggleNavigationPayload.TYPE,
                ToggleNavigationPayload.CODEC,
                (p, c) -> c.enqueueWork(() -> Waypoints.onToggleNavigation(p, (ServerPlayer) c.player())));
        registrar.playToClient(
                OpenLodestonePayload.TYPE,
                OpenLodestonePayload.CODEC,
                (p, c) -> c.enqueueWork(
                        () -> dev.poptartking.poptartcore.navigation.client.screen.LodestoneScreen.open(p)));
        registrar.playToClient(
                WaypointLocationsPayload.TYPE,
                WaypointLocationsPayload.CODEC,
                (p, c) -> c.enqueueWork(() -> dev.poptartking.poptartcore.navigation.client.ClientWaypoints.accept(p)));
    }
}
