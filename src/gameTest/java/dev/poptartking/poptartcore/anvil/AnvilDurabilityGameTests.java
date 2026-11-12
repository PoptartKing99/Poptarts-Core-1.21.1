package dev.poptartking.poptartcore.anvil;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.AnvilComponents;
import dev.poptartking.poptartcore.registry.PoptartToolItems;
import io.netty.buffer.Unpooled;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AnvilDurabilityGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void menuUpgradesPreserveCustomDurabilityAndWear(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = new AnvilMenu(1, player.getInventory());
        menu.getSlot(2).set(new ItemStack(PoptartToolItems.HAMMER.get()));
        var sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.MAX_DAMAGE, 1000);
        sword.setDamageValue(250);
        int[] maximums = {1000, 1200, 1400, 1600};
        int[] damage = {250, 300, 350, 400};
        for (int level = 1; level <= 3; level++) {
            menu.getSlot(0).set(sword);
            menu.getSlot(1).set(new ItemStack(Items.IRON_INGOT, level + 3));
            var result = menu.getSlot(3).getItem().copy();
            helper.assertTrue(
                    result.getMaxDamage() == maximums[level] && result.getDamageValue() == damage[level],
                    "Menu applies each upgrade to custom starting durability and preserves wear percentage");
            helper.assertTrue(
                    sword.getMaxDamage() == maximums[level - 1] && sword.getDamageValue() == damage[level - 1],
                    "Preview does not modify the input item");
            menu.onTake(player, result);
            helper.assertTrue(menu.getSlot(1).getItem().isEmpty(), "Original upgrade material cost is preserved");
            AnvilMenu.applyUpgrades(result, level);
            helper.assertTrue(result.getMaxDamage() == maximums[level], "Reapplying a level does not compound it");
            sword = result;
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void armorDurabilityReferenceSurvivesSavingAndNetworking(GameTestHelper helper) {
        var armor = new ItemStack(Items.IRON_CHESTPLATE);
        armor.set(DataComponents.MAX_DAMAGE, 2000);
        armor.setDamageValue(1000);
        AnvilMenu.applyUpgrades(armor, 3);
        helper.assertTrue(
                armor.getMaxDamage() == 2900 && armor.getDamageValue() == 1450, "Armor retains 15 percent per level");
        var registries = helper.getLevel().registryAccess();
        var restored = ItemStack.parseOptional(registries, (CompoundTag) armor.save(registries));
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            ItemStack.STREAM_CODEC.encode(buffer, restored);
            restored = ItemStack.STREAM_CODEC.decode(buffer);
        } finally {
            buffer.release();
        }
        AnvilMenu.applyUpgrades(restored, 1);
        helper.assertTrue(
                restored.getMaxDamage() == 2300 && restored.getDamageValue() == 1150,
                "Saved and synchronized items keep the original durability reference");
        AnvilMenu.applyUpgrades(restored, 0);
        helper.assertTrue(
                !AnvilMenu.isUpgraded(restored)
                        && restored.isDamageableItem()
                        && restored.getMaxDamage() == 2000
                        && restored.getDamageValue() == 1000,
                "Removing upgrades restores custom durability and preserves wear");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void oldVanillaUpgradesKeepTheirOriginalValues(GameTestHelper helper) {
        for (boolean legacy : new boolean[] {false, true}) {
            var pickaxe = oldUpgradedItem(legacy, 71, Items.WOODEN_PICKAXE);
            AnvilMenu.applyUpgrades(pickaxe, 1);
            helper.assertTrue(pickaxe.getMaxDamage() == 71, "Existing first upgrade does not gain durability twice");
            AnvilMenu.applyUpgrades(pickaxe, 3);
            helper.assertTrue(pickaxe.getMaxDamage() == 94, "Existing wooden tools keep the original rounding");
            AnvilMenu.applyUpgrades(pickaxe, 0);
            helper.assertTrue(
                    pickaxe.isDamageableItem() && pickaxe.getMaxDamage() == 59,
                    "Removing upgrades restores the normal item maximum");
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void oldCustomUpgradesKeepCurrentDurability(GameTestHelper helper) {
        for (boolean legacy : new boolean[] {false, true}) {
            var sword = oldUpgradedItem(legacy, 1000, Items.IRON_SWORD);
            AnvilMenu.applyUpgrades(sword, 1);
            helper.assertTrue(sword.getMaxDamage() == 1000, "Existing custom maximum is preserved during migration");
            AnvilMenu.applyUpgrades(sword, 3);
            helper.assertTrue(sword.getMaxDamage() == 1333, "Later upgrades add only the remaining upgrade bonus");
            AnvilMenu.applyUpgrades(sword, 1);
            helper.assertTrue(sword.getMaxDamage() == 1000, "The recorded reference survives changing levels");
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void laterCustomDurabilityChangesAreNotOverwritten(GameTestHelper helper) {
        var sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.MAX_DAMAGE, 1000);
        AnvilMenu.applyUpgrades(sword, 1);
        sword.set(DataComponents.MAX_DAMAGE, 1800);
        AnvilMenu.applyUpgrades(sword, 1);
        helper.assertTrue(sword.getMaxDamage() == 1800, "Later changes from another mod become the new reference");
        AnvilMenu.applyUpgrades(sword, 3);
        helper.assertTrue(sword.getMaxDamage() == 2400, "Later upgrades preserve the additional custom durability");
        sword.set(DataComponents.MAX_DAMAGE, 250);
        AnvilMenu.applyUpgrades(sword, 3);
        helper.assertTrue(
                sword.getMaxDamage() == 250, "An external change matching the item default is still preserved");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void veryLargeCustomDurabilityDoesNotOverflow(GameTestHelper helper) {
        var sword = new ItemStack(Items.IRON_SWORD);
        sword.set(DataComponents.MAX_DAMAGE, 1_500_000_000);
        AnvilMenu.applyUpgrades(sword, 3);
        helper.assertTrue(
                sword.getMaxDamage() == Integer.MAX_VALUE, "Maximum durability saturates instead of wrapping");
        helper.succeed();
    }

    private static ItemStack oldUpgradedItem(boolean legacy, int maximum, net.minecraft.world.item.Item item) {
        var stack = new ItemStack(item);
        stack.set(DataComponents.MAX_DAMAGE, maximum);
        var data = new CompoundTag();
        data.putBoolean("poptartcore:separate_upgrade_bonuses", true);
        if (legacy) data.putInt("poptartcore:upgrade_count", 1);
        else stack.set(AnvilComponents.UPGRADE_COUNT.get(), 1);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }
}
