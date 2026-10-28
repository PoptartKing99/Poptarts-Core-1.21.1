package dev.poptartking.poptartcore.crossbow;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartToolItems;
import java.util.ArrayList;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RepeatingCrossbowGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void repeatingCrossbowLoadsOnlyAfterFifteenTicks(GameTestHelper helper) {
        var bow = PoptartToolItems.REPEATING_CROSSBOW.get();
        for (var ammo : new net.minecraft.world.item.Item[] {Items.ARROW, Items.FIREWORK_ROCKET}) {
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            player.getInventory().clearContent();
            var weapon = new ItemStack(bow);
            var projectiles = new ItemStack(ammo, 2);
            player.setItemInHand(InteractionHand.MAIN_HAND, weapon);
            player.setItemInHand(InteractionHand.OFF_HAND, projectiles);
            helper.assertTrue(
                    bow.getUseDuration(weapon, player) == 18,
                    "Repeating crossbow retains its 15-tick charge plus release buffer");
            bow.releaseUsing(weapon, helper.getLevel(), player, 4);
            helper.assertTrue(
                    !net.minecraft.world.item.CrossbowItem.isCharged(weapon) && projectiles.getCount() == 2,
                    "Releasing after 14 ticks must not load or consume ammo");
            bow.releaseUsing(weapon, helper.getLevel(), player, 3);
            helper.assertTrue(
                    net.minecraft.world.item.CrossbowItem.isCharged(weapon)
                            && weapon.get(DataComponents.CHARGED_PROJECTILES).contains(ammo)
                            && projectiles.getCount() == 1,
                    "Releasing after 15 ticks must load and consume exactly one projectile");
            bow.releaseUsing(weapon, helper.getLevel(), player, 0);
            helper.assertTrue(
                    projectiles.getCount() == 1, "Releasing an already loaded crossbow must not consume more ammo");
        }
        var emptyPlayer = helper.makeMockPlayer(GameType.SURVIVAL);
        emptyPlayer.getInventory().clearContent();
        var empty = new ItemStack(bow);
        emptyPlayer.setItemInHand(InteractionHand.MAIN_HAND, empty);
        helper.assertTrue(
                !bow.use(helper.getLevel(), emptyPlayer, InteractionHand.MAIN_HAND)
                        .getResult()
                        .consumesAction(),
                "A survival player cannot begin loading without ammo");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void holdingUseActuallyCompletesTheCharge(GameTestHelper helper) {
        var player = new ChargingPlayer(helper.getLevel());
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 2, 2))));
        player.getInventory().clearContent();
        var weapon = new ItemStack(PoptartToolItems.REPEATING_CROSSBOW.get());
        var ammo = new ItemStack(Items.ARROW, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, weapon);
        player.setItemInHand(InteractionHand.OFF_HAND, ammo);
        helper.assertTrue(
                weapon.getItem()
                        .use(helper.getLevel(), player, InteractionHand.MAIN_HAND)
                        .getResult()
                        .consumesAction(),
                "Use must begin charging with ammo available");
        for (int tick = 0; tick < 20 && player.isUsingItem(); tick++) player.tickItemUse();
        helper.assertTrue(
                !player.isUsingItem()
                        && net.minecraft.world.item.CrossbowItem.isCharged(weapon)
                        && ammo.getCount() == 1,
                "Held use must finish loading, consume one arrow, and stop using the item");
        helper.succeed();
    }

    private static final class ChargingPlayer extends net.neoforged.neoforge.common.util.FakePlayer {
        ChargingPlayer(net.minecraft.server.level.ServerLevel level) {
            super(level, new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "crossbow-test"));
            setGameMode(GameType.SURVIVAL);
        }

        void tickItemUse() {
            // FakePlayer deliberately disables whole-player ticks. Run the real item-use tick instead.
            updateUsingItem(getUseItem());
        }
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void circuitRepairsAndWornOutCrossbowsCannotBeUsed(GameTestHelper helper) {
        var bow = PoptartToolItems.REPEATING_CROSSBOW.get();
        var weapon = new ItemStack(bow);
        helper.assertTrue(
                bow.isValidRepairItem(
                        weapon,
                        new ItemStack(
                                dev.poptartking.poptartcore.registry.PoptartMaterialItems.REDSTONE_CIRCUIT.get())),
                "Redstone circuits must repair the repeating crossbow");
        for (var wrong : new net.minecraft.world.item.Item[] {Items.CROSSBOW, Items.REDSTONE, Items.IRON_INGOT})
            helper.assertTrue(
                    !bow.isValidRepairItem(weapon, new ItemStack(wrong)),
                    "Unrelated items must not repair the repeating crossbow");
        dev.poptartking.poptartcore.anvil.AnvilMenu.applyUpgrades(weapon, 1);
        weapon.setDamageValue(weapon.getMaxDamage() - 1);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, weapon);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.ARROW, 2));
        helper.assertTrue(
                !bow.use(helper.getLevel(), player, InteractionHand.MAIN_HAND)
                        .getResult()
                        .consumesAction(),
                "Worn-out crossbow must reject use");
        bow.releaseUsing(weapon, helper.getLevel(), player, 0);
        helper.assertTrue(
                !net.minecraft.world.item.CrossbowItem.isCharged(weapon), "Worn-out crossbow must not load ammo");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void repeatingCrossbowMatchesNormalArrowSpread(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(4, 2, 4))));
        for (long seed : new long[] {123456789L, 987654321L}) {
            var shots = new ArrayList<Vec3>();
            Consumer<EntityEvent.EntityConstructing> seedArrow = event -> {
                if (event.getEntity() instanceof Arrow arrow && arrow.level() == level) {
                    arrow.getRandom().setSeed(seed);
                }
            };
            Consumer<EntityJoinLevelEvent> recordShot = event -> {
                if (event.getLevel() == level
                        && event.getEntity() instanceof Arrow arrow
                        && arrow.getOwner() == player) {
                    shots.add(arrow.getDeltaMovement());
                    event.setCanceled(true);
                }
            };
            NeoForge.EVENT_BUS.addListener(seedArrow);
            NeoForge.EVENT_BUS.addListener(recordShot);
            try {
                for (var item :
                        new net.minecraft.world.item.Item[] {Items.CROSSBOW, PoptartToolItems.REPEATING_CROSSBOW.get()
                        }) {
                    var weapon = new ItemStack(item);
                    weapon.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(new ItemStack(Items.ARROW)));
                    player.setItemInHand(InteractionHand.MAIN_HAND, weapon);
                    player.setYRot(0);
                    player.setYHeadRot(0);
                    player.setXRot(0);
                    helper.assertTrue(
                            item.use(level, player, InteractionHand.MAIN_HAND)
                                    .getResult()
                                    .consumesAction(),
                            "Loaded crossbow fires through its normal use interaction");
                    helper.assertTrue(
                            weapon.get(DataComponents.CHARGED_PROJECTILES).isEmpty(),
                            "Firing consumes the loaded projectile");
                }
                helper.assertTrue(shots.size() == 2, "Both weapons launch one arrow");
                helper.assertTrue(Math.abs(shots.getFirst().x) > 0.000001, "Normal crossbow retains random spread");
                helper.assertTrue(
                        shots.getFirst().distanceToSqr(shots.getLast()) < 1.0E-12,
                        "Repeating crossbow matches normal arrow motion for the same aim and random seed");
            } finally {
                NeoForge.EVENT_BUS.unregister(seedArrow);
                NeoForge.EVENT_BUS.unregister(recordShot);
            }
        }
        helper.succeed();
    }
}
