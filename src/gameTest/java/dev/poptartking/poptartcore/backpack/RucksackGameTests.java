package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.registry.PoptartEquipmentBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RucksackGameTests {
    private static ItemStack loaded() {
        var stack = new ItemStack(PoptartEquipmentBlocks.RUCKSACK.get());
        BackpackMenu.contents(stack).setItem(53, new ItemStack(Items.DIAMOND, 12));
        return stack;
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void sixRowMenuAndCorpseSnapshotKeepAll54Slots(GameTestHelper helper) {
        var stack = loaded();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().setItem(0, stack);
        var menu = new BackpackMenu(1, player.getInventory(), BackpackMenu.contents(stack), stack);
        helper.assertTrue(
                menu.getRowCount() == 6 && menu.getContainer().getContainerSize() == 54, "Six rows and 54 slots");
        helper.assertTrue(menu.quickMoveStack(player, 81).isEmpty(), "Open rucksack cannot shift-click itself");
        menu.getSlot(53).set(new ItemStack(Items.EMERALD, 9));
        helper.assertTrue(BackpackMenu.contents(stack).getItem(53).getCount() == 9, "Slot 54 edits persist");
        helper.assertTrue(
                !menu.getSlot(0).mayPlace(new ItemStack(PoptartEquipmentBlocks.BACKPACK.get()))
                        && !menu.getSlot(0).mayPlace(loaded()),
                "Neither size can be nested");
        player.getInventory().setItem(0, ItemStack.EMPTY);
        helper.assertTrue(BackpackItem.equip(player, stack), "Rucksack equips in back slot");
        var visual =
                BackpackVisual.renderStack(AccessoryInventory.get(player).getStackInSlot(AccessoryInventory.BACK_SLOT));
        helper.assertTrue(
                visual.is(PoptartEquipmentBlocks.RUCKSACK.get().asItem()) && !visual.has(DataComponents.CONTAINER),
                "Rucksack model snapshot excludes private inventory");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void upperHalfPickupAndBreakingKeepOneLoadedRucksack(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var ground = helper.absolutePos(new BlockPos(4, 1, 4));
        var pos = ground.above();
        player.setPos(Vec3.atCenterOf(pos).add(0, 0, 2));
        player.setShiftKeyDown(true);
        helper.getLevel().setBlockAndUpdate(ground, Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
        BackpackItem.equip(player, loaded());
        var hit = new BlockHitResult(Vec3.atCenterOf(ground).add(0, .5, 0), Direction.UP, ground, false);
        helper.assertTrue(
                !BackpackGestures.place(player, hit), "Low ceiling prevents placement without removing worn pack");
        helper.getLevel().setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        helper.assertTrue(BackpackGestures.place(player, hit), "Place tall rucksack");
        helper.assertTrue(
                helper.getLevel().getBlockState(pos.above()).is(PoptartEquipmentBlocks.RUCKSACK.get()),
                "Upper half exists");
        var entity = (BackpackBlockEntity) helper.getLevel().getBlockEntity(pos);
        helper.assertTrue(
                entity.getContainerSize() == 54 && entity.getItem(53).getCount() == 12,
                "Placed storage contains slot 54");
        var restored = new BackpackBlockEntity(pos, entity.getBlockState());
        restored.loadWithComponents(
                entity.saveWithFullMetadata(helper.getLevel().registryAccess()),
                helper.getLevel().registryAccess());
        helper.assertTrue(restored.getItem(53).getCount() == 12, "54th slot survives block save and load");
        helper.assertTrue(BackpackGestures.pickUp(player, pos.above()), "Upper half can be picked up");
        helper.assertTrue(
                helper.getLevel().getBlockState(pos).isAir()
                        && helper.getLevel().getBlockState(pos.above()).isAir(),
                "Pickup removes both halves");
        helper.assertTrue(BackpackGestures.place(player, hit), "Place again after pickup");
        helper.getLevel().destroyBlock(pos.above(), true);
        var drops = helper.getLevel()
                .getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2), entityItem -> entityItem
                        .getItem()
                        .is(PoptartEquipmentBlocks.RUCKSACK.get().asItem()));
        helper.assertTrue(
                drops.size() == 1
                        && BackpackMenu.contents(drops.getFirst().getItem())
                                        .getItem(53)
                                        .getCount()
                                == 12,
                "Breaking upper half drops exactly one complete loaded rucksack");
        helper.assertTrue(helper.getLevel().getBlockState(pos).isAir(), "Breaking upper half removes lower half");
        drops.forEach(ItemEntity::discard);
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void canceledTallPlacementRestoresBothBlocks(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var ground = helper.absolutePos(new BlockPos(4, 1, 4));
        var pos = ground.above();
        player.setPos(Vec3.atCenterOf(pos).add(0, 0, 2));
        player.setShiftKeyDown(true);
        helper.getLevel().setBlockAndUpdate(ground, Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        BackpackItem.equip(player, loaded());
        java.util.function.Consumer<net.neoforged.neoforge.event.level.BlockEvent.EntityMultiPlaceEvent> cancel =
                event -> {
                    if (event.getPos().equals(pos)) event.setCanceled(true);
                };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(cancel);
        try {
            var hit = new BlockHitResult(Vec3.atCenterOf(ground).add(0, .5, 0), Direction.UP, ground, false);
            helper.assertTrue(!BackpackGestures.place(player, hit), "Protected placement canceled");
            helper.assertTrue(
                    helper.getLevel().getBlockState(pos).isAir()
                            && helper.getLevel().getBlockState(pos.above()).isAir(),
                    "Both snapshots restored");
            helper.assertTrue(
                    BackpackMenu.contents(AccessoryInventory.get(player).getStackInSlot(AccessoryInventory.BACK_SLOT))
                                    .getItem(53)
                                    .getCount()
                            == 12,
                    "Cancellation preserves worn contents");
        } finally {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(cancel);
        }
        helper.succeed();
    }
}
