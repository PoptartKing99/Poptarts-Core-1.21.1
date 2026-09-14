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
