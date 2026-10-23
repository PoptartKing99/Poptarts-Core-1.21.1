package dev.poptartking.poptartcore.spider;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TemporaryCobwebGameTests {
    private TemporaryCobwebGameTests() {}

    @GameTest(template = "millstone_test", timeoutTicks = 60)
    public static void bareHandEscapeTakesFortyTicksOnGroundOrSuspended(GameTestHelper helper) {
        var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var pos = helper.absolutePos(new BlockPos(2, 2, 2));
        var state = PoptartCoreBlocks.TEMPORARY_COBWEB.get().defaultBlockState();
        helper.getLevel().setBlockAndUpdate(pos, state);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
        for (boolean grounded : new boolean[] {true, false}) {
            player.setOnGround(grounded);
            float rate = state.getDestroyProgress(player, helper.getLevel(), pos);
            helper.assertTrue(
                    Math.abs(rate - 1.0F / 40) < 0.00001F,
                    "Bare hand breaks a temporary web in 40 ticks, grounded=" + grounded + ", rate=" + rate);
            helper.assertTrue(rate * 39 < 1 && rate * 40 >= .99999F, "Web survives 39 mining ticks and breaks at 40");
        }
        player.setOnGround(true);
        helper.assertTrue(
                Blocks.COBWEB.defaultBlockState().getDestroyProgress(player, helper.getLevel(), pos) < 1.0F / 40,
                "Natural webs retain their slower mining rate");
        helper.assertTrue(
                net.minecraft.world.level.block.Block.getDrops(
                                state, helper.getLevel(), pos, null, player, net.minecraft.world.item.ItemStack.EMPTY)
                        .isEmpty(),
                "Temporary webs still drop nothing");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 120)
    public static void expiresWithoutChangingNaturalCobweb(GameTestHelper helper) {
        BlockPos temporary = new BlockPos(2, 2, 2);
        BlockPos natural = new BlockPos(4, 2, 2);
        helper.setBlock(temporary, PoptartCoreBlocks.TEMPORARY_COBWEB.get());
        helper.setBlock(natural, Blocks.COBWEB);
        helper.runAtTickTime(99, () -> helper.assertBlockPresent(PoptartCoreBlocks.TEMPORARY_COBWEB.get(), temporary));
        helper.runAtTickTime(101, () -> {
            helper.assertBlockPresent(Blocks.AIR, temporary);
            helper.assertBlockPresent(Blocks.COBWEB, natural);
            helper.succeed();
        });
    }

    @GameTest(template = "millstone_test", timeoutTicks = 120)
    public static void expiryDoesNotRemoveReplacementBlock(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, PoptartCoreBlocks.TEMPORARY_COBWEB.get());
        helper.runAtTickTime(20, () -> helper.setBlock(pos, Blocks.STONE));
        helper.runAtTickTime(101, () -> {
            helper.assertBlockPresent(Blocks.STONE, pos);
            helper.succeed();
        });
    }
}
