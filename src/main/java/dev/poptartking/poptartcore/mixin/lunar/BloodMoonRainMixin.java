package dev.poptartking.poptartcore.mixin.lunar;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import dev.poptartking.poptartcore.lunar.client.BloodMoonVisuals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public abstract class BloodMoonRainMixin {
    @Shadow
    @Final
    private static ResourceLocation RAIN_LOCATION;

    @WrapOperation(
            method = "renderSnowAndRain",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V"))
    private void poptartcore$bloodMoonRain(
            MeshData mesh, Operation<Void> original, LightTexture lightTexture, float partialTick) {
        float blend = BloodMoonVisuals.rainStrength(partialTick);
        if (blend > 0
                && RenderSystem.getShaderTexture(0)
                        == Minecraft.getInstance()
                                .getTextureManager()
                                .getTexture(RAIN_LOCATION)
                                .getId()) {
            BloodMoonVisuals.drawRain(blend, () -> original.call(mesh));
        } else {
            original.call(mesh);
        }
    }
}
