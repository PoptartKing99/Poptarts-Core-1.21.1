package dev.poptartking.poptartcore.mixin.integration.create;

import com.farcr.ragdoll.content.entity.RagdollPlayer;
import com.farcr.ragdoll.mixin.ModelPartAccessor;
import com.farcr.ragdoll.system.RagdollRenderManager;
import com.farcr.ragdoll.util.ModelPartHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.equipment.armor.BacktankArmorLayer;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BacktankArmorLayer.class)
public abstract class CorpseBacktankPoseMixin {
    @WrapOperation(
            method =
                    "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void poptartcore$torsoPose(ModelPart body, PoseStack stack, Operation<Void> original) {
        if (!(RagdollRenderManager.getBoundEntity() instanceof RagdollPlayer)
                || !RagdollRenderManager.isRenderingLayerOverride()) {
            original.call(body, stack);
            return;
        }
        var geometry = RagdollRenderManager.findMatchingGeometry("body");
        if (geometry == null || !geometry.hasRenderableBox()) {
            original.call(body, stack);
            return;
        }
        var center = RagdollRenderManager.isRenderLevel()
                ? new Vector3f()
                : ModelPartHelper.findCenter(RagdollRenderManager.getBoundEntity());
        // World transforms replace the stack. GUI transforms need the existing anchor removed first.
        if (!RagdollRenderManager.isRenderLevel()) {
            var anchor = new PoseStack();
            ModelPartHelper.translateAndRotate(anchor, geometry, null, center);
            var inverse = new Matrix4f(anchor.last().pose()).invert();
            var normalInverse = new Matrix3f(anchor.last().normal()).invert();
            if (!inverse.isFinite() || !normalInverse.isFinite()) {
                original.call(body, stack);
                return;
            }
            stack.last().pose().mul(inverse);
            stack.last().normal().mul(normalInverse);
        }
        ModelPartHelper.translateAndRotate(stack, geometry, (ModelPartAccessor) (Object) body, center);
    }
}
