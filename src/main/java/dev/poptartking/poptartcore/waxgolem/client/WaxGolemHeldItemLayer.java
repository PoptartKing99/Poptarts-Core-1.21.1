package dev.poptartking.poptartcore.waxgolem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.poptartking.poptartcore.waxgolem.WaxGolem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/client/waxgolem/WaxGolemHeldItemLayer.class */
public class WaxGolemHeldItemLayer extends RenderLayer<WaxGolem, WaxGolemModel> {
    private static final float HELD_SCALE = 0.85f;
    private static final float HAND_REACH = 0.6875f;
    private static final float HAND_INSET = 0.0f;
    private static final float HAND_DEPTH = 0.0625f;
    private static final float THIRD_PERSON_SCALE = 0.55f;
    private static final float BELLY_FRONT = 0.0625f;
    private static final float HIP_TILT = 25.0f;
    private static final float HIP_SCALE = 0.46750003f;
    private final ItemRenderer items;

    public WaxGolemHeldItemLayer(RenderLayerParent<WaxGolem, WaxGolemModel> parent, ItemRenderer items) {
        super(parent);
        this.items = items;
    }

    public void render(
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            WaxGolem golem,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float age,
            float netHeadYaw,
            float headPitch) {
        ItemStack result = golem.result();
        ItemStack tool = golem.tool();
        if (result.isEmpty()) {
            renderHand(poseStack, buffers, light, golem, tool);
        } else {
            renderHand(poseStack, buffers, light, golem, result);
            renderHip(poseStack, buffers, light, golem, tool);
        }
    }

    private void renderHip(PoseStack poseStack, MultiBufferSource buffers, int light, WaxGolem golem, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        ((WaxGolemModel) getParentModel()).body.translateAndRotate(poseStack);
        poseStack.translate(0.09f, -0.72f, -0.077109374f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(155.0f));
        poseStack.scale(HIP_SCALE, HIP_SCALE, HIP_SCALE);
        this.items.renderStatic(
                golem,
                stack,
                ItemDisplayContext.FIXED,
                false,
                poseStack,
                buffers,
                golem.level(),
                light,
                OverlayTexture.NO_OVERLAY,
                golem.getId());
        poseStack.popPose();
    }

    private void renderHand(
            PoseStack poseStack, MultiBufferSource buffers, int light, WaxGolem golem, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        ((WaxGolemModel) getParentModel()).body.translateAndRotate(poseStack);
        ((WaxGolemModel) getParentModel()).armLeft.translateAndRotate(poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        poseStack.translate(HAND_INSET, 0.0625f, -0.6875f);
        poseStack.scale(HELD_SCALE, HELD_SCALE, HELD_SCALE);
        this.items.renderStatic(
                golem,
                stack,
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                false,
                poseStack,
                buffers,
                golem.level(),
                light,
                OverlayTexture.NO_OVERLAY,
                golem.getId());
        poseStack.popPose();
    }
}
