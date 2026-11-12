package dev.poptartking.poptartcore.lunar.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public final class BloodMoonWater {
    private static final int CRIMSON = 0xD91623;
    private static final int TINT_STEPS = 24;
    // Both the vanilla and Sodium chunk builders can request water colors on worker threads.
    private static volatile Tint currentTint;

    private BloodMoonWater() {}

    public static int tint(int color) {
        var tint = currentTint;
        if (tint == null || tint.level() != Minecraft.getInstance().level) return color;
        float amount = 0.95F * (tint.step() / (float) TINT_STEPS);
        return FastColor.ARGB32.lerp(amount, color, (color & 0xFF000000) | CRIMSON);
    }

    static void update(Minecraft minecraft, float strength) {
        var level = minecraft.level;
        var previous = currentTint;
        int step = Math.round(strength * TINT_STEPS);
        if (step == 0) {
            if (previous == null) return;
            currentTint = null;
            if (previous.level() != level) return;
        } else {
            if (previous != null && previous.level() == level && previous.step() == step) return;
            currentTint = new Tint(level, step);
        }
        if (minecraft.player == null || level == null) return;

        // Water color is baked into chunk meshes. Rebuild water sections only when the tint step changes.
        var center = minecraft.player.chunkPosition();
        int radius = minecraft.options.renderDistance().get() + 3;
        var chunks = level.getChunkSource();
        for (int x = center.x - radius; x <= center.x + radius; x++) {
            for (int z = center.z - radius; z <= center.z + radius; z++) {
                var chunk = chunks.getChunk(x, z, ChunkStatus.FULL, false);
                if (chunk == null) continue;
                var sections = chunk.getSections();
                for (int index = 0; index < sections.length; index++) {
                    if (sections[index].maybeHas(state -> state.getFluidState().is(FluidTags.WATER))) {
                        minecraft.levelRenderer.setSectionDirty(x, level.getSectionYFromSectionIndex(index), z);
                    }
                }
            }
        }
    }

    static void clear() {
        currentTint = null;
    }

    private record Tint(ClientLevel level, int step) {}
}
