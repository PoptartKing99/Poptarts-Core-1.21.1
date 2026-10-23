package dev.poptartking.poptartcore.flesh;

import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.event.level.ChunkEvent.Load;
import net.neoforged.neoforge.event.level.ChunkEvent.Unload;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(
        modid = "poptartcore",
        value = {Dist.CLIENT})
public final class FleshWaterTint {
    private static final int TINT_COLOR = 9049108;
    private static final int MAX_RADIUS = FleshWaterTint.Source.BUDDING.radius;
    private static final Map<Long, Map<Long, FleshWaterTint.Source>> SOURCES = new ConcurrentHashMap<>();

    private FleshWaterTint() {}

    public static int tint(BlockPos pos, int base) {
        if (SOURCES.isEmpty()) {
            return base;
        } else {
            float amount = 0.0F;
            int minX = SectionPos.blockToSectionCoord(pos.getX() - MAX_RADIUS);
            int maxX = SectionPos.blockToSectionCoord(pos.getX() + MAX_RADIUS);
            int minY = SectionPos.blockToSectionCoord(pos.getY() - MAX_RADIUS);
            int maxY = SectionPos.blockToSectionCoord(pos.getY() + MAX_RADIUS);
            int minZ = SectionPos.blockToSectionCoord(pos.getZ() - MAX_RADIUS);
            int maxZ = SectionPos.blockToSectionCoord(pos.getZ() + MAX_RADIUS);
            MutableBlockPos cursor = new MutableBlockPos();

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        Map<Long, FleshWaterTint.Source> sources = SOURCES.get(SectionPos.asLong(x, y, z));
                        if (sources != null) {
                            for (Entry<Long, FleshWaterTint.Source> entry : sources.entrySet()) {
                                FleshWaterTint.Source source = entry.getValue();
                                cursor.set(entry.getKey());
                                double distance = Math.sqrt(cursor.distSqr(pos));
                                if (!(distance > (double) source.radius)) {
                                    amount = Math.max(
                                            amount, (1.0F - (float) distance / (float) source.radius) * source.maxTint);
                                }
                            }
                        }
                    }
                }
            }

            if (amount <= 0.0F) {
                return base;
            } else {
                int red = Mth.lerpInt(amount, ARGB32.red(base), ARGB32.red(9049108));
                int green = Mth.lerpInt(amount, ARGB32.green(base), ARGB32.green(9049108));
                int blue = Mth.lerpInt(amount, ARGB32.blue(base), ARGB32.blue(9049108));
                return ARGB32.color(ARGB32.alpha(base), red, green, blue);
            }
        }
    }

    public static void blockChanged(BlockPos pos, BlockState before, BlockState after) {
        FleshWaterTint.Source was = FleshWaterTint.Source.of(before);
        FleshWaterTint.Source is = FleshWaterTint.Source.of(after);
        if (was != is) {
            long section = SectionPos.asLong(pos);
            if (is != null) {
                SOURCES.computeIfAbsent(section, key -> new ConcurrentHashMap<>())
                        .put(pos.asLong(), is);
            } else {
                Map<Long, FleshWaterTint.Source> sources = SOURCES.get(section);
                if (sources == null) {
                    return;
                }

                sources.remove(pos.asLong());
                if (sources.isEmpty()) {
                    SOURCES.remove(section);
                }
            }

            redraw(pos.asLong());
        }
    }

    private static void redraw(long source) {
        int x = BlockPos.getX(source);
        int y = BlockPos.getY(source);
        int z = BlockPos.getZ(source);
        int var10001 = x - MAX_RADIUS;
        int var10002 = y - MAX_RADIUS;
        int var10003 = z - MAX_RADIUS;
        int var10004 = x + MAX_RADIUS;
        int var10005 = y + MAX_RADIUS;
        Minecraft.getInstance()
                .levelRenderer
                .setBlocksDirty(var10001, var10002, var10003, var10004, var10005, z + MAX_RADIUS);
    }

    @SubscribeEvent
    public static void onChunkLoad(Load event) {
        if (event.getLevel().isClientSide()) {
            ChunkAccess chunk = event.getChunk();
            LevelChunkSection[] sections = chunk.getSections();
            ChunkPos chunkPos = chunk.getPos();

            for (int index = 0; index < sections.length; index++) {
                LevelChunkSection section = sections[index];
                if (!section.hasOnlyAir() && section.maybeHas(state -> FleshWaterTint.Source.of(state) != null)) {
                    int sectionY = chunk.getSectionYFromSectionIndex(index);

                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            for (int x = 0; x < 16; x++) {
                                FleshWaterTint.Source source = FleshWaterTint.Source.of(section.getBlockState(x, y, z));
                                if (source != null) {
                                    BlockPos pos = new BlockPos(
                                            chunkPos.getBlockX(x),
                                            SectionPos.sectionToBlockCoord(sectionY) + y,
                                            chunkPos.getBlockZ(z));
                                    SOURCES.computeIfAbsent(SectionPos.asLong(pos), key -> new ConcurrentHashMap<>())
                                            .put(pos.asLong(), source);
                                    redraw(pos.asLong());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(Unload event) {
        if (event.getLevel().isClientSide()) {
            ChunkPos chunkPos = event.getChunk().getPos();
            SOURCES.entrySet().removeIf(entry -> {
                if (SectionPos.x(entry.getKey()) == chunkPos.x && SectionPos.z(entry.getKey()) == chunkPos.z) {
                    for (long source : entry.getValue().keySet()) {
                        redraw(source);
                    }

                    return true;
                } else {
                    return false;
                }
            });
        }
    }

    @SubscribeEvent
    public static void onLogout(LoggingOut event) {
        SOURCES.clear();
    }

    private static enum Source {
        BUDDING(6, 0.85F),
        LIVING(3, 0.4F);

        private final int radius;
        private final float maxTint;

        private Source(int radius, float maxTint) {
            this.radius = radius;
            this.maxTint = maxTint;
        }

        @Nullable
        private static FleshWaterTint.Source of(BlockState state) {
            if (state.is(PoptartCoreBlocks.BUDDING_FLESH.get())) {
                return BUDDING;
            } else {
                return state.is(PoptartCoreBlocks.LIVING_FLESH.get()) ? LIVING : null;
            }
        }
    }
}
