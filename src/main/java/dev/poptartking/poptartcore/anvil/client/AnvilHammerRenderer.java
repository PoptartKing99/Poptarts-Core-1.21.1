package dev.poptartking.poptartcore.anvil.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.poptartking.poptartcore.anvil.AnvilBlock;
import dev.poptartking.poptartcore.anvil.AnvilBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class AnvilHammerRenderer implements BlockEntityRenderer<AnvilBlockEntity> {
    private static final float TOP = 1.0F;
    private static final float SCALE = 0.55F;
    private static final float THICKNESS = 0.0625F;
    private static final float SIDEWAYS = -0.15F;
    private final Context context;

    public AnvilHammerRenderer(Context context) {
        this.context = context;
    }

    public void render(
            AnvilBlockEntity anvil,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        ItemStack hammer = anvil.hammer();
        if (!hammer.isEmpty()) {
            Direction facing = (Direction) anvil.getBlockState().getValue(AnvilBlock.FACING);
            poseStack.pushPose();
            float top =
                    anvil.getBlockState().getBlock() instanceof dev.poptartking.poptartcore.anvil.PrimitiveAnvilBlock
                            ? .875F
                            : 1F;
            poseStack.translate(0.5F, top + .0171875F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
            poseStack.translate(-0.15F, 0.0F, 0.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(0.55F, 0.55F, 0.55F);
            Minecraft.getInstance()
                    .getItemRenderer()
                    .renderStatic(
                            hammer,
                            ItemDisplayContext.FIXED,
                            light,
                            OverlayTexture.NO_OVERLAY,
                            poseStack,
                            buffers,
                            anvil.getLevel(),
                            (int) anvil.getBlockPos().asLong());
            poseStack.popPose();
        }
    }
}
