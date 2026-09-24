package dev.poptartking.poptartcore.lostheart.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.poptartking.poptartcore.lostheart.LostHeartEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class LostHeartRenderer extends EntityRenderer<LostHeartEntity> {
    private static final ResourceLocation[] STAGES = {
            ResourceLocation.fromNamespaceAndPath("poptartcore", "textures/entity/lost_heart/heart_1.png"),
            ResourceLocation.fromNamespaceAndPath("poptartcore", "textures/entity/lost_heart/heart_2.png"),
            ResourceLocation.fromNamespaceAndPath("poptartcore", "textures/entity/lost_heart/heart_3.png"),
            ResourceLocation.fromNamespaceAndPath("poptartcore", "textures/entity/lost_heart/heart_4.png")
    };

    public LostHeartRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(LostHeartEntity entity) {
        return STAGES[Mth.clamp(entity.cracks(), 0, STAGES.length - 1)];
    }

    @Override
    public void render(LostHeartEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        anchor(entity, partialTick, poseStack);
        float phase = (entity.level().getGameTime() % 140 + partialTick) / 140.0F;
        poseStack.translate(0, Mth.sin(phase * Mth.TWO_PI) * 0.09F, 0);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.55F, 0.55F, 0.55F);
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        int frame = (int) (entity.level().getGameTime() / 2 % 4);
        float v0 = frame / 4.0F;
        float v1 = (frame + 1) / 4.0F;
        vertex(vertices, pose.pose(), pose.normal(), -0.5F, -0.5F, 0, v1, packedLight);
        vertex(vertices, pose.pose(), pose.normal(), 0.5F, -0.5F, 1, v1, packedLight);
        vertex(vertices, pose.pose(), pose.normal(), 0.5F, 0.5F, 1, v0, packedLight);
        vertex(vertices, pose.pose(), pose.normal(), -0.5F, 0.5F, 0, v0, packedLight);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
    }

    private static void anchor(LostHeartEntity heart, float partialTick, PoseStack poseStack) {
        if (heart.corpseId() == -1) return;
        Entity corpse = heart.level().getEntity(heart.corpseId());
        if (corpse == null) return;
        Vec3 head = LostHeartEntity.headOf(corpse);
        poseStack.translate(
                head.x - Mth.lerp(partialTick, heart.xOld, heart.getX()),
                head.y + LostHeartEntity.HOVER_HEIGHT - Mth.lerp(partialTick, heart.yOld, heart.getY()),
                head.z - Mth.lerp(partialTick, heart.zOld, heart.getZ()));
    }

    private static void vertex(VertexConsumer vertices, Matrix4f position, Matrix3f normal,
                               float x, float y, float u, float v, int light) {
        vertices.addVertex(position, x, y, 0)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0, 1, 0);
    }
}
