package dev.poptartking.poptartcore.navigation;

import dev.poptartking.poptartcore.PoptartCore;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class NavigationSetup {
    private NavigationSetup() {}

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("sable"))
            event.enqueueWork(dev.poptartking.poptartcore.navigation.sable.SableCompat::init);
    }
}
