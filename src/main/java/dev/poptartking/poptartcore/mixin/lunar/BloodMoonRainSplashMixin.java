package dev.poptartking.poptartcore.mixin.lunar;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.poptartking.poptartcore.lunar.client.BloodMoonVisuals;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ParticleEngine.class)
public abstract class BloodMoonRainSplashMixin {
    @ModifyReturnValue(method = "createParticle", at = @At("RETURN"))
    private Particle poptartcore$bloodMoonRainSplash(Particle particle, ParticleOptions options) {
        if (particle != null && options.getType() == ParticleTypes.RAIN) {
            float blend = BloodMoonVisuals.rainStrength(1);
            if (blend > 0) {
                particle.setColor(1, Mth.lerp(blend, 1F, 0.13F), Mth.lerp(blend, 1F, 0.045F));
            }
        }
        return particle;
    }
}
