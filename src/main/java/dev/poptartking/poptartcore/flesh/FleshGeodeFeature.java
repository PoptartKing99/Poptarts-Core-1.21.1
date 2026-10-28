package dev.poptartking.poptartcore.flesh;

import com.farcr.nomansland.common.block.ToxicGasBlock;
import com.mojang.serialization.Codec;
import dev.poptartking.poptartcore.registry.PoptartFleshBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
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

public class FleshGeodeFeature extends Feature<FleshGeodeConfiguration> {
    private static final byte NONE = 0;
    private static final byte WATER = 2;
    private static final byte FLESH = 3;
    private static final Direction[] DIRECTIONS = Direction.values();

    public FleshGeodeFeature(Codec<FleshGeodeConfiguration> codec) {
        super(codec);
    }

    public boolean place(FeaturePlaceContext<FleshGeodeConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        FleshGeodeConfiguration config = (FleshGeodeConfiguration) context.config();
        int radius = 4 + random.nextInt(4);
        int bounds = radius + 4;
        if (origin.getY() - bounds > level.getMinBuildHeight() && origin.getY() + bounds < level.getMaxBuildHeight()) {
            int localWaterY = -(radius / 2);
            int n = 2 * bounds + 1;
            byte[][][] grid = new byte[n][n][n];
            int[][] creep = new int[n][n];

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    creep[i][j] = random.nextInt(3) + 2;
                }
            }

            NormalNoise noise = NormalNoise.create(
                    new WorldgenRandom(new LegacyRandomSource(level.getSeed())), -3, new double[] {1.0});
            MutableBlockPos cursor = new MutableBlockPos();

            for (int dx = -bounds; dx <= bounds; dx++) {
                for (int dy = -bounds; dy <= bounds; dy++) {
                    for (int dz = -bounds; dz <= bounds; dz++) {
                        cursor.setWithOffset(origin, dx, dy, dz);
                        double d = Math.sqrt((double) (dx * dx) + (double) (dy * dy) * 1.5625 + (double) (dz * dz))
                                + noise.getValue((double) cursor.getX(), (double) cursor.getY(), (double) cursor.getZ())
                                        * 1.0;
                        if (d <= (double) radius && dy >= localWaterY) {
                            grid[dx + bounds][dy + bounds][dz + bounds] = (byte) (dy == localWaterY ? 2 : 1);
                        }
                    }
                }
            }

            dilate(grid, n, (byte) 3, (byte) 1, (byte) 2);
            if (config.casing().isPresent()) {
                dilate(grid, n, (byte) 4, (byte) 3);
            }

            int shellCells = 0;
            int exposed = 0;

            for (int ix = 0; ix < n; ix++) {
                for (int iy = 0; iy < n; iy++) {
                    for (int iz = 0; iz < n; iz++) {
                        byte v = grid[ix][iy][iz];
                        if (v == 3 || v == 4) {
                            shellCells++;
                            cursor.setWithOffset(origin, ix - bounds, iy - bounds, iz - bounds);
                            BlockState existing = level.getBlockState(cursor);
                            if (existing.isAir() || !existing.getFluidState().isEmpty()) {
                                exposed++;
                            }
                        }
                    }
                }
            }

