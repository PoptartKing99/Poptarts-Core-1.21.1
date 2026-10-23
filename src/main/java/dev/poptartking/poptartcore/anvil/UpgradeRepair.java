package dev.poptartking.poptartcore.anvil;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

@EventBusSubscriber(modid = "poptartcore")
public final class UpgradeRepair {
    private UpgradeRepair() {}

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!event.getEntity().level().isClientSide) {
            AnvilMenu.healUpgradedModifiers(event.getTo());
        }
    }
}
