package dev.poptartking.poptartcore.navigation;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class NavigationEvents {
    private NavigationEvents() {}

    @SubscribeEvent
    public static void grindstone(GrindstoneEvent.OnPlaceItem event) {
        ItemStack top = event.getTopItem();
        ItemStack bottom = event.getBottomItem();
        if (!Waypoints.isMarkedCompass(top) && !Waypoints.isMarkedCompass(bottom)) return;
        ItemStack input = top.isEmpty() ? bottom : bottom.isEmpty() ? top : ItemStack.EMPTY;
        if (input.getCount() != 1) {
            event.setCanceled(true);
            return;
        }
        ItemStack cleared = input.copy();
        cleared.remove(NavigationComponents.WAYPOINTS.get());
        event.setOutput(cleared);
        event.setXp(0);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 10 == 0) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                var stack = player.getInventory().getItem(i);
                if (stack.is(Items.COMPASS)) Waypoints.refresh(stack, player);
            }
            Waypoints.refreshCarried(player);
            Waypoints.refreshAttached(player);
            WaypointTracking.tick(player);
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) WaypointTracking.forget(player);
    }
}
