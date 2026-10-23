package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartRingItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MagnetObstacleGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void magnetPullsItemOverOneBlockStep(GameTestHelper helper) {
        simulate(helper, Blocks.STONE.defaultBlockState(), false, false, 1);
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void magnetDoesNotLiftItemsUpFiveBlockWalls(GameTestHelper helper) {
        simulate(helper, Blocks.STONE.defaultBlockState(), true, false, 0);
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void magnetClimbsTwoThreeAndFourBlocksWithoutBouncing(GameTestHelper helper) {
        for (int height = 2; height <= 4; height++) {
            simulate(helper, Blocks.STONE.defaultBlockState(), false, false, height);
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void magnetStepsOverSlabsWithoutBouncing(GameTestHelper helper) {
        simulate(helper, Blocks.STONE_SLAB.defaultBlockState(), false, false, .5);
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void magnetStepsOverStairsWithoutBouncing(GameTestHelper helper) {
        simulate(
                helper,
                Blocks.STONE_STAIRS
                        .defaultBlockState()
                        .setValue(net.minecraft.world.level.block.StairBlock.FACING, net.minecraft.core.Direction.EAST),
                false,
                false,
                1);
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void magnetCannotStepThroughLowCeilings(GameTestHelper helper) {
        simulate(helper, Blocks.STONE.defaultBlockState(), false, true, 0);
        helper.succeed();
    }

    private static void simulate(
            GameTestHelper helper,
            net.minecraft.world.level.block.state.BlockState obstacle,
            boolean tallWall,
            boolean lowCeiling,
            double expectedHeight) {
        var origin = helper.absolutePos(new BlockPos(1, 1, 1));
        int wallHeight = tallWall ? 5 : Math.max(1, (int) Math.ceil(expectedHeight));
        for (int x = 0; x < 7; x++) {
            for (int z = 0; z < 5; z++) {
                helper.getLevel().setBlockAndUpdate(origin.offset(x, -1, z), Blocks.STONE.defaultBlockState());
                if (x == 3) {
                    helper.getLevel().setBlockAndUpdate(origin.offset(x, 0, z), obstacle);
                    for (int y = 1; y < wallHeight; y++)
                        helper.getLevel().setBlockAndUpdate(origin.offset(x, y, z), Blocks.STONE.defaultBlockState());
                }
                if (lowCeiling)
                    helper.getLevel().setBlockAndUpdate(origin.offset(x, 1, z), Blocks.STONE.defaultBlockState());
            }
        }
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(
                origin.getX() + 4.5, origin.getY() + Math.min(1, Math.max(.5, expectedHeight)), origin.getZ() + 2.5);
        new PlayerAccessoryHandler(player).setStackInSlot(0, new ItemStack(PoptartRingItems.MAGNET_RING.get()));
        var item = new ItemEntity(
                helper.getLevel(),
                origin.getX() + 1.5,
                origin.getY() + .01,
                origin.getZ() + 2.5,
                new ItemStack(Items.DIRT));
        item.setNoPickUpDelay();
        item.setDeltaMovement(Vec3.ZERO);
        helper.getLevel().addFreshEntity(item);
        helper.assertTrue(item.maxUpStep() == 0, "Ordinary dropped items keep vanilla movement");
        double highest = item.getY();
        for (int tick = 0; tick < 100; tick++) {
            double before = item.getY();
            RingEffects.tick(player);
            item.tick();
            helper.assertTrue(
                    item.getY() - before <= MagnetGlide.SPEED + .0001,
                    "Climbing must glide gradually, never jump to the top");
            helper.assertTrue(
                    helper.getLevel().noCollision(item, item.getBoundingBox().deflate(1e-7)),
                    "Gliding cannot pass through solid blocks");
            highest = Math.max(highest, item.getY());
        }
        if (tallWall || lowCeiling) {
            helper.assertTrue(
                    highest < origin.getY() + .5, "Walls above the limit and low ceilings must block climbing");
        } else {
            helper.assertTrue(
                    item.getX() > origin.getX() + 3.3 && highest >= origin.getY() + expectedHeight - .001,
                    "Pulled item must clear height " + expectedHeight + "; x=" + (item.getX() - origin.getX())
                            + ", highest=" + (highest - origin.getY()) + ", step=" + item.maxUpStep()
                            + ", grounded=" + item.onGround() + ", velocity=" + item.getDeltaMovement());
            helper.assertTrue(
                    highest <= origin.getY() + expectedHeight + .02,
                    "Item rises only to the obstacle surface, without a hop");
        }
        new PlayerAccessoryHandler(player).setStackInSlot(0, ItemStack.EMPTY);
        helper.assertTrue(item.maxUpStep() == 0, "Unequipping immediately removes step permission");
        item.discard();
    }
}
