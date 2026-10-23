package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class AccessoryEvents {
    private AccessoryEvents() {}

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        // Death drops/corpse capture already emptied the original when keepInventory is false.
        AccessoryInventory.copy(event.getOriginal(), event.getEntity());
    }
}
