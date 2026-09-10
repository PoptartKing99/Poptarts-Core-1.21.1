package dev.poptartking.poptartcore.quern.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.quern.QuernBlockEntity;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class QuernRenderer implements BlockEntityRenderer<QuernBlockEntity> {
    public static final ModelResourceLocation ROTOR_MODEL =
            ModelResourceLocation.standalone(PoptartCore.location("block/quern/quern_stone"));
    public static final ModelResourceLocation FLOUR_MODEL =
            ModelResourceLocation.standalone(PoptartCore.location("block/quern/quern_flour"));
    private static final float MAX_FLOUR_RISE = 1.01F / 16.0F;
    private static final float ITEM_HEIGHT = 1.36F;
    private static final float ITEM_SCALE = 0.32F;
    private static final float INPUT_ITEM_X = 0.14F;
    private static final float OUTPUT_ITEM_X = 0.86F;
    private static final float COUNT_FORWARD_OFFSET = 0.08F;
    private final Map<QuernBlockEntity, QuernSoundInstance> activeSounds = new WeakHashMap<>();

    @Override
    public void render(
            QuernBlockEntity quern,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        updateSound(quern);
        renderRotor(quern, partialTick, poseStack, bufferSource, packedOverlay);
        if (quern.flourFill() > 0) {
            poseStack.pushPose();
            poseStack.translate(0, quern.flourFill() * MAX_FLOUR_RISE, 0);
            renderFlourModel(quern, poseStack, bufferSource, packedLight, packedOverlay);
            poseStack.popPose();
        }
        if (isTargeted(quern)) {
            renderStoredItems(quern, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    private static boolean isTargeted(QuernBlockEntity quern) {
        return Minecraft.getInstance().hitResult instanceof BlockHitResult hit
                && hit.getBlockPos().equals(quern.getBlockPos());
    }

    private void updateSound(QuernBlockEntity quern) {
        if (!quern.isRotating()) {
            return;
        }
        QuernSoundInstance sound = activeSounds.get(quern);
        if (sound == null || sound.isStopped()) {
            sound = new QuernSoundInstance(quern);
            activeSounds.put(quern, sound);
            Minecraft.getInstance().getSoundManager().play(sound);
        }
    }

    private static void renderStoredItems(
            QuernBlockEntity quern,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        Minecraft minecraft = Minecraft.getInstance();
        double cameraX = minecraft.gameRenderer.getMainCamera().getPosition().x;
        double cameraZ = minecraft.gameRenderer.getMainCamera().getPosition().z;
        double centerX = quern.getBlockPos().getX() + 0.5F;
        double centerZ = quern.getBlockPos().getZ() + 0.5F;
        float facingAngle = (float) Math.toDegrees(Math.atan2(cameraX - centerX, cameraZ - centerZ));

        poseStack.pushPose();
        poseStack.translate(0.5F, 0, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(facingAngle));
        poseStack.translate(-0.5F, 0, -0.5F);
        renderStoredItem(
                quern,
                quern.getItem(QuernBlockEntity.INPUT_SLOT),
                INPUT_ITEM_X,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay,
                0);
        renderStoredItem(
                quern,
                quern.getItem(QuernBlockEntity.OUTPUT_SLOT),
                OUTPUT_ITEM_X,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay,
                1);
        poseStack.popPose();
    }

    private static void renderStoredItem(
            QuernBlockEntity quern,
            ItemStack stack,
            float x,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay,
            int seedOffset) {
        if (stack.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        poseStack.pushPose();
        poseStack.translate(x, ITEM_HEIGHT, 0.5F);
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        minecraft
                .getItemRenderer()
                .renderStatic(
                        stack,
                        ItemDisplayContext.FIXED,
                        packedLight,
                        packedOverlay,
                        poseStack,
                        bufferSource,
                        quern.getLevel(),
                        (int) quern.getBlockPos().asLong() + seedOffset);
        poseStack.popPose();

        String count = Integer.toString(stack.getCount());
        Font font = minecraft.font;
        poseStack.pushPose();
        poseStack.translate(x + 0.1F, ITEM_HEIGHT + 0.08F, 0.5F + COUNT_FORWARD_OFFSET);
        poseStack.scale(0.0125F, -0.0125F, 0.0125F);
        font.drawInBatch(
                count,
                -font.width(count) / 2.0F,
                0,
                0xFFFFFFFF,
                true,
                poseStack.last().pose(),
                bufferSource,
                Font.DisplayMode.NORMAL,
                0,
                packedLight);
        poseStack.popPose();
    }

    private static void renderFlourModel(
            QuernBlockEntity quern,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay) {
        int color = quern.powderColor();
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(FLOUR_MODEL);
        RenderType renderType = RenderType.cutout();
        VertexConsumer buffer = bufferSource.getBuffer(renderType);
        Minecraft.getInstance()
                .getBlockRenderer()
                .getModelRenderer()
                .renderModel(
                        poseStack.last(),
                        buffer,
                        quern.getBlockState(),
                        model,
                        red,
                        green,
                        blue,
                        packedLight,
                        packedOverlay,
                        ModelData.EMPTY,
                        renderType);
    }

    private static void renderRotor(
            QuernBlockEntity quern,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(quern.rotation(partialTick)));
        poseStack.translate(-0.5, 0, -0.5);
        renderModel(quern, ROTOR_MODEL, poseStack, bufferSource, packedOverlay);
        poseStack.popPose();
    }

    private static void renderModel(
            QuernBlockEntity quern,
            ModelResourceLocation location,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedOverlay) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(location);
        RenderType renderType = RenderType.cutout();
        VertexConsumer buffer = bufferSource.getBuffer(renderType);
        Minecraft.getInstance()
                .getBlockRenderer()
                .getModelRenderer()
                .tesselateBlock(
                        quern.getLevel(),
                        model,
                        quern.getBlockState(),
                        quern.getBlockPos(),
                        poseStack,
                        buffer,
                        false,
                        RandomSource.create(),
                        quern.getBlockState().getSeed(quern.getBlockPos()),
                        packedOverlay,
                        ModelData.EMPTY,
                        renderType);
    }
}
