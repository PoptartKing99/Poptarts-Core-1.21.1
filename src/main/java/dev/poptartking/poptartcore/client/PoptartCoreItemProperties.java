package dev.poptartking.poptartcore.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.crossbow.RepeatingCrossbowItem;
import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class PoptartCoreItemProperties {
    private PoptartCoreItemProperties() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(PoptartCoreItemProperties::registerRepeatingCrossbowProperties);
    }

    private static void registerRepeatingCrossbowProperties() {
        var item = PoptartCoreItems.REPEATING_CROSSBOW.get();
        ItemProperties.register(item, PoptartCore.location("pull"), (stack, level, entity, seed) -> {
            if (entity == null || CrossbowItem.isCharged(stack)) {
                return 0.0F;
            }
            return (float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks())
                    / RepeatingCrossbowItem.getChargeDuration();
        });
        ItemProperties.register(
                item,
                PoptartCore.location("pulling"),
                (stack, level, entity, seed) -> entity != null
                                && entity.isUsingItem()
                                && entity.getUseItem() == stack
                                && !CrossbowItem.isCharged(stack)
                        ? 1.0F
                        : 0.0F);
        ItemProperties.register(
                item,
                PoptartCore.location("charged"),
                (stack, level, entity, seed) -> CrossbowItem.isCharged(stack) ? 1.0F : 0.0F);
        ItemProperties.register(item, PoptartCore.location("firework"), (stack, level, entity, seed) -> {
            ChargedProjectiles projectiles = stack.get(DataComponents.CHARGED_PROJECTILES);
            return projectiles != null && projectiles.contains(Items.FIREWORK_ROCKET) ? 1.0F : 0.0F;
        });
    }
}
