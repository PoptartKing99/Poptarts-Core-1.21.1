package dev.poptartking.poptartcore.mixin.conduit;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.poptartking.poptartcore.client.ConduitEyeAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ConduitRenderer.class)
public abstract class ConduitEyeAnimationMixin {
    @Shadow @Final private ModelPart eye;

    @WrapOperation(method = "render(Lnet/minecraft/world/level/block/entity/ConduitBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void poptartcore$syncEyePulse(ModelPart part, PoseStack poses,
            VertexConsumer vertices, int light, int overlay, Operation<Void> original,
            ConduitBlockEntity conduit, float partialTick, PoseStack renderPoses,
            MultiBufferSource buffers, int renderLight, int renderOverlay) {
        if (part == eye) {
            ConduitEyeAnimation.render(conduit, poses, buffers, light, overlay);
        } else {
            original.call(part, poses, vertices, light, overlay);
        }
    }
}
