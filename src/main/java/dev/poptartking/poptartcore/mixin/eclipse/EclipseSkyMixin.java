package dev.poptartking.poptartcore.mixin.eclipse;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.poptartking.poptartcore.eclipse.client.EclipseVisuals;
import dev.poptartking.poptartcore.lunar.client.BloodMoonVisuals;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(LevelRenderer.class)
public abstract class EclipseSkyMixin {
    @ModifyArg(
            method = "renderSky",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V",
                            ordinal = 0),
            index = 1)
    private ResourceLocation poptartcore$eclipseSun(ResourceLocation original) {
        return EclipseVisuals.sunTexture(original);
    }

    // Bound the UV change to the sun quad; the following moon quad uses its own atlas.
    @WrapOperation(
            method = "renderSky",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/blaze3d/vertex/VertexConsumer;setUv(FF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"),
            slice =
                    @Slice(
                            from =
                                    @At(
                                            value = "INVOKE",
                                            target =
                                                    "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V",
                                            ordinal = 0),
                            to =
                                    @At(
                                            value = "INVOKE",
                                            target =
                                                    "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V",
                                            ordinal = 1)))
    private VertexConsumer poptartcore$eclipseFrame(
            VertexConsumer consumer, float u, float v, Operation<VertexConsumer> original) {
        return original.call(consumer, EclipseVisuals.sunU(u), EclipseVisuals.sunV(v));
    }

    @WrapOperation(
            method = "renderSky",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V"),
            slice =
                    @Slice(
                            from =
                                    @At(
                                            value = "FIELD",
                                            target =
                                                    "Lnet/minecraft/client/renderer/LevelRenderer;MOON_LOCATION:Lnet/minecraft/resources/ResourceLocation;"),
                            to =
                                    @At(
                                            value = "INVOKE",
                                            target =
                                                    "Lnet/minecraft/client/multiplayer/ClientLevel;getStarBrightness(F)F")))
    private void poptartcore$drawMoon(
            MeshData moon,
            Operation<Void> original,
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            float partialTick) {
        if (EclipseVisuals.visiblePhase().coverage() > 0) {
            // Upload normally releases this buffer; release it here when skipping the draw.
            moon.close();
        } else {
            BloodMoonVisuals.drawMoon(partialTick, () -> original.call(moon));
        }
    }

    @ModifyExpressionValue(
            method = "renderSky",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getStarBrightness(F)F"))
    private float poptartcore$eclipseStars(float original) {
        return EclipseVisuals.starBrightness(original);
    }
}
