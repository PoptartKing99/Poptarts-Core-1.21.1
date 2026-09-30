package dev.poptartking.poptartcore.treetap;

import com.farcr.nomansland.common.registry.items.NMLItems;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import dev.poptartking.poptartcore.registry.PoptartCoreFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TreeTapGameTests {
    private TreeTapGameTests() {}

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void jungleBucketGivesOneLatexBottlePerFillStage(GameTestHelper helper) {
        BlockPos tapPos = helper.absolutePos(new BlockPos(3, 2, 3));
        var level = helper.getLevel();
        level.setBlockAndUpdate(tapPos.south(), Blocks.JUNGLE_LOG.defaultBlockState());
        TreeTapBlock tap = PoptartCoreBlocks.TREE_TAP.get();
        BlockState bucket = tap.defaultBlockState()
                .setValue(TreeTapBlock.FACING, Direction.NORTH)
                .setValue(TreeTapBlock.WOOD, TreeTapBlock.Wood.JUNGLE)
                .setValue(TreeTapBlock.HAS_BUCKET, true);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var hit = new BlockHitResult(Vec3.atCenterOf(tapPos), Direction.NORTH, tapPos, false);
        ItemStack bottles = new ItemStack(Items.GLASS_BOTTLE, 5);
        level.setBlockAndUpdate(tapPos, bucket);
        var tapTank = level.getCapability(Capabilities.FluidHandler.BLOCK, tapPos, Direction.NORTH);
        helper.assertTrue(tapTank != null, "Jungle tap has no fluid tank");

        helper.assertTrue(TreeTapBlock.Wood.fromLog(Blocks.JUNGLE_LOG.defaultBlockState())
                == TreeTapBlock.Wood.JUNGLE, "Jungle log is not a tap source");
        for (int stage = 1; stage <= 5; stage++) {
            int toAdd = stage * LatexBottleFluidHandler.AMOUNT - tapTank.getFluidInTank(0).getAmount();
            int added = tapTank.fill(new FluidStack(PoptartCoreFluids.LATEX.source().get(),
                    toAdd),
                    IFluidHandler.FluidAction.EXECUTE);
            helper.assertTrue(added == toAdd,
                    "Tap did not accept the expected latex amount");
            BlockState filled = level.getBlockState(tapPos);
            helper.assertTrue(filled.getValue(TreeTapBlock.FILL_LEVEL) == stage, "Tank did not update the model");
            tap.useItemOn(bottles, filled, level, tapPos, player, net.minecraft.world.InteractionHand.MAIN_HAND, hit);

            helper.assertTrue(bottles.getCount() == 5 - stage, "Wrong number of glass bottles consumed");
            int latex = player.getInventory().items.stream()
                    .filter(stack -> stack.is(PoptartCoreItems.LATEX_BOTTLE.get()))
                    .mapToInt(ItemStack::getCount)
                    .sum();
            helper.assertTrue(latex == stage, "Wrong number of latex bottles at stage " + stage);
            BlockState remaining = level.getBlockState(tapPos);
            helper.assertTrue(remaining.getValue(TreeTapBlock.HAS_BUCKET), "Collecting latex removed the bucket");
            helper.assertTrue(!remaining.getValue(TreeTapBlock.FULL), "Collecting latex left the bucket full");
            helper.assertTrue(remaining.getValue(TreeTapBlock.FILL_LEVEL) == stage - 1,
                    "Collecting latex removed the wrong amount at stage " + stage);
            helper.assertTrue(tapTank.getFluidInTank(0).getAmount() == (stage - 1) * LatexBottleFluidHandler.AMOUNT,
                    "Collecting latex removed the wrong fluid amount at stage " + stage);
        }
        ItemStack latexBottle = new ItemStack(PoptartCoreItems.LATEX_BOTTLE.get());
        var bottleTank = latexBottle.getCapability(Capabilities.FluidHandler.ITEM);
        helper.assertTrue(bottleTank != null, "Latex bottle has no fluid capability");
        helper.assertTrue(bottleTank.getFluidInTank(0).getAmount() == LatexBottleFluidHandler.AMOUNT,
                "Latex bottle does not contain 200 mB");
        var barrelTank = new net.neoforged.neoforge.fluids.capability.templates.FluidTank(1000);
        var transfer = net.neoforged.neoforge.fluids.FluidUtil.tryEmptyContainer(
                latexBottle, barrelTank, 1000, player, true);
        helper.assertTrue(transfer.isSuccess(), "Latex bottle could not empty into a fluid tank");
        helper.assertTrue(barrelTank.getFluidAmount() == LatexBottleFluidHandler.AMOUNT,
                "Fluid tank received the wrong latex amount");
        helper.assertTrue(transfer.getResult().is(Items.GLASS_BOTTLE), "Emptying latex did not return a glass bottle");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void spruceBucketGivesTwoResinPerFillStage(GameTestHelper helper) {
        BlockPos tapPos = helper.absolutePos(new BlockPos(3, 2, 3));
        var level = helper.getLevel();
        level.setBlockAndUpdate(tapPos.south(), Blocks.SPRUCE_LOG.defaultBlockState());
        TreeTapBlock tap = PoptartCoreBlocks.TREE_TAP.get();
        BlockState bucket = tap.defaultBlockState()
                .setValue(TreeTapBlock.FACING, Direction.NORTH)
                .setValue(TreeTapBlock.WOOD, TreeTapBlock.Wood.SPRUCE)
                .setValue(TreeTapBlock.HAS_BUCKET, true);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var hit = new BlockHitResult(Vec3.atCenterOf(tapPos), Direction.NORTH, tapPos, false);

        int expectedResin = 0;
        for (int stage = 1; stage <= 5; stage++) {
            BlockState filled = bucket.setValue(TreeTapBlock.FILL_LEVEL, stage)
                    .setValue(TreeTapBlock.FULL, stage == 5);
            level.setBlockAndUpdate(tapPos, filled);
            player.setShiftKeyDown(true);
            tap.useWithoutItem(filled, level, tapPos, player, hit);
            helper.assertTrue(level.getBlockState(tapPos).equals(filled), "Sneaking removed a filled bucket");
            player.setShiftKeyDown(false);
            tap.useWithoutItem(filled, level, tapPos, player, hit);

            expectedResin += stage * 2;
            int resin = player.getInventory().items.stream()
                    .filter(stack -> stack.is(NMLItems.RESIN.get()))
                    .mapToInt(stack -> stack.getCount())
                    .sum();
            helper.assertTrue(resin == expectedResin, "Spruce bucket gave the wrong resin amount at stage " + stage);
            BlockState empty = level.getBlockState(tapPos);
            helper.assertTrue(empty.getValue(TreeTapBlock.HAS_BUCKET), "Resin collection removed the bucket");
            helper.assertTrue(!empty.getValue(TreeTapBlock.FULL), "Resin collection left the bucket full");
            helper.assertTrue(empty.getValue(TreeTapBlock.FILL_LEVEL) == 0, "Resin collection did not reset filling");
        }
        BlockState empty = level.getBlockState(tapPos);
        tap.useWithoutItem(empty, level, tapPos, player, hit);
        helper.assertTrue(level.getBlockState(tapPos).getValue(TreeTapBlock.HAS_BUCKET),
                "Normal right-click removed an empty bucket");
        player.setShiftKeyDown(true);
        tap.useWithoutItem(empty, level, tapPos, player, hit);
        helper.assertTrue(!level.getBlockState(tapPos).getValue(TreeTapBlock.HAS_BUCKET),
                "Sneaking did not remove an empty bucket");
        helper.assertTrue(player.getInventory().items.stream().anyMatch(stack -> stack.is(Items.BUCKET)),
                "Removing the bucket did not return it to the player");
        helper.succeed();
    }
}
