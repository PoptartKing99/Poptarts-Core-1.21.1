package dev.poptartking.poptartcore.quern.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class QuernHighlightRenderer {
    private static final double BASE_HEIGHT = 11.0 / 16.0;
    private static final double INNER_MIN = 2.0 / 16.0;
    private static final double INNER_MAX = 14.0 / 16.0;

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
        double x = pos.getX() - camera.x;
        double y = pos.getY() - camera.y;
        double z = pos.getZ() - camera.z;
        LevelRenderer.renderLineBox(event.getPoseStack(), lines, x, y, z, x + 1, y + BASE_HEIGHT, z + 1, 0, 0, 0, 0.4F);
        LevelRenderer.renderLineBox(
                event.getPoseStack(),
                lines,
                x + INNER_MIN,
                y + BASE_HEIGHT,
                z + INNER_MIN,
                x + INNER_MAX,
                y + BASE_HEIGHT,
                z + INNER_MAX,
                0,
                0,
                0,
                0.4F);
        event.setCanceled(true);
    }
}
