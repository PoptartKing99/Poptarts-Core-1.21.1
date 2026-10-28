package dev.poptartking.poptartcore.catalog;

import com.mojang.authlib.GameProfile;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartWorkstationBlocks;
import dev.poptartking.poptartcore.workbench.WorkbenchBlockEntity;
import dev.poptartking.poptartcore.workbench.menu.WorkbenchMenu;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AutomationGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void crucibleCapabilitiesKeepTheirSidedRouting(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlockAndUpdate(pos, PoptartWorkstationBlocks.CRUCIBLE.get().defaultBlockState());
        helper.assertTrue(
                level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null) == null,
                "Undirected access must not bypass routing");
        var top = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
        var bottom = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.DOWN);
        helper.assertTrue(top != null && top.getSlots() == 3, "Top exposes three ingredient slots");
        helper.assertTrue(bottom != null && bottom.getSlots() == 1, "Bottom exposes only the result slot");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void quernAutomationProtectsInputAndRespectsSimulation(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 1, 1));
        level.setBlockAndUpdate(pos, PoptartWorkstationBlocks.QUERN.get().defaultBlockState());
        var inventory = (Container) level.getBlockEntity(pos);
        var handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.UP);
        helper.assertTrue(handler != null && handler.getSlots() == 2, "Quern capability is registered");
        helper.assertTrue(
                handler.insertItem(0, new ItemStack(Items.WHEAT, 3), true).isEmpty()
                        && inventory.getItem(0).isEmpty(),
                "Simulated insertion must not mutate input");
        handler.insertItem(0, new ItemStack(Items.WHEAT, 3), false);
        helper.assertTrue(
                handler.extractItem(0, 1, true).isEmpty()
                        && handler.extractItem(0, 1, false).isEmpty(),
                "Input cannot be extracted");
        inventory.setItem(1, new ItemStack(Items.BONE_MEAL, 3));
        helper.assertTrue(
                handler.extractItem(1, 2, true).getCount() == 2
                        && inventory.getItem(1).getCount() == 3,
                "Output simulation preserves inventory");
        helper.assertTrue(
                handler.extractItem(1, 2, false).getCount() == 2
                        && inventory.getItem(1).getCount() == 1,
                "Actual output extraction removes exactly the requested count");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void workbenchStoragePersistsAndAutomationExcludesComputedOutput(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var state = PoptartWorkstationBlocks.WORKBENCH.get().defaultBlockState();
        level.setBlockAndUpdate(pos, state);
        var bench = (WorkbenchBlockEntity) level.getBlockEntity(pos);
        var handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        helper.assertTrue(
                bench.getContainerSize() == 25 && handler != null && handler.getSlots() == 25,
                "Nine grid and sixteen storage slots, without computed output");
        for (int slot = 0; slot < 25; slot++) bench.setItem(slot, new ItemStack(Items.STICK, slot + 1));
        var restored = new WorkbenchBlockEntity(pos, state);
        restored.loadWithComponents(bench.saveWithFullMetadata(level.registryAccess()), level.registryAccess());
        for (int slot = 0; slot < 25; slot++)
            helper.assertTrue(
                    restored.getItem(slot).is(Items.STICK)
                            && restored.getItem(slot).getCount() == slot + 1,
                    "Grid/storage slot survives saving: " + slot);
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void workbenchRefreshesAndConsumesRealCraftingInputs(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "bench-test"));
        player.setGameMode(GameType.SURVIVAL);
        var storage = new net.minecraft.world.SimpleContainer(25);
        var menu = new WorkbenchMenu(1, player.getInventory(), storage);
        player.containerMenu = menu;
        menu.getSlot(1).set(new ItemStack(Items.OAK_PLANKS));
        menu.getSlot(4).set(new ItemStack(Items.OAK_PLANKS));
        helper.assertTrue(
                menu.getSlot(0).getItem().is(Items.STICK)
                        && menu.getSlot(0).getItem().getCount() == 4,
                "Changing the grid refreshes a vanilla recipe");
        menu.clicked(0, 0, ClickType.PICKUP, player);
        helper.assertTrue(
                menu.getCarried().is(Items.STICK) && menu.getCarried().getCount() == 4,
                "Player receives crafted output");
        helper.assertTrue(
                storage.getItem(0).isEmpty()
                        && storage.getItem(3).isEmpty()
                        && menu.getSlot(0).getItem().isEmpty(),
                "Crafting consumes both inputs and clears the result");
        helper.succeed();
    }
}
