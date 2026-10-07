package dev.poptartking.poptartcore.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import dev.poptartking.poptartcore.conduit.ConduitHeartbeatTiming;
import static dev.poptartking.poptartcore.conduit.ConduitHeartbeatTiming.HALF_PULSE_DURATION_TICKS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;

public final class ConduitEyeAnimation {
    private static final Map<ResourceLocation, Integer> FRAME_COUNTS = new HashMap<>();
    private record HeartbeatPlayback(long startNanos, boolean singlePair) {}
    private static final Map<BlockPos, HeartbeatPlayback> SOUND_STARTS = new HashMap<>();
    private static ClientLevel soundLevel;
    private static long lastPlaybackCleanup;

    private ConduitEyeAnimation() {}

    public static void clearCache() {
        FRAME_COUNTS.clear();
        SOUND_STARTS.clear();
        soundLevel = null;
        lastPlaybackCleanup = 0;
    }

    private static void checkSoundLevel(ClientLevel level) {
        if (soundLevel != level) {
            SOUND_STARTS.clear();
            soundLevel = level;
        }
    }

    public static void heartbeatStarted(BlockPos position, long startNanos, boolean singlePair) {
        checkSoundLevel(Minecraft.getInstance().level);
        if (soundLevel != null) SOUND_STARTS.put(position, new HeartbeatPlayback(startNanos, singlePair));
    }

    public static void tickPlaybackCleanup() {
        checkSoundLevel(Minecraft.getInstance().level);
        long now = System.nanoTime();
        if (now - lastPlaybackCleanup < ConduitHeartbeatTiming.PLAYBACK_CLEANUP_INTERVAL_NANOS) return;
        lastPlaybackCleanup = now;
        SOUND_STARTS.values().removeIf(playback -> now - playback.startNanos()
                >= ConduitHeartbeatTiming.playbackDurationNanos(playback.singlePair()));
    }

    private static int frameCount(ResourceLocation texture) {
        return FRAME_COUNTS.computeIfAbsent(texture, location -> {
            var resource = Minecraft.getInstance().getResourceManager().getResource(location);
            if (resource.isEmpty()) return 1;
            try (var stream = resource.get().open(); var image = NativeImage.read(stream)) {
                return image.getHeight() % image.getWidth() == 0
                        ? Math.max(1, image.getHeight() / image.getWidth()) : 1;
            } catch (IOException exception) {
                return 1;
            }
        });
    }

    public static void render(ConduitBlockEntity conduit,
            PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        ResourceLocation texture = ResourceLocation.withDefaultNamespace(
                "textures/entity/conduit/" + (conduit.isHunting() ? "open_eye" : "closed_eye") + ".png");
        int count = frameCount(texture);
        checkSoundLevel(Minecraft.getInstance().level);
        HeartbeatPlayback playback = SOUND_STARTS.get(conduit.getBlockPos());
        // Audio runs on real time; follow actual playback instead of predicting server ticks.
        double phase = playback == null ? Double.POSITIVE_INFINITY
                : (double) (System.nanoTime() - playback.startNanos()) / ConduitHeartbeatTiming.NANOS_PER_TICK;
        int frame = frameAtBeat(phase, count, playback != null && playback.singlePair());
        float top = (float) frame / count;
        float bottom = (float) (frame + 1) / count;
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        PoseStack.Pose pose = poses.last();
        // Match ModelPart's north-face UV orientation under the vanilla billboard transform.
        vertex(vertices, pose, 0.25F, -0.25F, 1, top, light, overlay);
        vertex(vertices, pose, -0.25F, -0.25F, 0, top, light, overlay);
        vertex(vertices, pose, -0.25F, 0.25F, 0, bottom, light, overlay);
        vertex(vertices, pose, 0.25F, 0.25F, 1, bottom, light, overlay);
    }

    private static int frameAtBeat(double phase, int count, boolean singlePair) {
        int middleFrame = (count - 1) / 2;
        int beatCount = ConduitHeartbeatTiming.beatCount(singlePair);
        for (int i = 0; i < beatCount; i += 2) {
            double contraction = ConduitHeartbeatTiming.beatPeakTicks(i);
            double expansion = ConduitHeartbeatTiming.beatPeakTicks(i + 1);
            double contractionEnd = contraction + HALF_PULSE_DURATION_TICKS;
            double expansionEnd = expansion + HALF_PULSE_DURATION_TICKS;
            if (phase < contraction || phase > expansionEnd) continue;
            if (phase < contractionEnd) {
                double progress = (phase - contraction) / HALF_PULSE_DURATION_TICKS;
                return (int) Math.round(progress * middleFrame);
            }
            if (phase < expansion) return middleFrame;
            double progress = (phase - expansion) / HALF_PULSE_DURATION_TICKS;
            return middleFrame + (int) Math.round(progress * (count - 1 - middleFrame));
        }
        return 0;
    }

    private static void vertex(VertexConsumer vertices, PoseStack.Pose pose,
            float x, float y, float u, float v, int light, int overlay) {
        vertices.addVertex(pose, x, y, 0).setColor(-1).setUv(u, v)
                .setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);
    }
}
