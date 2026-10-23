package dev.poptartking.poptartcore.equipment.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.CapeColors;
import dev.poptartking.poptartcore.registry.PoptartCapeItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class CapeItemProperties {
    private CapeItemProperties() {}

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                PoptartCapeItems.AGILITY_CAPE.get(),
                PoptartCore.location("cape_color"),
                (stack, level, entity, seed) -> CapeColors.modelProperty(stack)));
    }
}
