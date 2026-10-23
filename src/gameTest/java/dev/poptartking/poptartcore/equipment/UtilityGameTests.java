package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class UtilityGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void bandageStacksUseNativeSixteenLimit(GameTestHelper helper) {
        for (String id : new String[] {"bandage", "antidote_bandage", "medicinal_bandage", "warding_bandage"}) {
            var player = new UtilityTestPlayer(helper);
            var stack = bandage(id);
            helper.assertTrue(stack.getMaxStackSize() == 16, id + " native stack size must be 16");
            var handler = new PlayerAccessoryHandler(player);
            var remainder = handler.insertItem(AccessoryInventory.UTILITY_SLOT, stack.copyWithCount(20), false);
            helper.assertTrue(
                    UtilityActions.stack(player).getCount() == 16 && remainder.getCount() == 4,
                    "Insert only native stack limit");
            var restored = new AccessoryInventory();
            restored.deserializeNBT(
                    helper.getLevel().registryAccess(),
                    AccessoryInventory.get(player)
                            .serializeNBT(helper.getLevel().registryAccess()));
            helper.assertTrue(
                    restored.getStackInSlot(AccessoryInventory.UTILITY_SLOT).getCount() == 16,
                    "Save and load preserves full stack");
            var slot = new AccessorySlot(player, AccessoryInventory.UTILITY_SLOT);
            helper.assertTrue(
                    slot.getMaxStackSize(stack) == 16 && slot.getMaxStackSize(new ItemStack(Items.PAPER)) == 1,
                    "Only bandages get expanded GUI limit");
            var corpseSlot = new dev.poptartking.poptartcore.integration.ragdoll.CorpseAccessorySlot(
                    new net.minecraft.world.SimpleContainer(AccessoryInventory.SIZE),
                    0,
                    AccessoryInventory.UTILITY_SLOT,
                    true,
                    false,
                    player);
            helper.assertTrue(
                    corpseSlot.getMaxStackSize(stack) == 16
                            && corpseSlot.getMaxStackSize(new ItemStack(Items.PAPER)) == 1,
                    "Corpse utility slot has same limits");
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void bandageShiftClickMergesAndNativeUseConsumesOne(GameTestHelper helper) {
        var player = new UtilityTestPlayer(helper);
        hands(player);
        var stack = bandage("bandage");
        player.getInventory().setItem(9, stack.copyWithCount(8));
        player.inventoryMenu.quickMoveStack(player, 9);
        helper.assertTrue(UtilityActions.stack(player).getCount() == 8, "Shift-click equips whole bandage stack");
        player.getInventory().setItem(9, stack.copyWithCount(16));
        player.inventoryMenu.quickMoveStack(player, 9);
        helper.assertTrue(
                UtilityActions.stack(player).getCount() == 16
                        && player.getInventory().getItem(9).getCount() == 8,
                "Shift-click tops up to 16 and leaves remainder");
        player.setHealth(8);
        begin(player);
        hold(player, stack.getUseDuration(player));
        UtilityWield.stop(player);
        helper.assertTrue(
                UtilityActions.stack(player).getCount() == 15, "Native use consumes exactly one from full stack");
        restored(helper, player);
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void manualPlacementAcceptsBandageStackButNotOtherUtilityStacks(GameTestHelper helper) {
        var player = new UtilityTestPlayer(helper);
        var slot = player.inventoryMenu.slots.stream()
                .filter(s -> s instanceof AccessorySlot accessory && accessory.accessoryType() == AccessoryType.UTILITY)
                .findFirst()
                .orElseThrow();
        player.inventoryMenu.setCarried(bandage("bandage").copyWithCount(16));
        player.inventoryMenu.clicked(slot.index, 0, net.minecraft.world.inventory.ClickType.PICKUP, player);
        helper.assertTrue(
                UtilityActions.stack(player).getCount() == 16
                        && player.inventoryMenu.getCarried().isEmpty(),
                "Mouse placement accepts all 16");
        UtilityActions.setStack(player, ItemStack.EMPTY);
        var remainder = new PlayerAccessoryHandler(player)
                .insertItem(AccessoryInventory.UTILITY_SLOT, new ItemStack(Items.TOTEM_OF_UNDYING, 2), false);
        helper.assertTrue(
                UtilityActions.stack(player).getCount() == 1 && remainder.getCount() == 1,
                "Other utility items stay limited to one");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void repeatedInputCommitsOnceAndSwapIsBlocked(GameTestHelper helper) {
        var player = new UtilityTestPlayer(helper);
        hands(player);
        var stack = bandage("bandage");
        stack.setCount(2);
        UtilityActions.setStack(player, stack);
        UtilityWield.input(player, true);
        var working = player.getMainHandItem();
        UtilityWield.input(player, true);
        helper.assertTrue(player.getMainHandItem() == working, "Heartbeat must not reset working item");
        working.shrink(1);
        UtilityWield.tick(player);
        helper.assertTrue(UtilityActions.stack(player).getCount() == 1, "Working changes commit to utility slot");
        var swap = new net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent.Hands(player);
        UtilityWield.swapHands(swap);
        helper.assertTrue(swap.isCanceled(), "Cannot swap a virtual item into the real offhand");
        UtilityWield.stop(player);
        UtilityWield.stop(player);
        helper.assertTrue(
                UtilityActions.stack(player).getCount() == 1, "Repeated stop cannot duplicate or consume another item");
        restored(helper, player);
        helper.succeed();
    }
    /** Drive the real native use tick without unrelated movement/dream-system ticks. */
    private static final class UtilityTestPlayer extends Player {
        UtilityTestPlayer(GameTestHelper helper) {
            super(
                    helper.getLevel(),
                    helper.absolutePos(net.minecraft.core.BlockPos.ZERO),
                    0,
                    new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "utility-test"));
        }

        @Override
        public boolean isSpectator() {
            return false;
        }

        @Override
        public boolean isCreative() {
            return false;
        }

        void tickUse() {
            if (isUsingItem()) updateUsingItem(getUseItem());
        }
    }

    private static ItemStack bandage(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("nomansland", id)));
    }

    private static void begin(Player player) {
        UtilityWield.input(player, true);
        player.getMainHandItem().use(player.level(), player, InteractionHand.MAIN_HAND);
    }

    private static void hold(Player player, int ticks) {
        for (int i = 0; i < ticks; i++) {
            UtilityWield.input(player, true);
            ((UtilityTestPlayer) player).tickUse();
            UtilityWield.tick(player);
        }
    }

    private static void hands(Player player) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.SHIELD));
    }

    private static void restored(GameTestHelper helper, Player player) {
        helper.assertTrue(
                player.getInventory().getSelected().is(Items.DIAMOND_SWORD)
                        && player.getMainHandItem().is(Items.DIAMOND_SWORD)
                        && player.getOffhandItem().is(Items.SHIELD),
                "Real hands must remain intact");
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void bandagesUseNativeLifecycleAndRestoreHands(GameTestHelper helper) {
        for (String id : new String[] {"bandage", "antidote_bandage", "medicinal_bandage", "warding_bandage"}) {
            var player = new UtilityTestPlayer(helper);
            player.setHealth(8);
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 200));
            hands(player);
            var stack = bandage(id);
            var item = stack.getItem();
            UtilityActions.setStack(player, stack);
            begin(player);
            helper.assertTrue(
                    player.isUsingItem() && player.getMainHandItem().is(item), "Native utility bandage use starts");
            helper.assertTrue(player.getInventory().getSelected().is(Items.DIAMOND_SWORD), "Hotbar is not moved");
            int duration = stack.getUseDuration(player);
            hold(player, duration - 1);
            helper.assertTrue(UtilityActions.stack(player).getCount() == 1, "No premature consumption");
            hold(player, 1);
            helper.assertTrue(UtilityActions.stack(player).isEmpty(), "Native completion consumes bandage");
            helper.assertTrue(player.getCooldowns().isOnCooldown(item), "Native bandage cooldown applies");
            if (id.equals("bandage")) helper.assertTrue(player.getHealth() > 8, "Bandage heals wearer");
            UtilityWield.stop(player);
            restored(helper, player);
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void releaseReplacementTimeoutAndPassiveTotem(GameTestHelper helper) {
        var player = new UtilityTestPlayer(helper);
        hands(player);
        UtilityActions.setStack(player, bandage("bandage"));
        begin(player);
        hold(player, 5);
        UtilityWield.input(player, false);
        helper.assertTrue(
                !player.isUsingItem() && UtilityActions.stack(player).getCount() == 1,
                "Release cancels use without consumption");
        restored(helper, player);
        begin(player);
        UtilityActions.setStack(player, bandage("antidote_bandage"));
        UtilityWield.tick(player);
        helper.assertTrue(
                !UtilityWield.isWielding(player)
                        && UtilityActions.stack(player)
                                .is(bandage("antidote_bandage").getItem()),
                "Replacement is not overwritten");
        begin(player);
        for (int i = 0; i < 31; i++) UtilityWield.tick(player);
        helper.assertTrue(!UtilityWield.isWielding(player), "Missing input heartbeat expires");
        restored(helper, player);
        UtilityActions.setStack(player, new ItemStack(Items.TOTEM_OF_UNDYING));
        helper.assertTrue(!UtilityWield.start(player), "Totem remains passive");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void replacementResultAndSpyglassUseVanillaHand(GameTestHelper helper) {
        var player = new UtilityTestPlayer(helper);
        hands(player);
        UtilityActions.setStack(player, new ItemStack(Items.SPYGLASS));
        begin(player);
        helper.assertTrue(player.isScoping(), "Vanilla spyglass scopes through virtual hand");
        UtilityWield.stop(player);
        helper.assertTrue(!player.isScoping(), "Release clears scope");
        restored(helper, player);
        UtilityWield.start(player);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
        UtilityWield.tick(player);
        helper.assertTrue(
                !UtilityWield.isWielding(player) && UtilityActions.stack(player).isEmpty(),
                "Non-utility result leaves slot");
        helper.assertTrue(
                player.getInventory().contains(new ItemStack(Items.GLASS_BOTTLE)),
                "Container result returned to inventory");
        restored(helper, player);
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void utilityBandageCannotHealStaleTarget(GameTestHelper helper) {
        var player = new UtilityTestPlayer(helper);
        var other = new UtilityTestPlayer(helper);
        player.setHealth(8);
        other.setHealth(8);
        var stack = bandage("bandage");
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        stack.getItem().interactLivingEntity(stack, player, other, InteractionHand.MAIN_HAND);
        player.stopUsingItem();
        hands(player);
        UtilityActions.setStack(player, stack);
        begin(player);
        hold(player, stack.getUseDuration(player));
        helper.assertTrue(
                player.getHealth() > 8 && other.getHealth() == 8, "Utility bandage treats wearer, not old target");
        restored(helper, player);
        helper.succeed();
    }
}
