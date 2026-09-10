package dev.poptartking.poptartcore.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class QuernHighlightRenderer {
    private static final VoxelShape BASE_OUTLINE = Block.box(0, 0, 0, 16, 4, 16);

    private QuernHighlightRenderer() {}

    @SubscribeEvent
    public static void renderBlockHighlight(RenderHighlightEvent.Block event) {
        Level level = Minecraft.getInstance().level;
        BlockPos pos = event.getTarget().getBlockPos();
        if (level == null || !level.getBlockState(pos).is(PoptartCoreBlocks.QUERN)) {
            return;
        }

        Vec3 camera = event.getCamera().getPosition();
        VertexConsumer lines = event.getMultiBufferSource().getBuffer(RenderType.lines());
        LevelRenderer.renderVoxelShape(
                event.getPoseStack(),
                lines,
                BASE_OUTLINE,
                pos.getX() - camera.x,
                pos.getY() - camera.y,
                pos.getZ() - camera.z,
                0,
                0,
                0,
                0.4F,
                true);
        event.setCanceled(true);
    }
}
