package dev.poptartking.poptartcore.backpack.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public final class BackpackModelRenderer {
    private BackpackModelRenderer() {}

    public static void render(
            BlockState state,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay,
            int flapColor,
            int bodyColor) {
        var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        render(model, state, pose, buffers, light, overlay, flapColor, bodyColor);
    }

    public static void render(
            net.minecraft.client.resources.model.BakedModel model,
            BlockState state,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay,
            int flapColor,
            int bodyColor) {
        var vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
        var random = RandomSource.create(42);
        for (int side = 0; side < 7; side++) {
            random.setSeed(42);
            var direction = side == 6 ? null : Direction.values()[side];
            for (var quad : model.getQuads(state, direction, random)) {
                int rgb = quad.isTinted() ? (quad.getTintIndex() == 1 ? bodyColor : flapColor) : 0xFFFFFF;
                vertices.putBulkData(
                        pose.last(),
                        quad,
                        ((rgb >> 16) & 255) / 255F,
                        ((rgb >> 8) & 255) / 255F,
                        (rgb & 255) / 255F,
                        1,
                        light,
                        overlay);
            }
        }
    }
}
