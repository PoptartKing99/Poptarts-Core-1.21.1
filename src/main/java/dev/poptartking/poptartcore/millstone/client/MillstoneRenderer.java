package dev.poptartking.poptartcore.millstone.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.millstone.MillstoneRotorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class MillstoneRenderer implements BlockEntityRenderer<MillstoneRotorBlockEntity> {
    public static final ModelResourceLocation ROTOR_MODEL =
            ModelResourceLocation.standalone(PoptartCore.location("block/millstone/rotor"));

    public void render(
            MillstoneRotorBlockEntity rotor,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(ROTOR_MODEL);
        float degrees = rotor.prevAngle + (rotor.angle - rotor.prevAngle) * partialTick;
        float angle = degrees * (float) (Math.PI / 180.0);
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotation(angle));
        poseStack.translate(-0.5, 0.0, -0.5);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutout());
        Minecraft.getInstance()
                .getBlockRenderer()
                .getModelRenderer()
                .renderModel(
                        poseStack.last(),
                        buffer,
                        rotor.getBlockState(),
                        model,
                        1.0F,
                        1.0F,
                        1.0F,
                        packedLight,
                        packedOverlay,
                        ModelData.EMPTY,
                        RenderType.cutout());
        poseStack.popPose();
    }

    public AABB getRenderBoundingBox(MillstoneRotorBlockEntity rotor) {
        BlockPos pos = rotor.getBlockPos();
        return new AABB(pos).inflate(1.0, 0.0, 1.0);
    }
}
