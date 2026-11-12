package dev.poptartking.poptartcore.decoration.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.decoration.TreeTapperBlock;
import dev.poptartking.poptartcore.decoration.TreeTapperBlockEntity;
import dev.poptartking.poptartcore.registry.PoptartCoreFluids;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class TreeTapperRenderer implements BlockEntityRenderer<TreeTapperBlockEntity> {
    public TreeTapperRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            TreeTapperBlockEntity entity,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        var fluid = entity.storedFluid();
        if (fluid.isEmpty()) return;
        String name = fluid.is(PoptartCoreFluids.RESIN.source().get()) ? "resin_layer" : "maple_syrup_layer";
        var texture = PoptartCore.location("textures/block/fluid/" + name + ".png");
        pose.pushPose();
        pose.translate(.5, 0, .5);
        float angle =
                switch (entity.getBlockState().getValue(TreeTapperBlock.FACING)) {
                    case NORTH -> 0;
                    case EAST -> -90;
                    case SOUTH -> 180;
                    case WEST -> 90;
                    default -> 0;
                };
        pose.mulPose(Axis.YP.rotationDegrees(angle));
        pose.translate(-.5, 0, -.5);
        float y = TreeTapperBlockEntity.surfaceHeight(fluid.getAmount());
        VertexConsumer out = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        vertex(out, pose.last(), .25F, y, .0625F, 0, 0, light);
        vertex(out, pose.last(), .25F, y, .5625F, 0, 1, light);
        vertex(out, pose.last(), .75F, y, .5625F, 1, 1, light);
        vertex(out, pose.last(), .75F, y, .0625F, 1, 0, light);
        pose.popPose();
    }

    private static void vertex(
            VertexConsumer out, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light) {
        out.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0, 1, 0);
    }
}
