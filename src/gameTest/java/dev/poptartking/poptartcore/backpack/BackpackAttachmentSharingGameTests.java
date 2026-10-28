package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartEquipmentBlocks;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BackpackAttachmentSharingGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void concurrentMenusCannotRemoveTheSameAttachment(GameTestHelper helper) {
        for (boolean rucksack : new boolean[] {false, true}) {
            var entity = placedBag(helper, rucksack);
            var stack = new ItemStack(entity.getBlockState().getBlock());
            BackpackAttachments.set(stack, 1, new ItemStack(Items.SPYGLASS));
            entity.applyComponentsFromItemStack(stack);
            var first = helper.makeMockPlayer(GameType.SURVIVAL);
            var second = helper.makeMockPlayer(GameType.SURVIVAL);
            var pos = entity.getBlockPos();
            first.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + 1);
            second.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + 1);
            var firstMenu = new BackpackMenu(1, first.getInventory(), entity, ItemStack.EMPTY);
            var secondMenu = new BackpackMenu(2, second.getInventory(), entity, ItemStack.EMPTY);
            int mount = entity.getContainerSize() + 36 + 1;
            var changes = new ArrayList<ItemStack>();
            secondMenu.addSlotListener(new ContainerListener() {
                @Override
                public void slotChanged(AbstractContainerMenu menu, int slot, ItemStack item) {
                    if (slot == mount) changes.add(item.copy());
                }

                @Override
                public void dataChanged(AbstractContainerMenu menu, int slot, int value) {}
            });
            changes.clear();

            firstMenu.clicked(mount, 0, ClickType.PICKUP, first);
            helper.assertTrue(firstMenu.getCarried().is(Items.SPYGLASS), "First viewer takes the attachment");
            secondMenu.broadcastChanges();
            helper.assertTrue(
                    !changes.isEmpty() && changes.getLast().isEmpty(),
                    "Other viewer receives the empty attachment slot");
            secondMenu.clicked(mount, 0, ClickType.PICKUP, second);
            helper.assertTrue(secondMenu.getCarried().isEmpty(), "Second viewer cannot take a duplicate");
            helper.assertTrue(
                    BackpackAttachments.items(entity.attachments()).get(1).isEmpty(),
                    "Removal leaves the placed bag empty");

            firstMenu.clicked(mount, 0, ClickType.PICKUP, first);
            helper.assertTrue(secondMenu.getSlot(mount).hasItem(), "Returning the attachment updates both menus");
            secondMenu.quickMoveStack(second, mount);
            helper.assertTrue(
                    second.getInventory().countItem(Items.SPYGLASS) == 1
                            && firstMenu.getSlot(mount).getItem().isEmpty()
                            && firstMenu.quickMoveStack(first, mount).isEmpty(),
                    "Shift click transfers the single attachment only once");
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void concurrentAttachmentEditsPreserveBothPlayersChanges(GameTestHelper helper) {
        for (boolean rucksack : new boolean[] {false, true}) {
            var entity = placedBag(helper, rucksack);
            entity.setItem(entity.getContainerSize() - 1, new ItemStack(Items.DIAMOND, 9));
            var first = helper.makeMockPlayer(GameType.SURVIVAL);
            var second = helper.makeMockPlayer(GameType.SURVIVAL);
            var pos = entity.getBlockPos();
            first.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + 1);
            second.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + 1);
            var firstMenu = new BackpackMenu(1, first.getInventory(), entity, ItemStack.EMPTY);
            var secondMenu = new BackpackMenu(2, second.getInventory(), entity, ItemStack.EMPTY);
            int mount = entity.getContainerSize() + 36 + 1;

            firstMenu.setCarried(new ItemStack(Items.SPYGLASS));
            firstMenu.clicked(mount, 0, ClickType.PICKUP, first);
            secondMenu.setCarried(new ItemStack(Items.CLOCK));
            secondMenu.clicked(mount + 1, 0, ClickType.PICKUP, second);
            helper.assertTrue(
                    firstMenu.getCarried().isEmpty() && secondMenu.getCarried().isEmpty(),
                    "Both viewers install their attachments");
            helper.assertTrue(
                    firstMenu.getSlot(mount + 1).getItem().is(Items.CLOCK)
                            && secondMenu.getSlot(mount).getItem().is(Items.SPYGLASS),
                    "Each viewer sees the other viewer's edit");

            var registries = helper.getLevel().registryAccess();
            var loaded = new BackpackBlockEntity(pos, entity.getBlockState());
            loaded.loadWithComponents(entity.saveWithFullMetadata(registries), registries);
            var drop = Block.getDrops(entity.getBlockState(), helper.getLevel(), pos, entity)
                    .getFirst();
            helper.assertTrue(
                    BackpackAttachments.items(loaded.attachments()).get(1).is(Items.SPYGLASS)
                            && BackpackAttachments.items(loaded.attachments())
                                    .get(2)
                                    .is(Items.CLOCK)
                            && BackpackAttachments.get(drop, 1).is(Items.SPYGLASS)
                            && BackpackAttachments.get(drop, 2).is(Items.CLOCK),
                    "Both edits survive saving and block drops");
            helper.assertTrue(
                    loaded.getItem(entity.getContainerSize() - 1).getCount() == 9,
                    "Attachment edits preserve ordinary storage");
        }
        helper.succeed();
    }

    private static BackpackBlockEntity placedBag(GameTestHelper helper, boolean rucksack) {
        var pos = helper.absolutePos(new BlockPos(4, 2, 4));
        var block = rucksack ? PoptartEquipmentBlocks.RUCKSACK.get() : PoptartEquipmentBlocks.BACKPACK.get();
        helper.getLevel().removeBlock(pos, false);
        helper.getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
        return (BackpackBlockEntity) helper.getLevel().getBlockEntity(pos);
    }
}
