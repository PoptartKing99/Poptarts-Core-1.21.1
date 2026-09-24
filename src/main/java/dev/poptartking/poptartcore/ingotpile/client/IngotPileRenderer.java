package dev.poptartking.poptartcore.ingotpile.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.poptartking.poptartcore.ingotpile.IngotPileBlockEntity;
import dev.poptartking.poptartcore.ingotpile.IngotPileMaterials;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;

public class IngotPileRenderer implements BlockEntityRenderer<IngotPileBlockEntity> {
    @Override
    public void render(IngotPileBlockEntity pile, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        IngotPileShape shape = IngotPileShape.current();
        VertexConsumer vertices = buffers.getBuffer(RenderType.solid());
        int index = 0;
        for (ItemStack stack : pile.ingots()) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .apply(IngotPileMaterials.texture(stack.getItem()));
            int layer = index / 8;
            pose.pushPose();
            if ((layer & 1) == 1) {
                pose.translate(0.5F, 0, 0.5F);
                pose.mulPose(Axis.YP.rotationDegrees(shape.alternateLayerRotation()));
                pose.translate(-0.5F, 0, -0.5F);
            }
            pose.translate((index % 4) * shape.columnSpacing() / 16,
                    layer * shape.layerSpacing() / 16,
                    (index % 8 >= 4) ? shape.rowSpacing() / 16 : 0);
            drawIngot(pose, vertices, sprite, light, overlay, shape);
            pose.popPose();
            index++;
        }
    }

    private static void drawIngot(PoseStack pose, VertexConsumer out, TextureAtlasSprite sprite,
            int light, int overlay, IngotPileShape shape) {
        IngotPileShape.Bounds bottom = shape.bottom();
        IngotPileShape.Bounds top = shape.top();
        float px0 = bottom.minX() / 16;
        float px1 = bottom.maxX() / 16;
        float pz0 = bottom.minZ() / 16;
        float pz1 = bottom.maxZ() / 16;
        float qx0 = top.minX() / 16;
        float qx1 = top.maxX() / 16;
        float qz0 = top.minZ() / 16;
        float qz1 = top.maxZ() / 16;
        float height = shape.height() / 16;
        quad(pose, out, sprite, light, overlay, shape.uv(), shape.uv().top(), 0, 1, 0,
                qx0, height, qz0, qx1, height, qz0, qx1, height, qz1, qx0, height, qz1);
        quad(pose, out, sprite, light, overlay, shape.uv(), shape.uv().bottom(), 0, -1, 0,
                px0, 0, pz1, px1, 0, pz1, px1, 0, pz0, px0, 0, pz0);
        quad(pose, out, sprite, light, overlay, shape.uv(), shape.uv().end(), 0, 0, -1,
                px0, 0, pz0, px1, 0, pz0, qx1, height, qz0, qx0, height, qz0);
        quad(pose, out, sprite, light, overlay, shape.uv(), shape.uv().end(), 0, 0, 1,
                px1, 0, pz1, px0, 0, pz1, qx0, height, qz1, qx1, height, qz1);
        quad(pose, out, sprite, light, overlay, shape.uv(), shape.uv().longSide(), -1, 0, 0,
                px0, 0, pz1, px0, 0, pz0, qx0, height, qz0, qx0, height, qz1);
        quad(pose, out, sprite, light, overlay, shape.uv(), shape.uv().longSide(), 1, 0, 0,
                px1, 0, pz0, px1, 0, pz1, qx1, height, qz1, qx1, height, qz0);
    }

    private static void quad(PoseStack pose, VertexConsumer out, TextureAtlasSprite sprite,
            int light, int overlay, IngotPileShape.UvMap uvMap, IngotPileShape.UvRect uvRect,
            float nx, float ny, float nz, float... xyz) {
        int shade = ny > 0 ? 255 : ny < 0 ? 128 : nz != 0 ? 204 : 153;
        for (int i = 0; i < 4; i++) {
            int corner = 3 - i;
            int p = corner * 3;
            float u = (corner == 1 || corner == 2) ? uvRect.maxU() : uvRect.minU();
            float v = corner >= 2 ? uvRect.maxV() : uvRect.minV();
            out.addVertex(pose.last().pose(), xyz[p], xyz[p + 1], xyz[p + 2])
                    .setColor(shade, shade, shade, 255)
                    .setUv(sprite.getU(u / uvMap.width()), sprite.getV(v / uvMap.height()))
                    .setOverlay(overlay).setLight(light)
                    .setNormal(pose.last(), nx, ny, nz);
        }
    }
}
