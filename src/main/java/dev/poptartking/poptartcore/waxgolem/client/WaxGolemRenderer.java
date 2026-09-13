package dev.poptartking.poptartcore.waxgolem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.waxgolem.WaxGolem;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/client/waxgolem/WaxGolemRenderer.class */
public class WaxGolemRenderer extends MobRenderer<WaxGolem, WaxGolemModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(PoptartCore.location("wax_golem"), "main");
    private static final ResourceLocation[] TEXTURES = {
        PoptartCore.location("textures/entity/wax_golem/wax_golem_1.png"),
        PoptartCore.location("textures/entity/wax_golem/wax_golem_2.png"),
        PoptartCore.location("textures/entity/wax_golem/wax_golem_3.png"),
        PoptartCore.location("textures/entity/wax_golem/wax_golem_4.png")
    };

    public WaxGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new WaxGolemModel(context.bakeLayer(LAYER)), 0.4f);
        addLayer(new WaxGolemHeldItemLayer(this, context.getItemRenderer()));
    }

    public void render(
            WaxGolem golem,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light) {
        super.render(golem, entityYaw, partialTick, poseStack, buffers, light);
        if (!golem.lit()) {
            golem.setWickAnchor(null);
            return;
        }
        PoseStack anchor = new PoseStack();
        anchor.mulPose(Axis.YP.rotationDegrees(180.0f - Mth.rotLerp(partialTick, golem.yBodyRotO, golem.yBodyRot)));
        anchor.scale(-1.0f, -1.0f, 1.0f);
        anchor.translate(0.0f, -1.501f, 0.0f);
        ((WaxGolemModel) getModel()).wickAnchor(anchor);
        Vector3f point = anchor.last().pose().transformPosition(new Vector3f());
        golem.setWickAnchor(new Vec3(point.x, point.y, point.z));
    }

    public ResourceLocation getTextureLocation(WaxGolem golem) {
        return TEXTURES[Math.clamp(golem.stage(), 0, TEXTURES.length - 1)];
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void scale(WaxGolem golem, PoseStack poseStack, float partialTick) {
        poseStack.scale(1.0f, 1.0f, 1.0f);
    }
}
