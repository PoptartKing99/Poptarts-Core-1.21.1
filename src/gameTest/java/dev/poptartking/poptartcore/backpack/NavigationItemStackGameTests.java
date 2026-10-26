package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NavigationItemStackGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void navigationItemsStaySeparateAndLegacyCountsSurviveLoading(GameTestHelper helper) {
        for (var item : java.util.List.of(
                Items.COMPASS,
                Items.CLOCK,
                Items.RECOVERY_COMPASS,
                dev.poptartking.poptartcore.registry.PoptartToolItems.DEPTH_METER.get())) {
            var stack = new ItemStack(item);
            helper.assertTrue(stack.getMaxStackSize() == 1 && !stack.isStackable(), "Navigation item cannot stack");
            var inventory = new SimpleContainer(2);
            inventory.addItem(new ItemStack(item));
            inventory.addItem(new ItemStack(item));
            helper.assertTrue(
                    inventory.getItem(0).getCount() == 1 && inventory.getItem(1).getCount() == 1,
                    "Separate items occupy separate slots");
            var legacy = new ItemStack(item, 16);
            var saved = (CompoundTag) legacy.save(helper.getLevel().registryAccess());
            var restored = ItemStack.parseOptional(helper.getLevel().registryAccess(), saved);
            helper.assertTrue(
                    restored.getCount() == 16 && restored.getMaxStackSize() == 1,
                    "Existing oversized stacks retain their items when loaded");
        }
        helper.assertTrue(new ItemStack(Items.APPLE).getMaxStackSize() == 64, "Other item limits are unchanged");
        helper.succeed();
    }
}
