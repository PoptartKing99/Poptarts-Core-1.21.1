package dev.poptartking.poptartcore.beekeeping;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vectorwing.farmersdelight.common.registry.ModItems;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BeeSmokerGameTests {
    private BeeSmokerGameTests() {}

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void smokerStopsAtEmptyInsteadOfBreaking(GameTestHelper helper) {
        BeeSmokerItem smokerItem = PoptartCoreItems.BEE_SMOKER.get();
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
        BeeSmokerItem smokerItem = PoptartCoreItems.BEE_SMOKER.get();
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
        BeeSmokerItem smokerItem = PoptartCoreItems.BEE_SMOKER.get();
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
        BeeSmokerItem smokerItem = PoptartCoreItems.BEE_SMOKER.get();
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
}
