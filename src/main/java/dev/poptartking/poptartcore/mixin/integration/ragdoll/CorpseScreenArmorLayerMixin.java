package dev.poptartking.poptartcore.mixin.integration.ragdoll;

import com.farcr.ragdoll.screen.CorpseScreen;
import com.farcr.ragdoll.system.EntityGeometry;
import com.farcr.ragdoll.system.RagdollRenderManager;
import com.farcr.ragdoll.util.ModelPartHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.poptartking.poptartcore.integration.ragdoll.CorpseScreenArmorPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class CorpseScreenArmorLayerMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void poptartcore$beginCorpseScreenArmor(PoseStack poseStack, MultiBufferSource bufferSource,
            int packedLight, LivingEntity entity, float limbSwing, float limbSwingAmount,
            float partialTick, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(Minecraft.getInstance().screen instanceof CorpseScreen)
                || !CorpseScreen.isEntityInScreen(RagdollRenderManager.getBoundEntity())
                || !RagdollRenderManager.isRenderingLayerOverride()) {
            return;
        }

        EntityGeometry anchor = null;
        for (String name : new String[]{"body", "body_adult", "torso", "upperbody", "head"}) {
            EntityGeometry candidate = RagdollRenderManager.findMatchingGeometry(name);
            if (candidate != null && candidate.hasRenderableBox()) {
                anchor = candidate;
                break;
            }
        }
        if (anchor == null) {
            return;
        }

        PoseStack anchorStack = new PoseStack();
        ModelPartHelper.translateAndRotate(anchorStack, anchor, null,
                ModelPartHelper.findCenter(RagdollRenderManager.getBoundEntity()));
        Matrix4f anchorInverse = new Matrix4f(anchorStack.last().pose()).invert();
        Matrix3f normalInverse = new Matrix3f(anchorStack.last().normal()).invert();
        if (anchorInverse.isFinite() && normalInverse.isFinite()) {
            CorpseScreenArmorPose.begin(
                    new Matrix4f(poseStack.last().pose()).mul(anchorInverse),
                    new Matrix3f(poseStack.last().normal()).mul(normalInverse));
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void poptartcore$endCorpseScreenArmor(PoseStack poseStack, MultiBufferSource bufferSource,
            int packedLight, LivingEntity entity, float limbSwing, float limbSwingAmount,
            float partialTick, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        CorpseScreenArmorPose.end();
    }
}
