package dev.poptartking.poptartcore.mixin.lunar;

import com.llamalad7.mixinextras.sugar.Local;
import dev.poptartking.poptartcore.lunar.client.BloodMoonVisuals;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public abstract class BloodMoonLightTextureMixin {
    @Inject(
            method = "updateLightTexture",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LightTexture;blockLightRedFlicker:F"))
    private void poptartcore$tintBloodMoonLight(
            float partialTick, CallbackInfo ci, @Local(ordinal = 0) Vector3f skyLight) {
        BloodMoonVisuals.tintSkyLight(skyLight, partialTick);
    }
}
