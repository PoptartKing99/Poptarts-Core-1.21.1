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
                UpdateLodestone.TYPE,
                UpdateLodestone.CODEC,
                (p, c) -> c.enqueueWork(() -> Lodestones.onUpdate(p, (ServerPlayer) c.player())));
        registrar.playToServer(
                SelectWaypoint.TYPE,
                SelectWaypoint.CODEC,
                (p, c) -> c.enqueueWork(() -> Waypoints.onSelect(p, (ServerPlayer) c.player())));
        registrar.playToServer(
                HideWaypoint.TYPE,
                HideWaypoint.CODEC,
                (p, c) -> c.enqueueWork(() -> Waypoints.onHide(p, (ServerPlayer) c.player())));
        registrar.playToServer(
                ToggleNavigation.TYPE,
                ToggleNavigation.CODEC,
                (p, c) -> c.enqueueWork(() -> Waypoints.onToggleNavigation(p, (ServerPlayer) c.player())));
        registrar.playToClient(
                OpenLodestone.TYPE,
                OpenLodestone.CODEC,
                (p, c) -> c.enqueueWork(
                        () -> dev.poptartking.poptartcore.navigation.client.screen.LodestoneScreen.open(p)));
        registrar.playToClient(
                WaypointLocations.TYPE,
                WaypointLocations.CODEC,
                (p, c) -> c.enqueueWork(() -> dev.poptartking.poptartcore.navigation.client.ClientWaypoints.accept(p)));
    }
}
