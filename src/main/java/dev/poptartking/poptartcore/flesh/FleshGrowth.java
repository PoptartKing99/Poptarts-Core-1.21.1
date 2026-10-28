package dev.poptartking.poptartcore.flesh;

import com.farcr.nomansland.common.block.ToxicGasBlock;
import com.teamabnormals.caverns_and_chasms.core.registry.CCItems;
import dev.poptartking.poptartcore.registry.PoptartCoreDataMaps;
import dev.poptartking.poptartcore.registry.PoptartCoreSounds;
import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import dev.poptartking.poptartcore.registry.PoptartFleshBlocks;
import dev.poptartking.poptartcore.registry.PoptartMaterialItems;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class FleshGrowth {
    private static final Direction[] DIRECTIONS = Direction.values();

    private FleshGrowth() {}

    private static UlcerFood nourishment(ItemStack stack) {
        var food = stack.getItemHolder().getData(PoptartCoreDataMaps.ULCER_FOOD);
        if (food != null) return food;
        return net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getKey(stack.getItem())
                        .toString()
                        .equals("brewinandchewin:innards")
                ? new UlcerFood(.05F)
                : null;
    }

    public static boolean isUlcerFood(ItemStack stack) {
        return stack.isEmpty()
                ? false
                : stack.is((Item) CCItems.ZIRCONIA.get())
                        || stack.is(PoptartMaterialItems.LIFESALT.get())
                        || stack.is(Items.WITHER_ROSE)
                        || nourishment(stack) != null;
    }

    public static void feed(ServerLevel level, BlockPos pos, ItemStack food) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof UlcerBlock) {
            RandomSource random = level.getRandom();
            Direction facing = (Direction) state.getValue(DirectionalBlock.FACING);
            Vec3 mouth = Vec3.atCenterOf(pos)
                    .add(Vec3.atLowerCornerOf(facing.getNormal()).scale(0.15));
            level.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, food),
                    mouth.x,
                    mouth.y,
                    mouth.z,
                    8,
                    0.15,
                    0.15,
                    0.15,
                    0.05);
            level.playSound(
                    null, pos, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 1.0F, 0.6F + random.nextFloat() * 0.3F);
            if (food.is((Item) CCItems.ZIRCONIA.get())) {
                level.destroyBlock(pos, false);
                ToxicGasBlock.scatterAround(level, pos.relative(facing), 8 + random.nextInt(6));
                Block.popResource(level, pos, new ItemStack((ItemLike) PoptartMaterialItems.MERCURY_DROPLET.get()));
            } else if (food.is(PoptartMaterialItems.LIFESALT.get())) {
                FleshWaves.start(level, pos, FleshWaves.Kind.REVIVE);
                level.playSound(
                        null, pos, PoptartCoreSounds.FLESH_GEODE_BREATHING.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            } else if (food.is(Items.WITHER_ROSE)) {
                FleshWaves.start(level, pos, FleshWaves.Kind.WITHER);
                level.destroyBlock(pos, false);
            } else {
                UlcerFood nourishment = nourishment(food);
                if (nourishment != null) {
                    grow(level, pos, facing, random);
                    if (random.nextFloat() < nourishment.closeChance()) {
                        level.destroyBlock(pos, false);
                    }
                }
            }
        }
    }

    public static boolean sprout(LevelAccessor level, BlockPos pos, RandomSource random, int flags) {
        return sprout(level, pos, random, flags, support -> true);
    }

    public static BlockState ulcer(Direction opening, BlockState replaced) {
        return (BlockState) ((BlockState) ((UlcerBlock) PoptartFleshBlocks.ULCER.get())
                        .defaultBlockState()
                        .setValue(DirectionalBlock.FACING, opening))
                .setValue(
                        BlockStateProperties.WATERLOGGED,
                        replaced.getFluidState().isSourceOfType(Fluids.WATER));
    }

    public static boolean isOpen(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) && state.getFluidState().isSource();
    }

    public static boolean canSprout(LevelAccessor level, BlockPos pos, Predicate<BlockPos> supports) {
        if (!isOpen(level.getBlockState(pos))) {
            return false;
        } else {
            for (Direction direction : DIRECTIONS) {
                BlockPos support = pos.relative(direction);
                BlockState supportState = level.getBlockState(support);
                if (supportState.is(PoptartCoreTags.FLESH)
                        && supportState.isFaceSturdy(level, support, direction.getOpposite())
                        && supports.test(support)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean sprout(
            LevelAccessor level, BlockPos pos, RandomSource random, int flags, Predicate<BlockPos> supports) {
        BlockState current = level.getBlockState(pos);
        if (!isOpen(current)) {
            return false;
        } else {
            BlockState tendrils = null;
            boolean attached = false;
            int start = random.nextInt(DIRECTIONS.length);

            for (int index = 0; index < DIRECTIONS.length; index++) {
                Direction direction = DIRECTIONS[(start + index) % DIRECTIONS.length];
                BlockPos support = pos.relative(direction);
                BlockState supportState = level.getBlockState(support);
                if (supportState.is(PoptartCoreTags.FLESH)
                        && supportState.isFaceSturdy(level, support, direction.getOpposite())
                        && supports.test(support)
                        && (!attached || !random.nextBoolean())) {
                    if (tendrils == null) {
                        tendrils = (BlockState) tendrilsFor(supportState)
                                .defaultBlockState()
                                .setValue(BlockStateProperties.WATERLOGGED, current.is(Blocks.WATER));
                    }

                    tendrils = (BlockState) tendrils.setValue(MultifaceBlock.getFaceProperty(direction), true);
                    attached = true;
                }
            }

            if (!attached) {
                return false;
            } else {
                level.setBlock(pos, tendrils, flags);
                return true;
            }
        }
    }

    public static boolean hasRoomForTendrils(LevelAccessor level, FleshOrganisms.Organism organism, double ratio) {
        LongOpenHashSet tendrils = new LongOpenHashSet();
        int exposed = 0;
        LongIterator members = organism.iterator();

        while (members.hasNext()) {
            BlockPos member = BlockPos.of(members.nextLong());
            boolean open = false;

            for (Direction direction : DIRECTIONS) {
                BlockPos side = member.relative(direction);
                BlockState state = level.getBlockState(side);
                if (state.getBlock() instanceof FleshTendrilsBlock) {
                    tendrils.add(side.asLong());
                    open = true;
                } else if (isOpen(state)) {
                    open = true;
                }
            }

            if (open) {
                exposed++;
            }
        }

        return (double) tendrils.size() < (double) exposed * ratio;
    }

    public static int countTouchingTendrils(LevelAccessor level, BlockPos pos, int limit) {
        int count = 0;
        MutableBlockPos cursor = new MutableBlockPos();

        for (int x = -1; x <= 1 && count < limit; x++) {
            for (int y = -1; y <= 1 && count < limit; y++) {
                for (int z = -1; z <= 1 && count < limit; z++) {
                    if ((x != 0 || y != 0 || z != 0)
                            && level.getBlockState(cursor.setWithOffset(pos, x, y, z))
                                            .getBlock()
                                    instanceof FleshTendrilsBlock) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    public static Block tendrilsFor(BlockState support) {
        if (support.is(PoptartFleshBlocks.LIVING_FLESH.get()) || support.is(PoptartFleshBlocks.BUDDING_FLESH.get())) {
            return (Block) PoptartFleshBlocks.LIVING_FLESH_TENDRILS.get();
        } else {
            return support.is(PoptartFleshBlocks.ROTTEN_FLESH.get())
                    ? (Block) PoptartFleshBlocks.ROTTEN_FLESH_TENDRILS.get()
                    : (Block) PoptartFleshBlocks.FLESH_TENDRILS.get();
        }
    }

    public static void retintAround(LevelAccessor level, BlockPos changed) {
        for (Direction direction : DIRECTIONS) {
            retint(level, changed.relative(direction));
        }
    }

    private static void retint(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof FleshTendrilsBlock) {
            for (Direction direction : DIRECTIONS) {
                if ((Boolean) state.getValue(MultifaceBlock.getFaceProperty(direction))) {
                    BlockState support = level.getBlockState(pos.relative(direction));
                    if (support.is(PoptartCoreTags.FLESH)) {
                        Block wanted = tendrilsFor(support);
                        if (state.is(wanted)) {
                            return;
                        }

                        level.setBlock(pos, wanted.withPropertiesOf(state), 2);
                        return;
                    }
                }
            }
        }
    }

    @Nullable
    public static Direction openSide(LevelAccessor level, BlockPos pos, RandomSource random) {
        int start = random.nextInt(DIRECTIONS.length);

        for (int index = 0; index < DIRECTIONS.length; index++) {
            Direction direction = DIRECTIONS[(start + index) % DIRECTIONS.length];
            if (isOpen(level.getBlockState(pos.relative(direction)))) {
                return direction;
            }
        }

        return null;
    }

    private static void grow(ServerLevel level, BlockPos ulcer, Direction facing, RandomSource random) {
        BlockPos mouth = ulcer.relative(facing);
        FleshOrganisms.Organism organism = FleshOrganisms.owning(level, ulcer.relative(facing.getOpposite()));
        List<BlockPos> living = new ArrayList<>();
        List<BlockPos> surface = new ArrayList<>();
        if (organism != null) {
            LongIterator members = organism.iterator();

            while (members.hasNext()) {
                collect(level, BlockPos.of(members.nextLong()), living, surface, random);
            }
        } else {
            MutableBlockPos cursor = new MutableBlockPos();

            for (int x = -8; x <= 8; x++) {
                for (int y = -8; y <= 8; y++) {
                    for (int z = -8; z <= 8; z++) {
                        cursor.setWithOffset(ulcer, x, y, z);
                        if (level.isLoaded(cursor)) {
                            collect(level, cursor.immutable(), living, surface, random);
                        }
                    }
                }
            }
        }

        int wanted = 1 + random.nextInt(3);
        int grown = 0;

        for (int round = 0; round < wanted && !living.isEmpty(); round++) {
            List<BlockPos> options = new ArrayList<>();

            for (int attempt = 0; attempt < 24; attempt++) {
                BlockPos source = living.get(random.nextInt(living.size()));
                BlockPos target = source.offset(random.nextInt(5) - 2, random.nextInt(5) - 2, random.nextInt(5) - 2);
                if (level.isLoaded(target) && level.getBlockState(target).is(PoptartCoreTags.SUPPORTS_FLESH_GROWTH)) {
                    options.add(target);
                }
            }

            BlockPos best = choose(level, options, organism, ulcer, false);
            if (best != null) {
                level.setBlockAndUpdate(
                        best, PoptartFleshBlocks.LIVING_FLESH.get().defaultBlockState());
                retintAround(level, best);
                FleshOrganisms.grew(level, best);
                living.add(best);
                grown++;
            }
        }

        for (int index = 0; index < grown && !surface.isEmpty(); index++) {
            if (!(random.nextFloat() >= 0.1F)) {
                List<BlockPos> walls = new ArrayList<>();

                for (int sample = 0; sample < 6; sample++) {
                    walls.add(surface.get(random.nextInt(surface.size())));
                }

                BlockPos wall = choose(level, walls, organism, ulcer, false);
                surface.remove(wall);
                Direction opening = openSide(level, wall, random);
                BlockPos spot = opening == null ? null : wall.relative(opening);
                if (spot != null && !spot.equals(mouth) && !spot.equals(ulcer)) {
                    level.setBlockAndUpdate(spot, ulcer(opening, level.getBlockState(spot)));
                    FleshOrganisms.grew(level, spot);
                }
            }
        }

        int tendrils = random.nextInt(3);
        Predicate<BlockPos> supports = organism == null ? support -> true : organism::contains;

        for (int placed = 0; placed < tendrils; placed++) {
            List<BlockPos> options = new ArrayList<>();

            for (int attemptx = 0; attemptx < 8; attemptx++) {
                BlockPos member = organism != null
                        ? organism.randomMember(random)
                        : (living.isEmpty() ? null : living.get(random.nextInt(living.size())));
                if (member == null) {
                    break;
                }

                BlockPos candidate = member.relative(DIRECTIONS[random.nextInt(DIRECTIONS.length)]);
                if (!candidate.equals(mouth) && level.isLoaded(candidate) && canSprout(level, candidate, supports)) {
                    options.add(candidate);
                }
            }

            BlockPos target = choose(level, options, organism, ulcer, true);
            if (target != null) {
                sprout(level, target, random, 3, supports);
            }
        }

        ToxicGasBlock.scatterAround(level, mouth, 3 + random.nextInt(3));
        if (grown > 0) {
            level.playSound(
                    null,
                    ulcer,
                    SoundEvents.SCULK_BLOCK_SPREAD,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.8F + random.nextFloat() * 0.2F);
        }
    }

    private static void collect(
            ServerLevel level, BlockPos pos, List<BlockPos> living, List<BlockPos> surface, RandomSource random) {
        if (level.getBlockState(pos).is(PoptartFleshBlocks.LIVING_FLESH.get())) {
            living.add(pos);
            if (openSide(level, pos, random) != null) {
                surface.add(pos);
            }
        }
    }

    @Nullable
    public static BlockPos choose(
            LevelAccessor level,
            List<BlockPos> options,
            @Nullable FleshOrganisms.Organism organism,
            @Nullable BlockPos anchor,
            boolean cluster) {
        int count = options.size();
        if (count == 0) {
            return null;
        } else {
            double[] balance = new double[count];
            double[] reach = new double[count];

            for (int index = 0; index < count; index++) {
                BlockPos option = options.get(index);
                balance[index] = organism == null ? 0.0 : organism.imbalanceWith(option);
                reach[index] = anchor == null ? 0.0 : Math.sqrt(option.distSqr(anchor));
            }

            double[] balanceRange = range(balance);
            double[] reachRange = range(reach);
            BlockPos best = null;
            double bestScore = Double.MAX_VALUE;

            for (int index = 0; index < count; index++) {
                double score = normalised(balance[index], balanceRange) + normalised(reach[index], reachRange);
                if (cluster) {
                    score -= 0.5 * (double) countTouchingTendrils(level, options.get(index), 4) / 4.0;
                }

                if (score < bestScore) {
                    best = options.get(index);
                    bestScore = score;
                }
            }

            return best;
        }
    }

    private static double[] range(double[] values) {
        double lowest = Double.MAX_VALUE;
        double highest = -Double.MAX_VALUE;

        for (double value : values) {
            lowest = Math.min(lowest, value);
            highest = Math.max(highest, value);
        }

        return new double[] {lowest, highest};
    }

    private static double normalised(double value, double[] range) {
        double spread = range[1] - range[0];
        return spread > 0.0 ? (value - range[0]) / spread : 0.0;
    }

    public static boolean inGeodeReach(BlockPos start, BlockPos pos) {
        return Math.abs(pos.getX() - start.getX()) <= 16
                && Math.abs(pos.getY() - start.getY()) <= 16
                && Math.abs(pos.getZ() - start.getZ()) <= 16;
    }

    public static int geodeLimit() {
        return 4096;
    }

    public static void revive(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState state = level.getBlockState(pos);
        if (state.is(PoptartFleshBlocks.ROTTEN_FLESH.get())) {
            level.setBlockAndUpdate(pos, PoptartFleshBlocks.FLESH.get().defaultBlockState());
            retintAround(level, pos);
        } else if (state.is(PoptartFleshBlocks.FLESH.get()) && random.nextFloat() < 0.8F) {
            level.setBlockAndUpdate(pos, PoptartFleshBlocks.LIVING_FLESH.get().defaultBlockState());
            retintAround(level, pos);
        }
    }

    public static void wither(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(PoptartFleshBlocks.FLESH.get()) || state.is(PoptartFleshBlocks.LIVING_FLESH.get())) {
            int spared = 4;

            for (BlockPos near : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
                if (near.distSqr(pos) <= (double) spared
                        && level.getBlockState(near).is(PoptartFleshBlocks.BUDDING_FLESH.get())) {
                    return;
                }
            }

            level.setBlockAndUpdate(pos, PoptartFleshBlocks.ROTTEN_FLESH.get().defaultBlockState());
            retintAround(level, pos);
        }
    }
}
