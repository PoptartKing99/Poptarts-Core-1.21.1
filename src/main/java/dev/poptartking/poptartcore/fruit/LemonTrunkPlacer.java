package dev.poptartking.poptartcore.fruit;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.poptartking.poptartcore.fruit.LemonFoliagePlacer.Crown;
import dev.poptartking.poptartcore.registry.PoptartFruitBlocks;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer.FoliageAttachment;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

/** An upright leader and two shorter branches that grow on different axes. */
public final class LemonTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<LemonTrunkPlacer> CODEC =
            RecordCodecBuilder.mapCodec(instance -> trunkPlacerParts(instance).apply(instance, LemonTrunkPlacer::new));

    private static final int MIN_LEAF_HEIGHT = 3;

    public LemonTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
        super(baseHeight, heightRandA, heightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return PoptartFruitBlocks.LEMON_TRUNK_PLACER.get();
    }

    @Override
    public List<FoliageAttachment> placeTrunk(
            LevelSimulatedReader level,
            BiConsumer<BlockPos, BlockState> setter,
            RandomSource random,
            int height,
            BlockPos base,
            TreeConfiguration config) {
        setDirtAt(level, setter, random, base.below(), config);
        int stemHeight = Math.max(1, height - 3);
        for (int y = 0; y < stemHeight; y++) {
            placeLog(level, setter, random, base.above(y), config);
        }

        BlockPos fork = base.above(stemHeight - 1);
        if (stemHeight < 2) return List.of(new FoliageAttachment(fork.above(), 1, false));

        Direction high = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction low = high.getOpposite();
        Direction side = random.nextBoolean() ? low.getClockWise() : low.getCounterClockWise();
        BlockPos sideFork = random.nextBoolean() ? fork.above() : fork;
        List<FoliageAttachment> crowns = new ArrayList<>();
        BlockPos highBranch = fork.relative(high);

        if (placeBranch(level, setter, random, config, highBranch, high.getAxis())) {
            if (placeLog(level, setter, random, highBranch.above(), config)
                    && placeLog(level, setter, random, highBranch.above(2), config)) {
                Crown.UPPER.addLeaves(crowns, fork, low, random);
            } else {
                crowns.add(new FoliageAttachment(highBranch.above(), 1, false));
            }
        }
        placeLowerBranch(level, setter, random, config, fork, low, Crown.LOWER, crowns);
        placeLowerBranch(level, setter, random, config, sideFork, side, Crown.SIDE, crowns);

        int minLeafY = base.getY() + MIN_LEAF_HEIGHT;
        crowns.removeIf(
                attachment -> attachment.radiusOffset() == 0 && attachment.pos().getY() < minLeafY);
        return crowns.isEmpty() ? List.of(new FoliageAttachment(fork.above(), 1, false)) : crowns;
    }

    private void placeLowerBranch(
            LevelSimulatedReader level,
            BiConsumer<BlockPos, BlockState> setter,
            RandomSource random,
            TreeConfiguration config,
            BlockPos fork,
            Direction direction,
            Crown crown,
            List<FoliageAttachment> crowns) {
        BlockPos branch = fork.below().relative(direction);
        if (!placeBranch(level, setter, random, config, branch, direction.getAxis())) return;

        BlockPos tip = branch.relative(direction);
        if (placeBranch(level, setter, random, config, tip, direction.getAxis())) {
            placeLog(level, setter, random, tip.above(), config);
            crown.addLeaves(crowns, fork, direction, random);
        } else {
            crowns.add(new FoliageAttachment(branch.above(), 1, false));
        }
    }

    private boolean placeBranch(
            LevelSimulatedReader level,
            BiConsumer<BlockPos, BlockState> setter,
            RandomSource random,
            TreeConfiguration config,
            BlockPos pos,
            Direction.Axis axis) {
        return placeLog(level, setter, random, pos, config, state -> state.trySetValue(RotatedPillarBlock.AXIS, axis));
    }
}
