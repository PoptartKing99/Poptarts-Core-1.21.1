package dev.poptartking.poptartcore.fruit;

import com.mojang.serialization.MapCodec;
import dev.poptartking.poptartcore.registry.PoptartFruitBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

/** Places the leaves of the lemon tree's staggered crowns. */
public final class LemonFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<LemonFoliagePlacer> CODEC = MapCodec.unit(LemonFoliagePlacer::new);

    enum Crown {
        UPPER(-3, 0, new String[][] {
            {"++...", "##...", "##...", ".#...", ".+..."},
            {".#...", "###..", "##...", "+##..", ".#..."},
            {".+#+.", ".###.", ".#.#.", ".###.", "..#+."},
            {"..+..", ".+##.", ".####", ".+##.", "..+.."},
            {".....", ".....", "...#.", "...+.", "....."}
        }),
        LOWER(1, -1, new String[][] {
            {".+..", ".##+", "..##", ".##.", "..+."},
            {".+#.", ".##+", "..##", "###.", ".#.."},
            {"..+.", ".##.", ".###", ".##+", ".+#."},
            {"....", "..+.", "..#.", "..#+", "..+."}
        }),
        SIDE(2, -1, new String[][] {
            {"...", "...", ".#.", ".+.", "..."},
            {"...", "##+", ".##", "##.", "..."},
            {"...", ".#+", "###", ".#+", "..."},
            {"...", ".+.", ".#.", ".+.", "..."}
        });

        // Layers run bottom to top. Rows run across the crown's depth and columns follow the given direction.
        // '#' keeps the reference silhouette. '+' adds variation along its outer edges.
        private final int firstColumn;
        private final int firstLayer;
        private final String[][] layers;

        Crown(int firstColumn, int firstLayer, String[][] layers) {
            this.firstColumn = firstColumn;
            this.firstLayer = firstLayer;
            this.layers = layers;
        }

        void addLeaves(List<FoliageAttachment> leaves, BlockPos fork, Direction forward, RandomSource random) {
            Direction depth = forward.getClockWise();
            BlockPos start =
                    fork.relative(forward, firstColumn).relative(depth, -2).above(firstLayer);
            float fringeChance = random.nextBoolean() ? 0.75F : 0.25F;
            for (int y = 0; y < layers.length; y++) {
                for (int z = 0; z < layers[y].length; z++) {
                    BlockPos rowStart = start.above(y).relative(depth, z);
                    String row = layers[y][z];
                    for (int x = 0; x < row.length(); x++) {
                        char leaf = row.charAt(x);
                        if (leaf == '#' || leaf == '+' && random.nextFloat() < fringeChance) {
                            leaves.add(new FoliageAttachment(rowStart.relative(forward, x), 0, false));
                        }
                    }
                }
            }
        }
    }

    public LemonFoliagePlacer() {
        super(ConstantInt.of(0), ConstantInt.of(0));
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return PoptartFruitBlocks.LEMON_FOLIAGE_PLACER.get();
    }

    @Override
    protected void createFoliage(
            LevelSimulatedReader level,
            FoliageSetter setter,
            RandomSource random,
            TreeConfiguration config,
            int treeHeight,
            FoliageAttachment attachment,
            int foliageHeight,
            int radius,
            int offset) {
        if (attachment.radiusOffset() == 0) {
            tryPlaceLeaf(level, setter, random, config, attachment.pos());
        } else {
            placeLeavesRow(level, setter, random, config, attachment.pos(), attachment.radiusOffset(), 0, false);
            tryPlaceLeaf(level, setter, random, config, attachment.pos().above());
        }
    }

    @Override
    public int foliageHeight(RandomSource random, int treeHeight, TreeConfiguration config) {
        return 5;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int x, int y, int z, int radius, boolean large) {
        return x + z > radius;
    }
}
