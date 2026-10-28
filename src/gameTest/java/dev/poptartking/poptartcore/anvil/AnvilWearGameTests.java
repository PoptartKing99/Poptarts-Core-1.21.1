package dev.poptartking.poptartcore.anvil;

import com.mojang.authlib.GameProfile;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartToolItems;
import dev.poptartking.poptartcore.registry.PoptartWorkstationBlocks;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AnvilWearGameTests {
    private static final class WearRandom extends LegacyRandomSource {
        float roll = .5F;
        int calls;

        WearRandom() {
            super(42);
        }

        @Override
        public float nextFloat() {
            calls++;
            return roll;
        }
    }

    private static final class WearPlayer extends Player {
        final WearRandom wear = new WearRandom();

        WearPlayer(GameTestHelper helper) {
            super(
                    helper.getLevel(),
                    helper.absolutePos(BlockPos.ZERO),
                    0,
                    new GameProfile(UUID.randomUUID(), "anvil-wear-test"));
        }

        @Override
        public boolean isSpectator() {
            return false;
        }

        @Override
        public boolean isCreative() {
            return false;
        }

        @Override
        public RandomSource getRandom() {
            return wear == null ? super.getRandom() : wear;
        }
    }

    private static void checkClicks(GameTestHelper helper, ClickType click, boolean primitive) {
        var player = new WearPlayer(helper);
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        Block normal = (primitive ? PoptartWorkstationBlocks.PRIMITIVE_ANVIL : PoptartWorkstationBlocks.ANVIL).get();
        Block chipped = (primitive
                        ? PoptartWorkstationBlocks.CHIPPED_PRIMITIVE_ANVIL
                        : PoptartWorkstationBlocks.CHIPPED_ANVIL)
                .get();
        Block damaged = (primitive
                        ? PoptartWorkstationBlocks.DAMAGED_PRIMITIVE_ANVIL
                        : PoptartWorkstationBlocks.DAMAGED_ANVIL)
                .get();
        helper.getLevel().setBlockAndUpdate(pos, normal.defaultBlockState());
        var menu = new AnvilMenu(1, player.getInventory(), ContainerLevelAccess.create(helper.getLevel(), pos));
        menu.getSlot(2).set(new ItemStack(PoptartToolItems.HAMMER.get()));
        float[] rolls = primitive ? new float[] {.9F, .25F, .249F, .1F, 0F} : new float[] {.9F, .05F, .049F, .025F, 0F};
        Block[] expected = {normal, normal, chipped, damaged, Blocks.AIR};
        for (int i = 0; i < rolls.length; i++) {
            player.wear.roll = rolls[i];
            int before = player.wear.calls;
            var tool = new ItemStack(Items.DIAMOND_SWORD);
            tool.setDamageValue(1000);
            menu.getSlot(0).set(tool);
            menu.getSlot(1).set(new ItemStack(Items.DIAMOND));
            menu.createResult();
            helper.assertTrue(menu.getSlot(3).hasItem(), "Repair preview exists");
            var expectedResult = menu.getSlot(3).getItem().copy();
            helper.assertTrue(player.wear.calls == before, "Preview does not roll wear");
            menu.clicked(3, 0, click, player);
            helper.assertTrue(
                    player.wear.calls == before + 1, "Exactly one wear roll per actual " + click + " result pickup");
            helper.assertTrue(
                    menu.getSlot(0).getItem().isEmpty()
                            && menu.getSlot(1).getItem().isEmpty(),
                    "Click consumes both inputs");
            helper.assertTrue(
                    helper.getLevel().getBlockState(pos).is(expected[i]),
                    "Actual " + click + " pickup reaches expected damage stage " + i);
            if (click == ClickType.PICKUP) {
                helper.assertTrue(menu.getCarried().is(Items.DIAMOND_SWORD), "Normal click receives repaired item");
                menu.setCarried(ItemStack.EMPTY);
            } else
                helper.assertTrue(
                        player.getInventory().items.stream()
                                .anyMatch(stack -> ItemStack.isSameItemSameComponents(stack, expectedResult)),
                        "Shift click transfers result to inventory");
        }
        helper.assertTrue(!menu.hasWorkingHammer(), "Broken anvil no longer exposes stored hammer");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void normalResultClicksRollFivePercentAndBreak(GameTestHelper helper) {
        checkClicks(helper, ClickType.PICKUP, false);
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void shiftResultClicksRollFivePercentAndBreak(GameTestHelper helper) {
        checkClicks(helper, ClickType.QUICK_MOVE, false);
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void primitiveNormalClicksRollTwentyFivePercentAndBreak(GameTestHelper helper) {
        checkClicks(helper, ClickType.PICKUP, true);
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void primitiveShiftClicksRollTwentyFivePercentAndBreak(GameTestHelper helper) {
        checkClicks(helper, ClickType.QUICK_MOVE, true);
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void nativeRandomSampleProducesExpectedWearFrequency(GameTestHelper helper) {
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        for (boolean primitive : new boolean[] {false, true}) {
            RandomSource random = RandomSource.create(81923L);
            int worn = 0;
            for (int i = 0; i < 1000; i++) {
                var state = (primitive ? PoptartWorkstationBlocks.PRIMITIVE_ANVIL : PoptartWorkstationBlocks.ANVIL)
                        .get()
                        .defaultBlockState();
                helper.getLevel().setBlockAndUpdate(pos, state);
                ((AnvilBlock) state.getBlock()).maybeDamage(helper.getLevel(), pos, state, random);
                if (helper.getLevel()
                        .getBlockState(pos)
                        .is((primitive
                                        ? PoptartWorkstationBlocks.CHIPPED_PRIMITIVE_ANVIL
                                        : PoptartWorkstationBlocks.CHIPPED_ANVIL)
                                .get())) worn++;
            }
            int expected = primitive ? 250 : 50;
            helper.assertTrue(
                    Math.abs(worn - expected) <= (primitive ? 50 : 25),
                    "Seeded wear sample for primitive=" + primitive + ": " + worn + "/1000");
            System.out.println(
                    "Anvil wear audit: primitive=" + primitive + ", " + worn + "/1000 fresh anvils chipped.");
        }
        helper.succeed();
    }
}
