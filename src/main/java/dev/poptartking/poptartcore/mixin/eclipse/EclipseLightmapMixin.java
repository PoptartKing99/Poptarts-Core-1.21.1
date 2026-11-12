package dev.poptartking.poptartcore.mixin.eclipse;

import com.mojang.blaze3d.platform.NativeImage;
import dev.poptartking.poptartcore.eclipse.client.EclipseVisuals;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public abstract class EclipseLightmapMixin {
    @Shadow
    @Final
    private NativeImage lightPixels;

    @Unique
    private final int[] poptartcore$originalLight = new int[256];

    // Transform the completed vanilla lightmap before its existing upload, avoiding a second GPU upload.
    @Inject(
            method = "updateLightTexture",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;upload()V"))
    private void poptartcore$eclipseLight(float partialTick, CallbackInfo ci) {
        EclipseVisuals.darkenLightmap(lightPixels, poptartcore$originalLight);
    }
}
