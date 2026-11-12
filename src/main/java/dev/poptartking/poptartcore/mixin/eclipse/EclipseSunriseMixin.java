package dev.poptartking.poptartcore.mixin.eclipse;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.poptartking.poptartcore.eclipse.client.EclipseVisuals;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DimensionSpecialEffects.class)
public abstract class EclipseSunriseMixin {
    @ModifyReturnValue(method = "getSunriseColor", at = @At("RETURN"))
    private float[] poptartcore$eclipseSunrise(float[] original) {
        float brightness = EclipseVisuals.brightness();
        return original == null || brightness >= 1
                ? original
                : new float[] {original[0], original[1], original[2], original[3] * brightness};
    }
}
