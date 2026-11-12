package dev.poptartking.poptartcore.fruit;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartFruitBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LemonTreeGameTests {
    private static final BlockPos SAPLING_POS = new BlockPos(5, 1, 5);

    @SubscribeEvent
    public static void template(ServerAboutToStartEvent event) throws Exception {
        event.getServer()
                .getStructureManager()
                .getOrCreate(PoptartCore.location("lemon_tree_test"))
                .load(
                        event.getServer().registryAccess().lookupOrThrow(Registries.BLOCK),
                        TagParser.parseTag(
                                "{size:[22,11,11],palette:[{Name:\"minecraft:air\"}],blocks:[],entities:[]}"));
    }

    @GameTest(template = "lemon_tree_test")
    public static void lemonSaplingGrowsACompactTreeWithSpacedFruit(GameTestHelper helper) {
        plantSapling(helper);
        growSapling(helper, 20);
        helper.assertBlockProperty(SAPLING_POS, SaplingBlock.STAGE, 1);
        growSapling(helper, 20);

        int trunkHeight = 0;
        while (trunkHeight < 8
                && helper.getBlockState(SAPLING_POS.above(trunkHeight)).is(Blocks.OAK_LOG)) {
            trunkHeight++;
        }
        helper.assertTrue(trunkHeight == 4 || trunkHeight == 5, "Expected a four- or five-block oak trunk");
        BlockPos fork = SAPLING_POS.above(trunkHeight - 1);
        Direction upperBranch = Direction.Plane.HORIZONTAL.stream()
                .filter(direction ->
                        helper.getBlockState(fork.above(2).relative(direction)).is(Blocks.OAK_LOG))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Expected a branch at the top of the trunk"));
        Direction lowerBranch = upperBranch.getOpposite();
        helper.assertBlockPresent(Blocks.OAK_LOG, fork.above(2).relative(upperBranch));
        helper.assertBlockPresent(Blocks.OAK_LOG, fork.below().relative(lowerBranch, 2));
        helper.assertBlockPresent(Blocks.OAK_LOG, fork.relative(lowerBranch, 2));
        var plainLeaves = PoptartFruitBlocks.LEMON_LEAVES.get();
        var floweringLeaves = PoptartFruitBlocks.FLOWERING_LEMON_LEAVES.get();
        var fruit = PoptartFruitBlocks.LEMON_FRUIT.get();
        for (int distance = 1; distance <= 2; distance++) {
            var state = helper.getBlockState(fork.above(2).relative(lowerBranch, distance));
            helper.assertTrue(state.isAir() || state.is(fruit), "Expected an opening between the crowns");
        }
        Direction depth = lowerBranch.getClockWise();
        BlockPos sideFork = hasRaisedSideBranch(helper, fork) ? fork.above() : fork;
        boolean clockwiseBranch =
                helper.getBlockState(sideFork.below().relative(depth)).is(Blocks.OAK_LOG);
        boolean counterclockwiseBranch = helper.getBlockState(sideFork.below().relative(depth.getOpposite()))
                .is(Blocks.OAK_LOG);
        helper.assertTrue(clockwiseBranch != counterclockwiseBranch, "Expected one branch on the perpendicular axis");
        Direction sideBranch = clockwiseBranch ? depth : depth.getOpposite();
        helper.assertBlockPresent(Blocks.OAK_LOG, sideFork.below().relative(sideBranch, 2));
        helper.assertBlockPresent(Blocks.OAK_LOG, sideFork.relative(sideBranch, 2));
        assertLemonLeaves(helper, sideFork.above().relative(sideBranch, 4));
        assertLemonLeaves(helper, fork.above().relative(upperBranch, 3));
        assertLemonLeaves(helper, fork.above().relative(upperBranch, 2).relative(depth, -2));
        assertLemonLeaves(helper, fork.above().relative(upperBranch, 2).relative(depth, 2));
        assertLemonLeaves(helper, fork.relative(lowerBranch, 3).relative(depth, -2));
        assertLemonLeaves(helper, fork.above().relative(lowerBranch, 3).relative(depth, 2));
        assertLemonLeaves(helper, fork.above(4));
        List<BlockPos> fruitingSites = new ArrayList<>();
        int plainCount = 0;
        int fruitCount = 0;
        int blossomCount = 0;
        int branchLogs = 0;
        int lowestLeaf = Integer.MAX_VALUE;
        int highestLeaf = Integer.MIN_VALUE;
        for (BlockPos pos : canopyVolume(SAPLING_POS)) {
            var state = helper.getBlockState(pos);
            if (state.is(plainLeaves) || state.is(floweringLeaves)) {
                lowestLeaf = Math.min(lowestLeaf, pos.getY());
                highestLeaf = Math.max(highestLeaf, pos.getY());
            }
            if (state.is(Blocks.OAK_LOG) && state.getValue(RotatedPillarBlock.AXIS) != Direction.Axis.Y) {
                helper.assertTrue(
                        Math.abs(pos.getX() - SAPLING_POS.getX()) + Math.abs(pos.getZ() - SAPLING_POS.getZ()) <= 2,
                        "Sideways logs must stay close to the trunk");
                branchLogs++;
            } else if (state.is(plainLeaves)) {
                plainCount++;
                helper.assertTrue(
                        !state.getValue(LeavesBlock.PERSISTENT), "Generated leaves should decay without logs");
            } else if (state.is(floweringLeaves)) {
                var below = helper.getBlockState(pos.below());
                if (!below.is(fruit)) {
                    helper.assertTrue(
                            below.is(plainLeaves) || below.is(floweringLeaves) || below.is(Blocks.OAK_LOG),
                            "Blossoms without fruit should have a leaf or log beneath them");
                    blossomCount++;
                    continue;
                }
                helper.assertBlockProperty(pos.below(), LemonFruitBlock.AGE, 0);
                for (BlockPos other : fruitingSites) {
                    helper.assertTrue(
                            Math.abs(pos.getX() - other.getX()) > 1
                                    || Math.abs(pos.getY() - other.getY()) > 1
                                    || Math.abs(pos.getZ() - other.getZ()) > 1,
                            "Fruit sites should be spaced horizontally or vertically");
                }
                fruitingSites.add(pos.immutable());
            } else if (state.is(fruit)) {
                fruitCount++;
                helper.assertBlockPresent(floweringLeaves, pos.above());
            }
        }
        helper.assertTrue(branchLogs == 5, "Expected an upper branch and two shorter forks");
        helper.assertTrue(lowestLeaf == SAPLING_POS.getY() + 3, "Expected three blocks below the lowest leaves");
        helper.assertTrue(highestLeaf == fork.getY() + 4, "Expected the upper crown to rise above the other branches");
        helper.assertTrue(plainCount > 0, "Expected plain lemon leaves");
        helper.assertTrue(fruitingSites.size() == 8, "Expected eight fruiting leaves, got " + fruitingSites.size());
        helper.assertTrue(fruitCount == fruitingSites.size(), "Expected one young lemon per fruiting site");
        var fruitingHeights = fruitingSites.stream().mapToInt(BlockPos::getY).summaryStatistics();
        helper.assertTrue(fruitingHeights.getMin() == lowestLeaf, "Expected a lemon on the lowest leaf layer");
        helper.assertTrue(fruitingHeights.getMax() >= lowestLeaf + 2, "Expected a lemon under an upper branch");
        helper.assertTrue(blossomCount == 2, "Expected two visible blossoms with no room for fruit underneath");
        helper.succeed();
    }

    @GameTest(template = "lemon_tree_test")
    public static void lemonCanopyVariesWithTheGrowthSeed(GameTestHelper helper) {
        BlockPos second = SAPLING_POS.east(11);
        plantSapling(helper, SAPLING_POS);
        growSapling(helper, SAPLING_POS, 20);
        growSapling(helper, SAPLING_POS, 20);
        plantSapling(helper, second);
        growSapling(helper, second, 1);
        growSapling(helper, second, 1);
        helper.assertBlockPresent(Blocks.OAK_LOG, SAPLING_POS.above(4));
        helper.assertBlockPresent(Blocks.OAK_LOG, second.above(3));
        helper.assertTrue(!helper.getBlockState(second.above(4)).is(Blocks.OAK_LOG), "Expected a shorter second trunk");
        helper.assertTrue(
                hasRaisedSideBranch(helper, SAPLING_POS.above(4)) != hasRaisedSideBranch(helper, second.above(3)),
                "Expected the third branch to grow at different heights relative to the fork");

        List<BlockPos> firstCanopy = canopyShape(helper, SAPLING_POS);
        List<BlockPos> secondCanopy = canopyShape(helper, second);
        helper.assertTrue(
                firstCanopy.stream().mapToInt(BlockPos::getY).min().orElse(-1) == 3
                        && secondCanopy.stream().mapToInt(BlockPos::getY).min().orElse(-1) == 3,
                "Both tree heights should place their lowest leaves three blocks above the sapling base");
        helper.assertTrue(
                !firstCanopy.isEmpty() && !secondCanopy.isEmpty() && !firstCanopy.equals(secondCanopy),
                "Expected different canopy shapes with different seeds");
        helper.succeed();
    }

    @GameTest(template = "lemon_tree_test")
    public static void obstructedLemonTreeDoesNotReplaceTheObstacleOrLoseTheSapling(GameTestHelper helper) {
        plantSapling(helper);
        helper.setBlock(SAPLING_POS.above(), Blocks.STONE);
        growSapling(helper, 20);
        growSapling(helper, 20);
        helper.assertBlockPresent(Blocks.STONE, SAPLING_POS.above());
        helper.assertBlockPresent(PoptartFruitBlocks.LEMON_SAPLING.get(), SAPLING_POS);
        helper.assertBlockProperty(SAPLING_POS, SaplingBlock.STAGE, 1);
        helper.succeed();
    }

    private static void plantSapling(GameTestHelper helper) {
        plantSapling(helper, SAPLING_POS);
    }

    private static void plantSapling(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos.below(), Blocks.DIRT);
        helper.setBlock(pos, PoptartFruitBlocks.LEMON_SAPLING.get());
    }

    private static void growSapling(GameTestHelper helper, long seed) {
        growSapling(helper, SAPLING_POS, seed);
    }

    private static void growSapling(GameTestHelper helper, BlockPos pos, long seed) {
        PoptartFruitBlocks.LEMON_SAPLING
                .get()
                .performBonemeal(
                        helper.getLevel(),
                        RandomSource.create(seed),
                        helper.absolutePos(pos),
                        helper.getBlockState(pos));
    }

    private static void assertLemonLeaves(GameTestHelper helper, BlockPos pos) {
        var state = helper.getBlockState(pos);
        helper.assertTrue(
                state.is(PoptartFruitBlocks.LEMON_LEAVES.get())
                        || state.is(PoptartFruitBlocks.FLOWERING_LEMON_LEAVES.get()),
                "Expected lemon leaves at " + pos);
    }

    private static boolean hasRaisedSideBranch(GameTestHelper helper, BlockPos fork) {
        return Direction.Plane.HORIZONTAL.stream().anyMatch(direction -> {
            var state = helper.getBlockState(fork.relative(direction, 2));
            return state.is(Blocks.OAK_LOG) && state.getValue(RotatedPillarBlock.AXIS) == direction.getAxis();
        });
    }

    private static Iterable<BlockPos> canopyVolume(BlockPos base) {
        return BlockPos.betweenClosed(base.offset(-4, 0, -4), base.offset(4, 8, 4));
    }

    private static List<BlockPos> canopyShape(GameTestHelper helper, BlockPos base) {
        List<BlockPos> leaves = new ArrayList<>();
        for (BlockPos pos : canopyVolume(base)) {
            var state = helper.getBlockState(pos);
            if (state.is(PoptartFruitBlocks.LEMON_LEAVES.get())
                    || state.is(PoptartFruitBlocks.FLOWERING_LEMON_LEAVES.get())) {
                leaves.add(pos.subtract(base));
            }
        }
        return leaves;
    }
}
