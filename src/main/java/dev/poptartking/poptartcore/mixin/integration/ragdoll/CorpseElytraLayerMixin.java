package dev.poptartking.poptartcore.mixin.integration.ragdoll;

import com.farcr.ragdoll.content.entity.RagdollPlayer;
import com.farcr.ragdoll.system.RagdollRenderManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.poptartking.poptartcore.integration.ragdoll.CorpseBackpackRender;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ElytraLayer.class)
public abstract class CorpseElytraLayerMixin {
    @Unique
    private ModelPart poptartcore$corpseWings;

    @WrapOperation(
            method =
                    "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/model/ElytraModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void poptartcore$attachWingsToTorso(
            ElytraModel<?> model,
            PoseStack stack,
            VertexConsumer vertices,
            int light,
            int overlay,
            Operation<Void> original) {
        var geometry = RagdollRenderManager.findMatchingGeometry("body");
        var parent = ((RenderLayer<?, ?>) (Object) this).getParentModel();
        if (!(RagdollRenderManager.getBoundEntity() instanceof RagdollPlayer)
                || !RagdollRenderManager.isRenderingLayerOverride()
                || geometry == null
                || !geometry.hasRenderableBox()
                || !(parent instanceof HumanoidModel<?> humanoid)) {
            original.call(model, stack, vertices, light, overlay);
            return;
        }
        if (poptartcore$corpseWings == null) {
            var wings = (CorpseElytraModelAccessor) model;
            var offset = new ModelPart(
                    List.of(),
                    Map.of(
                            "left_wing", wings.poptartcore$leftWing(),
                            "right_wing", wings.poptartcore$rightWing()));
            // The torso anchor replaces the layer stack, including vanilla's 1/8-block offset.
            offset.z = 2;
            poptartcore$corpseWings = new ModelPart(List.of(), Map.of("wings", offset));
        }
        var wings = (CorpseElytraModelAccessor) model;
        var left = wings.poptartcore$leftWing();
        var right = wings.poptartcore$rightWing();
        var leftPose = left.storePose();
        var rightPose = right.storePose();
        stack.pushPose();
        try {
            // Remove the layer's offset before changing from the preview anchor to the torso frame.
            // The child container reapplies it in torso-local coordinates instead.
            stack.translate(0, 0, -0.125F);
            if (!RagdollRenderManager.isRenderLevel()) {
                // Ragdoll's default ElytraLayer override adds (-.25, -.75, -.125)
                // after the anchor. World re-anchoring discards it; the GUI must undo it.
                stack.translate(0.25F, 0.75F, 0.125F);
            }
            // A corpse must not inherit the surrogate player's crouching/flying wing animation.
            left.resetPose();
            right.resetPose();
            CorpseBackpackRender.render(poptartcore$corpseWings, humanoid.body, stack, vertices, light, overlay, -1);
        } finally {
            left.loadPose(leftPose);
            right.loadPose(rightPose);
            stack.popPose();
        }
    }
}
