package dev.poptartking.poptartcore.beekeeping;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartToolItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vectorwing.farmersdelight.common.registry.ModItems;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BeeSmokerGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void smokerRetainsCapacityAndStackLimit(GameTestHelper helper) {
        var stack = new ItemStack(PoptartToolItems.BEE_SMOKER.get());
        helper.assertTrue(
                stack.getMaxDamage() == 360 && stack.getMaxStackSize() == 1,
                "Smoker keeps its capacity and stack limit");
        helper.succeed();
    }

    private BeeSmokerGameTests() {}

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void smokerStopsAtEmptyInsteadOfBreaking(GameTestHelper helper) {
        BeeSmokerItem smokerItem = PoptartToolItems.BEE_SMOKER.get();
        ItemStack smoker = new ItemStack(smokerItem);
        smoker.setDamageValue(smoker.getMaxDamage() - 2);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, smoker);
        player.startUsingItem(InteractionHand.MAIN_HAND);

        smokerItem.onUseTick(helper.getLevel(), player, smoker, smokerItem.getUseDuration(smoker, player) - 20);

        helper.assertTrue(smoker.getCount() == 1, "The smoker broke at the end of its fuel");
        helper.assertTrue(BeeSmokerItem.isEmpty(smoker), "The smoker did not enter its empty state");
        helper.assertTrue(!player.isUsingItem(), "The player continued using an empty smoker");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void emptySmokerCannotBeUsed(GameTestHelper helper) {
        BeeSmokerItem smokerItem = PoptartToolItems.BEE_SMOKER.get();
        ItemStack smoker = new ItemStack(smokerItem);
        smoker.setDamageValue(smoker.getMaxDamage() - 1);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, smoker);

        InteractionResult result = smokerItem
                .use(helper.getLevel(), player, InteractionHand.MAIN_HAND)
                .getResult();

        helper.assertTrue(result == InteractionResult.FAIL, "The empty smoker allowed use");
        helper.assertTrue(!player.isUsingItem(), "The player started using an empty smoker");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void strawRefillsEmptySmoker(GameTestHelper helper) {
        BeeSmokerItem smokerItem = PoptartToolItems.BEE_SMOKER.get();
        ItemStack smoker = new ItemStack(smokerItem);
        smoker.setDamageValue(smoker.getMaxDamage() - 1);
        ItemStack straw = new ItemStack(ModItems.STRAW.get(), 2);
        SimpleContainer container = new SimpleContainer(smoker);
        Slot slot = new Slot(container, 0, 0, 0);

        boolean handled = smokerItem.overrideOtherStackedOnMe(
                smoker, straw, slot, ClickAction.PRIMARY, helper.makeMockPlayer(GameType.SURVIVAL), SlotAccess.NULL);

        helper.assertTrue(handled, "Empty smoker did not accept straw");
        helper.assertTrue(smoker.getDamageValue() == 0, "Straw did not fully refill the smoker");
        helper.assertTrue(straw.getCount() == 1, "Refilling did not consume exactly one straw");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void filledSmokerDoesNotConsumeStraw(GameTestHelper helper) {
        BeeSmokerItem smokerItem = PoptartToolItems.BEE_SMOKER.get();
        ItemStack smoker = new ItemStack(smokerItem);
        ItemStack straw = new ItemStack(ModItems.STRAW.get(), 2);
        SimpleContainer container = new SimpleContainer(smoker);
        Slot slot = new Slot(container, 0, 0, 0);

        boolean handled = smokerItem.overrideOtherStackedOnMe(
                smoker, straw, slot, ClickAction.PRIMARY, helper.makeMockPlayer(GameType.SURVIVAL), SlotAccess.NULL);

        helper.assertTrue(!handled, "A filled smoker incorrectly accepted straw");
        helper.assertTrue(straw.getCount() == 2, "A filled smoker consumed straw");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 360)
    public static void completedApplicationsResetProgressAndKeepSmokeDuration(GameTestHelper helper) {
        var spray = hiveSpray(helper);
        spray.apply(99);
        helper.assertTrue(!spray.isSmoked(), "A hive requires all 100 application ticks");
        spray.apply(1);
        helper.assertTrue(spray.isSmoked(), "The hundredth application tick smokes the hive");

        helper.runAfterDelay(5, () -> spray.apply(50));
        helper.runAfterDelay(
                301,
                () -> helper.assertTrue(spray.isSmoked(), "Continued spray refreshes the existing 300-tick effect"));
        helper.runAfterDelay(306, () -> {
            helper.assertTrue(!spray.isSmoked(), "Smoke expires 300 ticks after the last spray");
            spray.apply(99);
            helper.assertTrue(!spray.isSmoked(), "Completed or refreshed applications cannot prefill the next one");
            spray.apply(1);
            helper.assertTrue(spray.isSmoked(), "A later application still requires 100 ticks");
            helper.succeed();
        });
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void replacementHivesDoNotInheritPartialProgress(GameTestHelper helper) {
        var spray = hiveSpray(helper);
        spray.apply(99);
        helper.assertTrue(!spray.isSmoked(), "Original hive is not fully smoked yet");
        spray.replaceHive();
        spray.apply(1);
        helper.assertTrue(!spray.isSmoked(), "Replacement hive does not inherit the original's 99 ticks");
        spray.apply(98);
        helper.assertTrue(!spray.isSmoked(), "Replacement still needs its final application tick");
        spray.apply(1);
        helper.assertTrue(spray.isSmoked(), "Replacement can receive its own full application");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void replacementHivesDoNotInheritCompletedSmoke(GameTestHelper helper) {
        var spray = hiveSpray(helper);
        spray.apply(100);
        helper.assertTrue(spray.isSmoked(), "Original hive is smoked");
        spray.replaceHive();
        helper.assertTrue(!spray.isSmoked(), "A new hive does not inherit the old hive's smoked effect");
        spray.apply(99);
        helper.assertTrue(!spray.isSmoked(), "New hive requires its own five-second application");
        spray.apply(1);
        helper.assertTrue(spray.isSmoked(), "New hive becomes smoked after all 100 ticks");
        helper.succeed();
    }

    private static HiveSpray hiveSpray(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(4, 2, 4));
        for (int distance = 1; distance <= 3; distance++) {
            level.setBlockAndUpdate(pos.north(distance), Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(pos, Blocks.BEEHIVE.defaultBlockState());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(pos.getX() + .5, pos.getY() + .5 - player.getEyeHeight(), pos.getZ() - 2);
        player.setYRot(0);
        player.setYHeadRot(0);
        player.setXRot(0);
        var smoker = new ItemStack(PoptartToolItems.BEE_SMOKER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, smoker);
        return new HiveSpray(level, pos, player, smoker);
    }

    private record HiveSpray(ServerLevel level, BlockPos pos, Player player, ItemStack smoker) {
        void apply(int ticks) {
            var item = (BeeSmokerItem) smoker.getItem();
            player.startUsingItem(InteractionHand.MAIN_HAND);
            for (int tick = 1; tick <= ticks; tick++) {
                item.onUseTick(level, player, smoker, item.getUseDuration(smoker, player) - tick);
            }
            player.stopUsingItem();
        }

        boolean isSmoked() {
            return SmokedHives.isSmoked(level, pos);
        }

        void replaceHive() {
            level.removeBlock(pos, false);
            level.setBlockAndUpdate(pos, Blocks.BEEHIVE.defaultBlockState());
        }
    }
}
