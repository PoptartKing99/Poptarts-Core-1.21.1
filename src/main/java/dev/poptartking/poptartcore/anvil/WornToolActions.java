package dev.poptartking.poptartcore.anvil;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent.BlockToolModificationEvent;

@EventBusSubscriber(modid = "poptartcore")
public class WornToolActions {
    @SubscribeEvent
    public static void onToolModification(BlockToolModificationEvent event) {
        if (AnvilMenu.isWornOut(event.getHeldItemStack())) {
            event.setCanceled(true);
        }
    }
}
