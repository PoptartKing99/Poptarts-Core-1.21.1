package dev.poptartking.poptartcore.milk;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import dev.poptartking.poptartcore.registry.PoptartCoreDataMaps;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vectorwing.farmersdelight.common.registry.ModItems;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MilkingGameTests {
    private MilkingGameTests() {}

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void bucketDrainsCowAndBlocksSecondMilking(GameTestHelper helper) {
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(3, 1, 3));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        NeoForge.EVENT_BUS.post(new EntityInteract(player, InteractionHand.MAIN_HAND, cow));
        helper.assertTrue(player.getMainHandItem().is(Items.MILK_BUCKET), "A full cow did not fill the bucket");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        NeoForge.EVENT_BUS.post(new EntityInteract(player, InteractionHand.MAIN_HAND, cow));
        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "An empty cow filled a second bucket");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void cowProvidesFourMilkBottles(GameTestHelper helper) {
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(3, 1, 3));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        for (int i = 0; i < 4; i++) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
            NeoForge.EVENT_BUS.post(new EntityInteract(player, InteractionHand.MAIN_HAND, cow));
            helper.assertTrue(
                    player.getMainHandItem().is(ModItems.MILK_BOTTLE.get()),
                    "Milk bottle " + (i + 1) + " was not filled");
        }

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
        NeoForge.EVENT_BUS.post(new EntityInteract(player, InteractionHand.MAIN_HAND, cow));
        helper.assertTrue(player.getMainHandItem().is(Items.GLASS_BOTTLE), "An empty cow filled a fifth bottle");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void milkSupplyRechargesInQuarterSteps(GameTestHelper helper) {
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(3, 1, 3));
        MilkRecharge recharge = PoptartCoreDataMaps.milkRecharge(cow.getType());
        helper.assertTrue(recharge != null, "Cow milk recharge data did not load");

        long now = cow.level().getGameTime();
        cow.setData(PoptartCoreAttachments.MILK_FULL_AT.get(), now + recharge.rechargeTicks());
        helper.assertTrue(Milking.available(cow, recharge) == 0.0F, "A drained cow still had milk");

        cow.setData(PoptartCoreAttachments.MILK_FULL_AT.get(), now + Math.round(recharge.rechargeTicks() * 0.75F));
        helper.assertTrue(
                Milking.available(cow, recharge) == 0.25F, "Cow did not recharge one quarter after 75 seconds");
        helper.succeed();
    }
}
