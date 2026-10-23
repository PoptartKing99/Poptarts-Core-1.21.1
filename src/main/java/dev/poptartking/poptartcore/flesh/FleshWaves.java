package dev.poptartking.poptartcore.flesh;

import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent.Post;

@EventBusSubscriber(modid = "poptartcore")
public final class FleshWaves {
    private static final float SPREAD_CHANCE = 0.25F;
    private static final List<FleshWaves.Wave> WAVES = new ArrayList<>();

    private FleshWaves() {}

    public static void start(ServerLevel level, BlockPos source, FleshWaves.Kind kind) {
        WAVES.add(new FleshWaves.Wave(level, source.immutable(), kind));
    }

    @SubscribeEvent
    public static void onLevelTick(Post event) {
        if (!WAVES.isEmpty() && event.getLevel() instanceof ServerLevel level) {
            Iterator var4 = WAVES.iterator();

            while (var4.hasNext()) {
                FleshWaves.Wave wave = (FleshWaves.Wave) var4.next();
                if (wave.getLevel() == level && wave.spread()) {
                    var4.remove();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        WAVES.clear();
    }

    public static enum Kind {
        REVIVE,
        WITHER;
    }

    private static final class Wave {
        private final ServerLevel level;
        private final BlockPos source;
        private final FleshWaves.Kind kind;
        private final LongOpenHashSet seen = new LongOpenHashSet();
        private List<BlockPos> frontier = new ArrayList<>();
        private int reached;

        private Wave(ServerLevel level, BlockPos source, FleshWaves.Kind kind) {
            this.level = level;
            this.source = source;
            this.kind = kind;
            this.seen.add(source.asLong());
            this.reach(source);
        }

        private ServerLevel getLevel() {
            return this.level;
        }

        private boolean spread() {
            RandomSource random = this.level.getRandom();
            List<BlockPos> waiting = new ArrayList<>();
            List<BlockPos> current = this.frontier;
            this.frontier = new ArrayList<>();

            for (BlockPos pos : current) {
                if (random.nextFloat() >= 0.25F) {
                    waiting.add(pos);
                } else if (this.level.isLoaded(pos)) {
                    if (this.kind == FleshWaves.Kind.REVIVE) {
                        FleshGrowth.revive(this.level, pos, random);
                    } else {
                        FleshGrowth.wither(this.level, pos);
                    }

                    this.reach(pos);
                }
            }

            this.frontier.addAll(waiting);
            return this.frontier.isEmpty();
        }

        private void reach(BlockPos from) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (this.reached >= FleshGrowth.geodeLimit()) {
                            return;
                        }

                        BlockPos next = from.offset(x, y, z);
                        if (FleshGrowth.inGeodeReach(this.source, next)
                                && this.seen.add(next.asLong())
                                && this.level.isLoaded(next)
                                && this.level.getBlockState(next).is(PoptartCoreTags.FLESH)) {
                            this.frontier.add(next);
                            this.reached++;
                        }
                    }
                }
            }
        }
    }
}
