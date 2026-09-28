package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.flesh.FleshGeodeConfiguration;
import dev.poptartking.poptartcore.flesh.FleshGeodeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PoptartCoreFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, PoptartCore.MOD_ID);

    public static final DeferredHolder<Feature<?>, FleshGeodeFeature> FLESH_GEODE =
            FEATURES.register("flesh_geode", () -> new FleshGeodeFeature(FleshGeodeConfiguration.CODEC));

    private PoptartCoreFeatures() {}

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
