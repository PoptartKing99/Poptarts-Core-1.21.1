package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PoptartCoreParticles {
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, PoptartCore.MOD_ID);

    public static final Supplier<SimpleParticleType> RIFT_FIRE_FLAME =
            PARTICLE_TYPES.register("rift_fire_flame", () -> new SimpleParticleType(false));

    private PoptartCoreParticles() {}

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
