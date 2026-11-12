package dev.poptartking.poptartcore.mixin.integration.nomansland;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.poptartking.poptartcore.eclipse.client.EclipseVisuals;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.farcr.nomansland.client.renderer.SunDogRenderer")
public abstract class EclipseSunDogMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void poptartcore$hideCoveredSun(
            PoseStack poseStack, Matrix4f projectionMatrix, float partialTick, CallbackInfo ci) {
        if (EclipseVisuals.visiblePhase().coverage() > 0) ci.cancel();
    }
}
