package dev.poptartking.poptartcore.equipment;

import com.evandev.reliable_backpacks.common.blocks.BackpackBlockEntity;
import com.evandev.reliable_backpacks.common.events.BackpackPickupEvents;
import com.evandev.reliable_backpacks.common.menus.BackpackMenu;
import com.evandev.reliable_backpacks.platform.Services;
import com.evandev.reliable_backpacks.registry.BPItems;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.integration.reliablebackpacks.BackSlotBackpack;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BackpackGameTests {
    private BackpackGameTests() {}

    private static ItemStack filledBackpack() {
        var stack = new ItemStack(BPItems.BACKPACK);
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND, 5))));
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(0x48AACC, false));
        return stack;
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void backtankHidesBackpackWithoutUnequippingIt(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (var tank : List.of(
                com.simibubi.create.AllItems.COPPER_BACKTANK.asStack(),
                com.simibubi.create.AllItems.NETHERITE_BACKTANK.asStack())) {
            var backpack = filledBackpack();
            BackSlotBackpack.equip(player, backpack);
            player.setItemSlot(EquipmentSlot.CHEST, tank);
            helper.assertTrue(!Services.PLATFORM.isBackpackVisible(player), "Chest backtank must hide backpack model");
            helper.assertTrue(
                    Services.PLATFORM.getEquippedBackpack(player) == backpack,
                    "Hidden backpack must remain equipped with its real contents");
            helper.assertTrue(
                    backpack.get(DataComponents.CONTAINER).stream()
                            .anyMatch(stack -> stack.is(Items.DIAMOND) && stack.getCount() == 5),
                    "Hiding must not change stored items");
            player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
            helper.assertTrue(
                    Services.PLATFORM.isBackpackVisible(player), "Removing chest backtank must reveal backpack");
            BackSlotBackpack.equip(player, tank);
            player.setItemSlot(EquipmentSlot.CHEST, backpack);
            helper.assertTrue(
                    !Services.PLATFORM.isBackpackVisible(player), "Back-slot backtank must also hide backpack model");
            helper.assertTrue(
                    Services.PLATFORM.getEquippedBackpack(player) == backpack,
                    "Chest backpack must remain functional when hidden");
            BackSlotBackpack.equip(player, ItemStack.EMPTY);
            helper.assertTrue(
                    Services.PLATFORM.isBackpackVisible(player), "Removing Back-slot tank must reveal chest backpack");
            player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void backpackSaveAndCorpseRecoveryKeepContents(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var backpack = filledBackpack();
        Services.PLATFORM.equipBackpack(player, backpack);
        var saved = player.saveWithoutId(new net.minecraft.nbt.CompoundTag());
        var loaded = helper.makeMockPlayer(GameType.SURVIVAL);
        loaded.load(saved);
        helper.assertTrue(
                ItemStack.matches(backpack, BackSlotBackpack.get(loaded)),
                "Save/load must preserve backpack contents and dye");
        var rules = helper.getLevel().getGameRules();
        var keepRule = net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY;
        boolean keep = rules.getBoolean(keepRule);
        try {
            rules.getRule(keepRule).set(false, helper.getLevel().getServer());
            int slot = com.farcr.ragdoll.api.inventory.CorpseSlots.baseSlot(
                            dev.poptartking.poptartcore.integration.ragdoll.AccessoryCorpseProvider.ID)
                    + AccessoryInventory.BACK_SLOT;
            var captured = com.farcr.ragdoll.api.inventory.CorpseSlots.capture(loaded);
            helper.assertTrue(
                    ItemStack.matches(backpack, captured.get(slot)), "Corpse must capture filled backpack intact");
            helper.assertTrue(BackSlotBackpack.get(loaded).isEmpty(), "Death capture must clear Back slot");
            helper.assertTrue(
                    com.farcr.ragdoll.api.inventory.CorpseSlots.restore(loaded, slot, captured.get(slot))
                            .isEmpty(),
                    "Corpse recovery must restore filled backpack to Back");
            helper.assertTrue(
                    ItemStack.matches(backpack, BackSlotBackpack.get(loaded)),
                    "Recovery must preserve every backpack component");
        } finally {
            rules.getRule(keepRule).set(keep, helper.getLevel().getServer());
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void backpackUsesBackSlotAndRejectsDuplicates(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var backpack = filledBackpack();
        var chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        player.setItemSlot(EquipmentSlot.CHEST, chestplate);
        helper.assertTrue(Services.PLATFORM.canEquipBackpack(player), "Chestplate must not block equipping in Back");
        Services.PLATFORM.equipBackpack(player, backpack);
        helper.assertTrue(
                BackSlotBackpack.get(player) == backpack, "Native equip must store the actual backpack in Back");
        helper.assertTrue(
                Services.PLATFORM.getEquippedBackpack(player) == backpack, "Native lookup must return actual storage");
        helper.assertTrue(Services.PLATFORM.isBackpackVisible(player), "Backpack renderer must see the Back slot");
        helper.assertTrue(
                player.getItemBySlot(EquipmentSlot.CHEST) == chestplate, "Backpack must not replace chestplate");
        helper.assertTrue(!Services.PLATFORM.canEquipBackpack(player), "Native equip must reject a second backpack");
        helper.assertTrue(
                !new ItemStack(BPItems.BACKPACK).canEquip(EquipmentSlot.CHEST, player),
                "Chest slot must reject duplicate backpack");
        Services.PLATFORM.unequipBackpack(player);
        player.setItemSlot(EquipmentSlot.CHEST, backpack);
        helper.assertTrue(
                !new PlayerAccessoryHandler(player).isItemValid(AccessoryInventory.BACK_SLOT, backpack),
                "Chest backpack must block Back placement");
        helper.assertTrue(!Services.PLATFORM.canEquipBackpack(player), "Chest backpack must block native equip");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void backpackGroundRoundTripPreservesContents(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var base = new BlockPos(4, 1, 4);
        var placed = base.above();
        helper.setBlock(base, Blocks.STONE);
        helper.setBlock(placed, Blocks.AIR);
        var absoluteBase = helper.absolutePos(base);
        player.setPos(Vec3.atCenterOf(absoluteBase).add(0, 1, 2));
        player.setShiftKeyDown(true);
        Services.PLATFORM.equipBackpack(player, filledBackpack());
        var placeHit =
                new BlockHitResult(Vec3.atCenterOf(absoluteBase).add(0, 0.5, 0), Direction.UP, absoluteBase, false);
        var placedResult =
                BackpackPickupEvents.onRightClickBlock(player, helper.getLevel(), InteractionHand.MAIN_HAND, placeHit);
        helper.assertTrue(placedResult.consumesAction(), "Native sneak-right-click must place equipped backpack");
        helper.assertTrue(BackSlotBackpack.get(player).isEmpty(), "Placement must empty Back slot");
        helper.assertTrue(
                helper.getBlockEntity(placed) instanceof BackpackBlockEntity, "Expected placed backpack block entity");
        var block = (BackpackBlockEntity) helper.getBlockEntity(placed);
        helper.assertTrue(
                block.getItem(0).is(Items.DIAMOND) && block.getItem(0).getCount() == 5,
                "Placement must preserve stored items");
        var menu = new BackpackMenu(1, player.getInventory(), block);
        helper.assertTrue(
                menu.getSlot(0).getItem().getCount() == 5, "Original ground menu must expose backpack contents");
        menu.getSlot(0).set(new ItemStack(Items.DIAMOND, 6));
        menu.removed(player);
        var absolutePlaced = helper.absolutePos(placed);
        var pickupHit = new BlockHitResult(Vec3.atCenterOf(absolutePlaced), Direction.UP, absolutePlaced, false);
        var pickupResult =
                BackpackPickupEvents.onRightClickBlock(player, helper.getLevel(), InteractionHand.MAIN_HAND, pickupHit);
        helper.assertTrue(pickupResult.consumesAction(), "Native sneak-right-click must pick up ground backpack");
        helper.assertTrue(helper.getBlockState(placed).isAir(), "Pickup must remove placed block");
        var pickedUp = BackSlotBackpack.get(player);
        helper.assertTrue(pickedUp.is(BPItems.BACKPACK), "Pickup must return backpack to Back slot");
        var contents = pickedUp.get(DataComponents.CONTAINER);
        helper.assertTrue(
                contents != null
                        && contents.stream().anyMatch(stack -> stack.is(Items.DIAMOND) && stack.getCount() == 6),
                "Ground menu changes must persist after pickup");
        helper.assertTrue(
                pickedUp.get(DataComponents.DYED_COLOR).rgb() == 0x48AACC, "Backpack dye must survive the round trip");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void backpackSnapshotKeepsDyeButNotInventory(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var backpack = filledBackpack();
        Services.PLATFORM.equipBackpack(player, backpack);
        BackSlotBackpack.sync(new PlayerTickEvent.Post(player));
        var snapshot = player.getData(PoptartCoreAttachments.BACKPACK_VISUAL);
        helper.assertTrue(
                snapshot.is(BPItems.BACKPACK) && snapshot != backpack, "Appearance must be an independent snapshot");
        helper.assertTrue(
                snapshot.get(DataComponents.CONTAINER) == null, "Nearby players must not receive stored contents");
        helper.assertTrue(snapshot.get(DataComponents.DYED_COLOR).rgb() == 0x48AACC, "Snapshot must preserve dye");
        helper.assertTrue(backpack.get(DataComponents.CONTAINER) != null, "Snapshot must not strip real storage");
        Services.PLATFORM.unequipBackpack(player);
        BackSlotBackpack.sync(new PlayerTickEvent.Post(player));
        helper.assertTrue(
                player.getData(PoptartCoreAttachments.BACKPACK_VISUAL).isEmpty(),
                "Unequipping must clear worn model snapshot");
        helper.succeed();
    }
}
