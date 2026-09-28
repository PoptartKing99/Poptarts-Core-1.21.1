package dev.poptartking.poptartcore.flesh;

import com.farcr.nomansland.common.block.ToxicGasBlock;
import com.mojang.serialization.Codec;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class FleshGeodeFeature extends Feature<FleshGeodeConfiguration> {
    private static final byte NONE = 0;
    private static final byte CAVITY = 1;
    private static final byte POOL = 2;
    private static final byte FLESH = 3;
    private static final byte CASING = 4;

    public FleshGeodeFeature(Codec<FleshGeodeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FleshGeodeConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        FleshGeodeConfiguration config = context.config();
        int radius = 3 + random.nextInt(4);
        int bounds = radius + 4;
        if (origin.getY() - bounds <= level.getMinBuildHeight()
                || origin.getY() + bounds >= level.getMaxBuildHeight()) {
            return false;
        }

        int localPoolY = -(radius / 2);
        int size = 2 * bounds + 1;
        byte[][][] cells = new byte[size][size][size];
        int[][] liningHeight = new int[size][size];
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                liningHeight[x][z] = random.nextInt(3) + 2;
            }
        }

        NormalNoise noise = NormalNoise.create(new WorldgenRandom(new LegacyRandomSource(level.getSeed())), -3, 1.0);
        MutableBlockPos cursor = new MutableBlockPos();
        for (int x = -bounds; x <= bounds; x++) {
            for (int y = -bounds; y <= bounds; y++) {
                for (int z = -bounds; z <= bounds; z++) {
                    cursor.setWithOffset(origin, x, y, z);
                    double distance = Math.sqrt(x * x + y * y * 1.5625 + z * z)
                            + noise.getValue(cursor.getX(), cursor.getY(), cursor.getZ());
                    if (distance <= radius && y >= localPoolY) {
                        cells[x + bounds][y + bounds][z + bounds] = y == localPoolY ? POOL : CAVITY;
                    }
                }
            }
        }

        dilate(cells, size, FLESH, CAVITY, POOL);
        if (config.casing().isPresent()) {
            dilate(cells, size, CASING, FLESH);
        }

        int shellCells = 0;
        int exposed = 0;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    byte cell = cells[x][y][z];
                    if (cell == FLESH || cell == CASING) {
                        shellCells++;
                        cursor.setWithOffset(origin, x - bounds, y - bounds, z - bounds);
                        BlockState existing = level.getBlockState(cursor);
                        if (existing.isAir() || !existing.getFluidState().isEmpty()) {
                            exposed++;
                        }
                    }
                }
            }
        }
        if (shellCells == 0 || exposed > shellCells * 0.2) {
            return false;
        }

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    byte cell = cells[x][y][z];
                    if (cell == NONE) continue;
                    cursor.setWithOffset(origin, x - bounds, y - bounds, z - bounds);
                    if (level.getBlockState(cursor).is(BlockTags.FEATURES_CANNOT_REPLACE)) continue;
                    switch (cell) {
                        case CAVITY -> placeInterior(level, cursor, config.air().getState(random, cursor));
                        case POOL ->
                            placeInterior(
                                    level,
                                    cursor,
                                    config.pool().orElse(config.air()).getState(random, cursor));
                        case FLESH -> {
                            boolean lining = y - bounds - localPoolY <= liningHeight[x][z];
                            setBlock(
                                    level,
                                    cursor,
                                    (lining ? config.lining() : config.shell()).getState(random, cursor));
                        }
                        case CASING ->
                            setBlock(
                                    level, cursor, config.casing().orElseThrow().getState(random, cursor));
                        default -> throw new IllegalStateException("Unknown flesh geode cell: " + cell);
                    }
                }
            }
        }

        int poolY = origin.getY() + localPoolY;
        List<int[]> raised = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                boolean center = x == 0 && z == 0;
                if (!center && random.nextFloat() >= 0.6F) continue;
                boolean rise = center || random.nextFloat() < 0.5F;
                int topY = poolY + (rise ? 1 : 0);
                for (int y = poolY; y <= topY; y++) {
                    cursor.set(origin.getX() + x, y, origin.getZ() + z);
                    setBlock(level, cursor, config.lining().getState(random, cursor));
                }
                if (rise) raised.add(new int[] {x, z});
            }
        }

        if (config.gem().isPresent() && !raised.isEmpty()) {
            List<int[]> offCenter =
                    raised.stream().filter(cell -> cell[0] != 0 || cell[1] != 0).toList();
            int[] cell = !offCenter.isEmpty() && random.nextFloat() >= 0.37F
                    ? offCenter.get(random.nextInt(offCenter.size()))
                    : new int[] {0, 0};
            BlockPos budPos = new BlockPos(origin.getX() + cell[0], poolY + 2, origin.getZ() + cell[1]);
            setBlock(
                    level, budPos.below(), PoptartCoreBlocks.BUDDING_FLESH.get().defaultBlockState());
            BlockState bud = config.gem().orElseThrow().getState(random, budPos);
            if (bud.hasProperty(LifebudBlock.ROOTED)) {
                bud = bud.setValue(LifebudBlock.ROOTED, LifebudBlock.rootsInto(level.getBlockState(budPos.below())));
            }
            setBlock(level, budPos, bud);
        }
        return true;
    }

    private void placeInterior(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (ToxicGasBlock.isToxicGas(state)) {
            setBlock(level, pos, Blocks.AIR.defaultBlockState());
            ToxicGasBlock.place(level, pos.immutable());
        } else {
            setBlock(level, pos, state);
        }
    }

    private static void dilate(byte[][][] cells, int size, byte target, byte... seeds) {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    if (cells[x][y][z] == NONE && hasNeighbor(cells, size, x, y, z, seeds)) {
                        cells[x][y][z] = target;
                    }
                }
            }
        }
    }

    private static boolean hasNeighbor(byte[][][] cells, int size, int x, int y, int z, byte[] seeds) {
        for (int neighborX = Math.max(0, x - 1); neighborX <= Math.min(size - 1, x + 1); neighborX++) {
            for (int neighborY = Math.max(0, y - 1); neighborY <= Math.min(size - 1, y + 1); neighborY++) {
                for (int neighborZ = Math.max(0, z - 1); neighborZ <= Math.min(size - 1, z + 1); neighborZ++) {
                    if (neighborX == x && neighborY == y && neighborZ == z) continue;
                    byte value = cells[neighborX][neighborY][neighborZ];
                    for (byte seed : seeds) {
                        if (value == seed) return true;
                    }
                }
            }
        }
        return false;
    }
}
