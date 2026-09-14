package dev.poptartking.poptartcore.spider.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.spider.WebProjectile;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class WebProjectileRenderer extends EntityRenderer<WebProjectile> {
    private static final ResourceLocation TEXTURE = PoptartCore.location("textures/entity/spider/web_projectile.png");
    private final ModelPart model;

    public WebProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        MeshDefinition mesh = new MeshDefinition();
        // The supplied geometry uses upward-positive Y; Minecraft model cubes use downward-positive Y.
        mesh.getRoot()
                .addOrReplaceChild(
                        "web", CubeListBuilder.create().texOffs(0, 0).addBox(-2, -4, -4, 4, 4, 7), PartPose.ZERO);
        model = LayerDefinition.create(mesh, 32, 16).bakeRoot();
    }

    @Override
    public void render(
            WebProjectile entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot())));
        pose.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        // Turn the model around in local space, preserving its flight pitch.
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.scale(-1, -1, 1);
        model.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(WebProjectile entity) {
        return TEXTURE;
    }
}
