package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartMaterialItems;
import dev.poptartking.poptartcore.registry.PoptartRingItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RingGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void ringEquipRepairAndWear(GameTestHelper helper) {
        Item[] rings = {
            PoptartRingItems.DULL_IRON_RING.get(),
            PoptartRingItems.MOONLIGHT_RING.get(),
            PoptartRingItems.RING_OF_WARMTH.get(),
            PoptartRingItems.FIREWARD_RING.get(),
            PoptartRingItems.SILVERCAT_RING.get()
        };
        Item[] repairs = {
            Items.IRON_INGOT,
            PoptartMaterialItems.SILVER_INGOT.get(),
            PoptartMaterialItems.BRONZE_INGOT.get(),
            Items.GOLD_INGOT,
            PoptartMaterialItems.SILVER_INGOT.get()
        };
        int[] durability = {327, 327, 527, 927, 527};
        for (int i = 0; i < rings.length; i++) {
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            var handler = new PlayerAccessoryHandler(player);
            var stack = new ItemStack(rings[i]);
            helper.assertTrue(
                    stack.getMaxDamage() == durability[i] && stack.getMaxStackSize() == 1,
                    "Ring must match Wayfarer durability and be unstackable");
            helper.assertTrue(
                    rings[i].isValidRepairItem(stack, new ItemStack(repairs[i])), "Correct repair material required");
            helper.assertTrue(
                    !rings[i].isValidRepairItem(stack, new ItemStack(Items.DIRT)), "Invalid repair must be rejected");
            player.getInventory().setItem(9, stack);
            player.inventoryMenu.quickMoveStack(player, 9);
            helper.assertTrue(
                    AccessoryInventory.get(player).getStackInSlot(0).is(rings[i]), "Shift-click must equip each ring");
            helper.assertTrue(!handler.isItemValid(1, new ItemStack(rings[i])), "Duplicate must be rejected");
            helper.assertTrue(
                    handler.isItemValid(1, new ItemStack(PoptartRingItems.RUSTED_IRON_RING.get())),
                    "Different rings may coexist");
            var worn = handler.getStackInSlot(0);
            worn.setDamageValue(worn.getMaxDamage() - 1);
            EquipmentRing.hurt(player, player.damageSources().generic(), 4);
            helper.assertTrue(handler.getStackInSlot(0).isEmpty(), "Broken ring must unequip");
            var item = rings[i];
            var recovered = player.getInventory().items.stream()
                    .filter(s -> s.is(item) && s.getDamageValue() == s.getMaxDamage())
                    .findFirst()
                    .orElse(ItemStack.EMPTY);
            helper.assertTrue(
                    !recovered.isEmpty() && !handler.isItemValid(0, recovered),
                    "Broken ring must be preserved but not wearable");
            recovered.setDamageValue(0);
            helper.assertTrue(handler.isItemValid(1, recovered), "Repair restores equip eligibility in either slot");
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void toughnessAndHealthBonuses(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var handler = new PlayerAccessoryHandler(player);
        double toughness = player.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float health = player.getMaxHealth();
        handler.insertItem(0, new ItemStack(PoptartRingItems.DULL_IRON_RING.get()), false);
        handler.insertItem(1, new ItemStack(PoptartRingItems.MOONLIGHT_RING.get()), false);
        helper.assertTrue(
                player.getAttributeValue(Attributes.ARMOR_TOUGHNESS) == toughness + 3,
                "Dull Iron grants three toughness");
        helper.assertTrue(player.getMaxHealth() == health + 4, "Moonlight grants two hearts");
        player.setHealth(player.getMaxHealth());
        EquipmentRing.reconcile(player);
        helper.assertTrue(player.getMaxHealth() == health + 4, "Repeated reconciliation cannot stack bonuses");
        handler.extractItem(1, 1, false);
        helper.assertTrue(
                player.getMaxHealth() == health && player.getHealth() == health,
                "Removing Moonlight restores max health and clamps excess health");
        handler.extractItem(0, 1, false);
        helper.assertTrue(
                player.getAttributeValue(Attributes.ARMOR_TOUGHNESS) == toughness,
                "Removing Dull Iron removes toughness");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void firewardReducesOnlyFireDamage(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var handler = new PlayerAccessoryHandler(player);
        handler.setStackInSlot(0, new ItemStack(PoptartRingItems.FIREWARD_RING.get()));
        player.setHealth(20);
        player.hurt(player.damageSources().onFire(), 10);
        helper.assertTrue(
                Math.abs(player.getHealth() - 11.5F) < .001F, "Fireward must reduce actual fire damage by 15 percent");
        player.invulnerableTime = 0;
        player.setHealth(20);
        player.hurt(player.damageSources().generic(), 10);
        helper.assertTrue(player.getHealth() == 10, "Nonfire damage must be unchanged");
        handler.setStackInSlot(0, ItemStack.EMPTY);
        player.invulnerableTime = 0;
        player.setHealth(20);
        player.hurt(player.damageSources().onFire(), 10);
        helper.assertTrue(player.getHealth() == 10, "Unequipping must stop fire protection");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void silvercatHalvesOnlyNonfatalFalls(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        new PlayerAccessoryHandler(player).setStackInSlot(1, new ItemStack(PoptartRingItems.SILVERCAT_RING.get()));
        player.setHealth(20);
        player.hurt(player.damageSources().fall(), 8);
        helper.assertTrue(player.getHealth() == 16, "Nonfatal fall damage must be halved");
        player.invulnerableTime = 0;
        player.setHealth(20);
        player.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(4);
        player.setAbsorptionAmount(4);
        helper.assertTrue(player.getAbsorptionAmount() == 4, "Test player must have four absorption points");
        player.hurt(player.damageSources().fall(), 22);
        helper.assertTrue(player.getHealth() == 13, "Survivability must include absorption hearts");
        player.invulnerableTime = 0;
        player.setHealth(20);
        player.setAbsorptionAmount(0);
        player.hurt(player.damageSources().fall(), 20);
        helper.assertTrue(!player.isAlive(), "Exactly fatal fall must not be reduced");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void warmthReducesActualExhaustion(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var handler = new PlayerAccessoryHandler(player);
        handler.setStackInSlot(1, new ItemStack(PoptartRingItems.RING_OF_WARMTH.get()));
        player.causeFoodExhaustion(1);
        helper.assertTrue(
                Math.abs(player.getFoodData().getExhaustionLevel() - .85F) < .001F,
                "Warmth must reduce exhaustion by 15 percent through actual Player method");
        handler.setStackInSlot(1, ItemStack.EMPTY);
        player.causeFoodExhaustion(1);
        helper.assertTrue(
                Math.abs(player.getFoodData().getExhaustionLevel() - 1.85F) < .001F,
                "Unequipping must restore normal exhaustion");
        helper.succeed();
    }
}
