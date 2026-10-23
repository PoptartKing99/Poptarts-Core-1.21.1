package dev.poptartking.poptartcore.treetap.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.treetap.TreeTapBlock;
import dev.poptartking.poptartcore.treetap.TreeTapBlockEntity;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

/** Plays the existing texture strip once, using this tap's fill-event clock instead of atlas time. */
public final class TreeTapDripRenderer implements BlockEntityRenderer<TreeTapBlockEntity> {
    public static final ModelResourceLocation MODEL =
            ModelResourceLocation.standalone(PoptartCore.location("block/tree_tap_drip"));
    private static final Map<TreeTapBlock.Wood, ResourceLocation> TEXTURES = new EnumMap<>(TreeTapBlock.Wood.class);

    static {
        for (var wood : TreeTapBlock.Wood.values())
            TEXTURES.put(
                    wood,
                    PoptartCore.location("textures/block/tree_tap/tree_tap_" + wood.getSerializedName() + "_drip.png"));
    }

    @Override
    public void render(
            TreeTapBlockEntity tap,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        int frame = tap.dripFrame(partialTick);
        var state = tap.getBlockState();
        if (frame < 0 || !state.getValue(TreeTapBlock.HAS_BUCKET)) return;
        var model = Minecraft.getInstance().getModelManager().getModel(MODEL);
        // The original flat elements already have front and back faces. Cull the reverse
        // face so coincident quads cannot compete for depth and produce dark stripes.
        VertexConsumer vertices =
                buffers.getBuffer(RenderType.entityCutout(TEXTURES.get(state.getValue(TreeTapBlock.WOOD))));
        poseStack.pushPose();
        poseStack.translate(.5, .5, .5);
        float rotation =
                switch (state.getValue(TreeTapBlock.FACING)) {
                    case EAST -> -90;
                    case SOUTH -> 180;
                    case WEST -> 90;
                    default -> 0;
                };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(-.5, -.5, -.5);
        var random = RandomSource.create(0);
        for (BakedQuad quad : model.getQuads(null, null, random)) draw(quad, frame, poseStack.last(), vertices, light);
        for (Direction direction : Direction.values()) {
            for (BakedQuad quad : model.getQuads(null, direction, random))
                draw(quad, frame, poseStack.last(), vertices, light);
        }
        poseStack.popPose();
    }

    private static void draw(BakedQuad quad, int frame, PoseStack.Pose pose, VertexConsumer output, int light) {
        int[] data = quad.getVertices();
        int stride = data.length / 4;
        var sprite = quad.getSprite();
        var normal = quad.getDirection();
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * stride;
            float u = sprite.getUOffset(Float.intBitsToFloat(data[offset + 4]));
            float v = (frame + sprite.getVOffset(Float.intBitsToFloat(data[offset + 5])))
                    / TreeTapBlockEntity.DRIP_FRAME_COUNT;
            output.addVertex(
                            pose,
                            Float.intBitsToFloat(data[offset]),
                            Float.intBitsToFloat(data[offset + 1]),
                            Float.intBitsToFloat(data[offset + 2]))
                    .setColor(255, 255, 255, 255)
                    .setUv(u, v)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(pose, normal.getStepX(), normal.getStepY(), normal.getStepZ());
        }
    }
}
