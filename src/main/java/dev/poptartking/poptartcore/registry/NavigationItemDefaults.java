package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class NavigationItemDefaults {
    private NavigationItemDefaults() {}

    @SubscribeEvent
    public static void components(ModifyDefaultComponentsEvent event) {
        for (var item : java.util.List.of(
                Items.COMPASS, Items.CLOCK, Items.RECOVERY_COMPASS, PoptartToolItems.DEPTH_METER.get())) {
            event.modify(item, components -> components.set(DataComponents.MAX_STACK_SIZE, 1));
        }
    }
}
