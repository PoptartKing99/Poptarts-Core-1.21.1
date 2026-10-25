package dev.poptartking.poptartcore.backpack.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.poptartking.poptartcore.backpack.BackpackBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public final class BackpackBlockRenderer implements BlockEntityRenderer<BackpackBlockEntity> {
    public BackpackBlockRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(BackpackBlockEntity backpack) {
        boolean banner = dev.poptartking.poptartcore.backpack.BackpackAttachments.items(backpack.attachments())
                        .getFirst()
                        .getItem()
                instanceof net.minecraft.world.item.BannerItem;
        return new net.minecraft.world.phys.AABB(backpack.getBlockPos())
                .expandTowards(0, banner ? 2 : backpack.getContainerSize() == 54 ? 1 : 0, 0)
                .inflate(1.0 / 16.0);
    }

    @Override
    public void render(
            BackpackBlockEntity backpack,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        BackpackModelRenderer.render(
                backpack.getBlockState(), pose, buffers, light, overlay, backpack.dyeColor(0), backpack.dyeColor(1));
        pose.pushPose();
        pose.translate(.5, .5, .5);
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-backpack.getBlockState()
                .getValue(dev.poptartking.poptartcore.backpack.BackpackBlock.FACING)
                .toYRot()));
        pose.translate(-.5, -.5, -.5);
        boolean rucksack = backpack.getContainerSize() == 54;
        if (rucksack) pose.translate(0, -3.0 / 16, 0);
        BackpackAttachmentRenderer.render(
                rucksack, backpack.attachments(), pose, buffers, light, overlay, backpack.getLevel(), false);
        pose.popPose();
    }
}
