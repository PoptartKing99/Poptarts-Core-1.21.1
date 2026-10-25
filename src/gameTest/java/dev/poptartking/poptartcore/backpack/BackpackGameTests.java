package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BackpackGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void corpseBackpackVisualFollowsLootWithoutCopyingStorage(GameTestHelper helper) {
        int base = com.farcr.ragdoll.api.inventory.CorpseSlots.baseSlot(
                dev.poptartking.poptartcore.integration.ragdoll.AccessoryCorpseProvider.ID);
        helper.assertTrue(base >= 0, "Accessory corpse slots registered");
        int index = base + AccessoryInventory.BACK_SLOT;
        var items = new java.util.ArrayList<ItemStack>(java.util.Collections.nCopies(index + 1, ItemStack.EMPTY));
        var original = loaded();
        items.set(index, original);
        var visual = BackpackVisual.corpseVisualStack(items);
        helper.assertTrue(
                visual.is(PoptartCoreBlocks.BACKPACK.get().asItem())
                        && visual.getHoverName().getString().equals("Trail pack"),
                "Corpse snapshot keeps backpack identity and name");
        helper.assertTrue(
                visual != original
                        && !visual.has(DataComponents.CONTAINER)
                        && !visual.has(DataComponents.BLOCK_ENTITY_DATA),
                "Visual snapshot contains no private storage");
        helper.assertTrue(
                BackpackMenu.contents(original).getItem(26).getCount() == 12,
                "Corpse inventory keeps original stored items");
        items.set(index, ItemStack.EMPTY);
        helper.assertTrue(BackpackVisual.corpseVisualStack(items).isEmpty(), "Looting backpack clears its visual");
        items.set(index, new ItemStack(Items.DIRT));
        helper.assertTrue(
                BackpackVisual.corpseVisualStack(items).isEmpty(), "Unrelated back items cannot render basic backpack");
        helper.assertTrue(
                BackpackVisual.corpseVisualStack(java.util.List.of()).isEmpty(),
                "Incomplete corpse inventory has no backpack visual");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void placementFacesPlayerAndPickupDoesNotSwing(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var ground = helper.absolutePos(new BlockPos(4, 1, 4));
        var pos = ground.above();
        player.setShiftKeyDown(true);
        helper.getLevel().setBlockAndUpdate(ground, Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        for (var direction : Direction.Plane.HORIZONTAL) {
            player.setPos(Vec3.atCenterOf(pos).add(-direction.getStepX() * 2, 0, -direction.getStepZ() * 2));
            player.setYRot(direction.toYRot());
            BackpackItem.equip(player, loaded());
            var hit = new BlockHitResult(Vec3.atCenterOf(ground).add(0, .5, 0), Direction.UP, ground, false);
            helper.assertTrue(BackpackGestures.place(player, hit), "Place pack while looking " + direction);
            var state = helper.getLevel().getBlockState(pos);
            helper.assertTrue(
                    state.getValue(BackpackBlock.FACING) == direction.getOpposite(),
                    "Front faces player for " + direction);
            var result = state.useWithoutItem(
                    helper.getLevel(),
                    player,
                    new BlockHitResult(Vec3.atCenterOf(pos), direction.getOpposite(), pos, false));
            helper.assertTrue(
                    result.consumesAction() && !result.shouldSwing(),
                    "Pickup interaction consumes use without arm swing");
            helper.assertTrue(BackpackGestures.pickUp(player, pos), "Pick up for next facing test");
            AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, ItemStack.EMPTY);
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void invalidAndCanceledGesturesDoNotLoseOrDuplicateStorage(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var ground = helper.absolutePos(new BlockPos(4, 1, 4));
        var pos = ground.above();
        player.setPos(Vec3.atCenterOf(pos).add(0, 0, 2));
        player.setShiftKeyDown(true);
        helper.getLevel().setBlockAndUpdate(ground, Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        BackpackItem.equip(player, loaded());
        var hit = new BlockHitResult(Vec3.atCenterOf(ground).add(0, .5, 0), Direction.UP, ground, false);
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TORCH));
        helper.assertTrue(!BackpackGestures.place(player, hit), "Occupied offhand prevents placement");
        player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        java.util.function.Consumer<net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent> denyPlace =
                event -> {
                    if (event.getPos().equals(pos)) event.setCanceled(true);
                };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(denyPlace);
        try {
            helper.assertTrue(!BackpackGestures.place(player, hit), "Canceled placement rejected");
            helper.assertTrue(
                    helper.getLevel().getBlockState(pos).isAir(), "Canceled placement restores original block");
            helper.assertTrue(
                    BackpackMenu.contents(AccessoryInventory.get(player).getStackInSlot(AccessoryInventory.BACK_SLOT))
                                    .getItem(26)
                                    .getCount()
                            == 12,
                    "Canceled placement leaves contents in worn pack");
        } finally {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(denyPlace);
        }
        helper.assertTrue(BackpackGestures.place(player, hit), "Valid placement succeeds after cancellation removed");
        java.util.function.Consumer<net.neoforged.neoforge.event.level.BlockEvent.BreakEvent> denyPickup = event -> {
            if (event.getPos().equals(pos)) event.setCanceled(true);
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(denyPickup);
        try {
            helper.assertTrue(!BackpackGestures.pickUp(player, pos), "Canceled pickup rejected");
            helper.assertTrue(
                    helper.getLevel().getBlockEntity(pos) instanceof BackpackBlockEntity
                            && AccessoryInventory.get(player)
                                    .getStackInSlot(AccessoryInventory.BACK_SLOT)
                                    .isEmpty(),
                    "Canceled pickup leaves original pack in world");
        } finally {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(denyPickup);
        }
        player.setPos(Vec3.atCenterOf(pos).add(20, 0, 0));
        helper.assertTrue(!BackpackGestures.pickUp(player, pos), "Out of reach pickup rejected");
        player.setPos(Vec3.atCenterOf(pos).add(0, 0, 2));
        helper.assertTrue(BackpackGestures.pickUp(player, pos), "Pack remains recoverable after rejected gestures");
        helper.succeed();
    }

    private static ItemStack loaded() {
        var stack = new ItemStack(PoptartCoreBlocks.BACKPACK.get());
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Trail pack"));
        BackpackMenu.contents(stack).setItem(26, new ItemStack(Items.DIAMOND, 12));
        return stack;
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void storageAndBackSlotPreserveArmorAndContents(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = loaded();
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        helper.assertTrue(BackpackItem.equip(player, stack), "Equip backpack in empty back slot");
        var equipped = AccessoryInventory.get(player).getStackInSlot(AccessoryInventory.BACK_SLOT);
        helper.assertTrue(stack.isEmpty() && equipped.getCount() == 1, "Move exactly one pack");
        helper.assertTrue(
                BackpackMenu.contents(equipped).getItem(26).getCount() == 12, "Last of 27 slots survives equip");
        helper.assertTrue(
                player.getItemBySlot(EquipmentSlot.CHEST).is(Items.IRON_CHESTPLATE), "Chest armor remains equipped");
        var second = loaded();
        helper.assertTrue(
                !BackpackItem.equip(player, second) && second.getCount() == 1,
                "Occupied back slot cannot be overwritten");
        helper.assertTrue(
                !BackpackMenu.canStore(second) && !BackpackMenu.canStore(new ItemStack(Items.SHULKER_BOX)),
                "No nested container-unsafe items");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void heldMenuLocksItsSourceAndPersistsEdits(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = loaded();
        player.getInventory().setItem(0, stack);
        var menu = new BackpackMenu(1, player.getInventory(), BackpackMenu.contents(stack), stack);
        menu.getSlot(0).set(new ItemStack(Items.EMERALD, 7));
        helper.assertTrue(BackpackMenu.contents(stack).getItem(0).getCount() == 7, "Menu edits write to item");
        helper.assertTrue(!menu.getSlot(0).mayPlace(loaded()), "Menu rejects nested backpack");
        helper.assertTrue(menu.quickMoveStack(player, 54).isEmpty(), "Shift-click cannot move open pack");
        menu.clicked(54, 0, ClickType.THROW, player);
        helper.assertTrue(player.getInventory().getItem(0) == stack, "Throw cannot remove source pack");
        player.getInventory().setItem(0, ItemStack.EMPTY);
        helper.assertTrue(!menu.stillValid(player), "Removing source invalidates menu");
        player.getInventory().setItem(0, stack);
        stack.setCount(0);
        helper.assertTrue(!menu.stillValid(player), "An emptied source cannot become an unlimited detached container");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void wornPlacementPickupAndLootPreserveContents(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var ground = helper.absolutePos(new BlockPos(4, 1, 4));
        var pos = ground.above();
        player.setPos(Vec3.atCenterOf(pos).add(0, 0, 2));
        player.setShiftKeyDown(true);
        helper.getLevel().setBlockAndUpdate(ground, Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        var stack = loaded();
        helper.assertTrue(BackpackItem.equip(player, stack), "Equip test backpack");
        var hit = new BlockHitResult(Vec3.atCenterOf(ground).add(0, .5, 0), Direction.UP, ground, false);
        helper.assertTrue(BackpackGestures.place(player, hit), "Place directly from back slot");
        helper.assertTrue(
                AccessoryInventory.get(player)
                        .getStackInSlot(AccessoryInventory.BACK_SLOT)
                        .isEmpty(),
                "Back slot cleared after placement");
        var entity = (BackpackBlockEntity) helper.getLevel().getBlockEntity(pos);
        helper.assertTrue(
                entity != null && entity.getItem(26).getCount() == 12, "Placed pack receives stored contents");
        var saved = entity.saveWithFullMetadata(helper.getLevel().registryAccess());
        var restored = new BackpackBlockEntity(pos, entity.getBlockState());
        restored.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.assertTrue(restored.getItem(26).getCount() == 12, "Block storage survives save/load");
        var drops = Block.getDrops(entity.getBlockState(), helper.getLevel(), pos, entity);
        helper.assertTrue(
                drops.size() == 1
                        && BackpackMenu.contents(drops.getFirst()).getItem(26).getCount() == 12,
                "Breaking drops one loaded backpack, without spilled duplicates");
        helper.assertTrue(BackpackGestures.pickUp(player, pos), "Pick up directly into back slot");
        var worn = AccessoryInventory.get(player).getStackInSlot(AccessoryInventory.BACK_SLOT);
        helper.assertTrue(
                BackpackMenu.contents(worn).getItem(26).getCount() == 12
                        && worn.getHoverName().getString().equals("Trail pack"),
                "Pickup keeps name and contents");
        helper.assertTrue(helper.getLevel().getBlockState(pos).isAir(), "Pickup removes placed block");
        helper.assertTrue(!BackpackGestures.pickUp(player, pos), "Repeated pickup cannot duplicate pack");
        helper.succeed();
    }
}
