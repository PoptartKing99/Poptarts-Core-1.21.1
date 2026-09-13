package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.waxgolem.WaxGolem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class PoptartCoreEntityEvents {
    private PoptartCoreEntityEvents() {}

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(PoptartCoreEntities.WAX_GOLEM.get(), WaxGolem.attributes().build());
    }
}