            if (shellCells != 0 && !((double) exposed > (double) shellCells * 0.2)) {
                for (int ix = 0; ix < n; ix++) {
                    for (int iy = 0; iy < n; iy++) {
                        for (int izx = 0; izx < n; izx++) {
                            byte v = grid[ix][iy][izx];
                            if (v != 0) {
                                cursor.setWithOffset(origin, ix - bounds, iy - bounds, izx - bounds);
                                if (!level.getBlockState(cursor).is(BlockTags.FEATURES_CANNOT_REPLACE)) {
                                    switch (v) {
                                        case 1:
                                            this.placeInterior(
                                                    level, cursor, config.air().getState(random, cursor));
                                            break;
                                        case 2:
                                            this.placeInterior(
                                                    level,
                                                    cursor,
                                                    config.pool()
                                                            .orElse(config.air())
                                                            .getState(random, cursor));
                                            break;
                                        case 3:
                                        default:
                                            boolean lining = iy - bounds - localWaterY <= creep[ix][izx];
                                            this.setBlock(
                                                    level,
                                                    cursor,
                                                    (lining ? config.lining() : config.shell())
                                                            .getState(random, cursor));
                                            break;
                                        case 4:
                                            this.setBlock(
                                                    level,
                                                    cursor,
                                                    config.casing().get().getState(random, cursor));
                                    }
                                }
                            }
                        }
                    }
                }

                int worldWaterY = origin.getY() + localWaterY;
                List<int[]> raised = new ArrayList<>();

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dzx = -1; dzx <= 1; dzx++) {
                        boolean center = dx == 0 && dzx == 0;
                        if (center || !(random.nextFloat() >= 0.6F)) {
                            boolean raise = center || random.nextFloat() < 0.5F;
                            int topY = worldWaterY + (raise ? 1 : 0);

                            for (int wy = worldWaterY; wy <= topY; wy++) {
                                cursor.set(origin.getX() + dx, wy, origin.getZ() + dzx);
                                this.setBlock(level, cursor, config.lining().getState(random, cursor));
                            }

                            if (raise) {
                                raised.add(new int[] {dx, dzx});
                            }
                        }
                    }
                }

                if (config.gem().isPresent() && !raised.isEmpty()) {
                    List<int[]> offset = new ArrayList<>();

                    for (int[] c : raised) {
                        if (c[0] != 0 || c[1] != 0) {
                            offset.add(c);
                        }
                    }

                    int[] cell = !offset.isEmpty() && !(random.nextFloat() < 0.37F)
                            ? offset.get(random.nextInt(offset.size()))
                            : new int[] {0, 0};
                    BlockPos budPos = new BlockPos(origin.getX() + cell[0], worldWaterY + 2, origin.getZ() + cell[1]);
                    this.setBlock(
                            level,
                            budPos.below(),
                            PoptartFleshBlocks.BUDDING_FLESH.get().defaultBlockState());
                    BlockState budState = config.gem().get().getState(random, budPos);
                    if (budState.hasProperty(LifebudBlock.ROOTED)) {
                        budState = (BlockState) budState.setValue(
                                LifebudBlock.ROOTED, LifebudBlock.rootsInto(level.getBlockState(budPos.below())));
                    }

                    this.setBlock(level, budPos, budState);
                    this.decorate(level, origin, grid, n, bounds, random);
                }

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void decorate(
            WorldGenLevel level, BlockPos origin, byte[][][] grid, int n, int bounds, RandomSource random) {
        List<BlockPos> walls = new ArrayList<>();
        List<Direction> openings = new ArrayList<>();
        MutableBlockPos cursor = new MutableBlockPos();

        for (int ix = 0; ix < n; ix++) {
            for (int iy = 0; iy < n; iy++) {
                for (int iz = 0; iz < n; iz++) {
                    if (grid[ix][iy][iz] == 1) {
                        cursor.setWithOffset(origin, ix - bounds, iy - bounds, iz - bounds);
                        if (level.getBlockState(cursor).isAir()) {
                            for (Direction direction : DIRECTIONS) {
                                BlockPos wall = cursor.relative(direction);
                                BlockState wallState = level.getBlockState(wall);
                                if (wallState.is(PoptartFleshBlocks.FLESH.get())
                                        || wallState.is(PoptartFleshBlocks.LIVING_FLESH.get())) {
                                    walls.add(wall);
                                    openings.add(direction.getOpposite());
                                }
                            }

                            if (random.nextFloat() < 0.12F) {
                                FleshGrowth.sprout(level, cursor.immutable(), random, 2);
                            }
                        }
                    }
                }
            }
        }

        int ulcers = 1 + random.nextInt(2);

        for (int placed = 0; placed < ulcers && !walls.isEmpty(); placed++) {
            int pick = random.nextInt(walls.size());
            if (random.nextBoolean()) {
                for (int index = 0; index < walls.size(); index++) {
                    if (openings.get(index) == Direction.UP) {
                        pick = index;
                        break;
                    }
                }
            }

            BlockPos wall = walls.remove(pick);
            Direction opening = openings.remove(pick);
            BlockPos spot = wall.relative(opening);
            BlockState spotState = level.getBlockState(spot);
            if (FleshGrowth.isOpen(spotState) || spotState.getBlock() instanceof FleshTendrilsBlock) {
                level.setBlock(spot, FleshGrowth.ulcer(opening, spotState), 2);
            }
        }
    }

    private void placeInterior(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (ToxicGasBlock.isToxicGas(state)) {
            this.setBlock(level, pos, Blocks.AIR.defaultBlockState());
            ToxicGasBlock.place(level, pos.immutable());
        } else {
            this.setBlock(level, pos, state);
        }
    }

    private static void dilate(byte[][][] grid, int n, byte target, byte... seeds) {
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                for (int z = 0; z < n; z++) {
                    if (grid[x][y][z] == 0 && hasNeighbor(grid, n, x, y, z, seeds)) {
                        grid[x][y][z] = target;
                    }
                }
            }
        }
    }

    private static boolean hasNeighbor(byte[][][] grid, int n, int x, int y, int z, byte[] seeds) {
        for (int ax = Math.max(0, x - 1); ax <= Math.min(n - 1, x + 1); ax++) {
            for (int ay = Math.max(0, y - 1); ay <= Math.min(n - 1, y + 1); ay++) {
                for (int az = Math.max(0, z - 1); az <= Math.min(n - 1, z + 1); az++) {
                    if (ax != x || ay != y || az != z) {
                        byte val = grid[ax][ay][az];

                        for (byte s : seeds) {
                            if (val == s) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}
