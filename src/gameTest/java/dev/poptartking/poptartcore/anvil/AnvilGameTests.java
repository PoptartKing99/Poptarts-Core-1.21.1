package dev.poptartking.poptartcore.anvil;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import dev.poptartking.poptartcore.registry.PoptartToolItems;
import dev.poptartking.poptartcore.registry.PoptartWorkstationBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AnvilGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void armorBonusesUpgradePersistenceAndRepairRestoreFunction(GameTestHelper helper) {
        var armor = new ItemStack(Items.IRON_CHESTPLATE);
        helper.assertTrue(armor.is(PoptartCoreTags.UPGRADEABLE), "Vanilla armor qualifies");
        int base = armor.getMaxDamage();
        AnvilMenu.applyUpgrades(armor, 3);
        helper.assertTrue(
                armor.getMaxDamage() == Math.round(base * 1.45), "Armor durability gains 15 percent per level");
        var modifiers = armor.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS)
                .modifiers();
        helper.assertTrue(
                modifiers.stream()
                                .filter(e ->
                                        e.attribute().equals(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR))
                                .mapToDouble(e -> e.modifier().amount())
                                .sum()
                        == 9,
                "Iron chestplate receives +3 armor: " + modifiers);
        helper.assertTrue(
                modifiers.stream()
                        .anyMatch(e -> e.attribute()
                                        .equals(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS)
                                && e.modifier().amount() == 1),
                "Third upgrade adds toughness");
        var restored = ItemStack.parseOptional(helper.getLevel().registryAccess(), (net.minecraft.nbt.CompoundTag)
                armor.save(helper.getLevel().registryAccess()));
        helper.assertTrue(
                AnvilMenu.getUpgradeCount(restored) == 3 && restored.getMaxDamage() == armor.getMaxDamage(),
                "Upgrades survive item serialization");
        restored.setDamageValue(restored.getMaxDamage());
        var applied = new java.util.concurrent.atomic.AtomicInteger();
        restored.forEachModifier(
                net.minecraft.world.entity.EquipmentSlot.CHEST, (attribute, modifier) -> applied.incrementAndGet());
        helper.assertTrue(applied.get() == 0, "Worn armor grants no attributes");
        restored.setDamageValue(0);
        restored.forEachModifier(
                net.minecraft.world.entity.EquipmentSlot.CHEST, (attribute, modifier) -> applied.incrementAndGet());
        helper.assertTrue(applied.get() > 0, "Repair restores attributes");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void storedHammerSurvivesSavingAndIronRepair(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = menu(helper, player, false);
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        var hammer = new ItemStack(PoptartToolItems.HAMMER.get());
        hammer.setDamageValue(7);
        menu.getSlot(2).set(hammer);
        var entity = (AnvilBlockEntity) helper.getLevel().getBlockEntity(pos);
        var saved = entity.saveWithFullMetadata(helper.getLevel().registryAccess());
        var loaded = new AnvilBlockEntity(pos, entity.getBlockState());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.assertTrue(loaded.hammer().getDamageValue() == 7, "Stored hammer persists");
        AnvilBlock.replace(
                helper.getLevel(),
                pos,
                PoptartWorkstationBlocks.DAMAGED_ANVIL.get().defaultBlockState());
        helper.assertTrue(
                menu.hasWorkingHammer() && menu.getSlot(2).getItem().getDamageValue() == 7,
                "Open menu follows damage-stage replacement");
        var iron = new ItemStack(Items.IRON_INGOT, 2);
        for (int i = 0; i < 2; i++) {
            var state = helper.getLevel().getBlockState(pos);
            ((AnvilBlock) state.getBlock())
                    .useItemOn(
                            iron,
                            state,
                            helper.getLevel(),
                            pos,
                            player,
                            net.minecraft.world.InteractionHand.MAIN_HAND,
                            new net.minecraft.world.phys.BlockHitResult(
                                    net.minecraft.world.phys.Vec3.atCenterOf(pos),
                                    net.minecraft.core.Direction.UP,
                                    pos,
                                    false));
        }
        helper.assertTrue(
                helper.getLevel().getBlockState(pos).is(PoptartWorkstationBlocks.ANVIL.get()) && iron.isEmpty(),
                "One iron repairs each damage stage");
        helper.assertTrue(menu.getSlot(2).getItem().getDamageValue() == 7, "Block repair preserves hammer");
        helper.succeed();
    }

    private static AnvilMenu menu(GameTestHelper helper, Player player, boolean primitive) {
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        helper.getLevel()
                .setBlockAndUpdate(
                        pos,
                        (primitive ? PoptartWorkstationBlocks.PRIMITIVE_ANVIL : PoptartWorkstationBlocks.ANVIL)
                                .get()
                                .defaultBlockState());
        return new AnvilMenu(1, player.getInventory(), ContainerLevelAccess.create(helper.getLevel(), pos));
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void tridentUsesShardsForThreeUpgradesAndRepair(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = menu(helper, player, false);
        menu.getSlot(2).set(new ItemStack(PoptartToolItems.HAMMER.get()));
        var trident = new ItemStack(Items.TRIDENT);
        for (int level = 1; level <= 3; level++) {
            menu.getSlot(0).set(trident);
            menu.getSlot(1).set(new ItemStack(Items.PRISMARINE_CRYSTALS, 64));
            helper.assertTrue(menu.getSlot(3).getItem().isEmpty(), "Crystals do not upgrade tridents");
            menu.getSlot(1).set(new ItemStack(Items.PRISMARINE_SHARD, level + 3));
            var result = menu.getSlot(3).getItem().copy();
            helper.assertTrue(AnvilMenu.getUpgradeCount(result) == level, "Shards upgrade trident");
            menu.onTake(player, result);
            helper.assertTrue(menu.getSlot(1).getItem().isEmpty(), "Costs 4, 5, then 6 shards");
            trident = result;
        }
        trident.setDamageValue(200);
        menu.getSlot(0).set(trident);
        menu.getSlot(1).set(new ItemStack(Items.PRISMARINE_SHARD));
        helper.assertTrue(menu.getSlot(3).getItem().getDamageValue() < 200, "Shards repair upgraded tridents");
        helper.assertTrue(
                !Items.TRIDENT.isValidRepairItem(trident, new ItemStack(Items.PRISMARINE_SHARD)),
                "Vanilla repair rules remain unchanged");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void upgradesPreserveCustomAttributesWithoutStacking(GameTestHelper helper) {
        var sword = new ItemStack(Items.IRON_SWORD);
        var attribute = net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE;
        var custom = new net.minecraft.world.item.component.ItemAttributeModifiers.Entry(
                attribute,
                new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        PoptartCore.location("test_custom"),
                        12,
                        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
                net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND);
        var entries =
                new java.util.ArrayList<>(AnvilMenu.defaultModifiers(sword).modifiers());
        entries.add(custom);
        sword.set(
                net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                new net.minecraft.world.item.component.ItemAttributeModifiers(entries, false));
        for (int level = 1; level <= 3; level++) {
            AnvilMenu.applyUpgrades(sword, level);
            AnvilMenu.healUpgradedModifiers(sword);
            AnvilMenu.healUpgradedModifiers(sword);
            var actual = sword.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS);
            helper.assertTrue(
                    actual.modifiers().contains(custom) && !actual.showInTooltip(),
                    "Custom stats and tooltip flag survive");
            double attack = actual.modifiers().stream()
                    .filter(e -> e.attribute().equals(attribute))
                    .mapToDouble(e -> e.modifier().amount())
                    .sum();
            helper.assertTrue(attack == 17 + level, "Upgrade bonus appears once");
        }
        AnvilMenu.applyUpgrades(sword, 0);
        helper.assertTrue(
                sword.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS)
                        .modifiers()
                        .equals(entries),
                "Removing upgrades preserves original stats");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void wornCrossbowKeepsAmmoAndWorksAfterRepair(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var weapon = new ItemStack(PoptartToolItems.REPEATING_CROSSBOW.get());
        var crossbow = (dev.poptartking.poptartcore.crossbow.RepeatingCrossbowItem) weapon.getItem();
        AnvilMenu.applyUpgrades(weapon, 1);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, weapon);
        player.getInventory().add(new ItemStack(Items.ARROW, 8));
        weapon.setDamageValue(weapon.getMaxDamage());
        crossbow.releaseUsing(weapon, helper.getLevel(), player, 0);
        helper.assertTrue(!net.minecraft.world.item.CrossbowItem.isCharged(weapon), "Worn crossbow cannot load");
        var ammo = net.minecraft.world.item.component.ChargedProjectiles.of(new ItemStack(Items.ARROW));
        weapon.set(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES, ammo);
        helper.assertTrue(
                crossbow.use(helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND)
                                .getResult()
                        == net.minecraft.world.InteractionResult.FAIL,
                "Worn crossbow rejects right click");
        crossbow.performShooting(
                helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND, weapon, 3, 1, null);
        helper.assertTrue(
                ammo.equals(weapon.get(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES)),
                "Loaded ammo is retained");
        weapon.setDamageValue(0);
        crossbow.use(helper.getLevel(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        helper.assertTrue(
                !net.minecraft.world.item.CrossbowItem.isCharged(weapon), "Repaired crossbow fires stored arrow");
        crossbow.releaseUsing(weapon, helper.getLevel(), player, 0);
        helper.assertTrue(net.minecraft.world.item.CrossbowItem.isCharged(weapon), "Repaired crossbow loads again");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void oldUpgradeBonusesMigrateOnceAndSurviveSaving(GameTestHelper helper) {
        var armor = new ItemStack(Items.IRON_CHESTPLATE);
        var oldEntries = AnvilMenu.defaultModifiers(armor).modifiers().stream()
                .map(entry -> {
                    var modifier = entry.modifier();
                    double bonus =
                            entry.attribute().equals(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR) ? 2 : 0;
                    return new net.minecraft.world.item.component.ItemAttributeModifiers.Entry(
                            entry.attribute(),
                            new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                                    modifier.id(), modifier.amount() + bonus, modifier.operation()),
                            entry.slot());
                })
                .toList();
        armor.set(dev.poptartking.poptartcore.registry.AnvilDataComponents.UPGRADE_COUNT.get(), 2);
        armor.set(
                net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                new net.minecraft.world.item.component.ItemAttributeModifiers(oldEntries, true));
        AnvilMenu.healUpgradedModifiers(armor);
        var restored = ItemStack.parseOptional(helper.getLevel().registryAccess(), (net.minecraft.nbt.CompoundTag)
                armor.save(helper.getLevel().registryAccess()));
        for (int i = 0; i < 3; i++) AnvilMenu.healUpgradedModifiers(restored);
        double total =
                restored.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS).modifiers().stream()
                        .filter(entry ->
                                entry.attribute().equals(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR))
                        .mapToDouble(entry -> entry.modifier().amount())
                        .sum();
        helper.assertTrue(total == 8, "Old +2 armor migrates without gaining or losing bonuses");
        AnvilMenu.applyUpgrades(restored, 3);
        total = restored.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS).modifiers().stream()
                .filter(entry -> entry.attribute().equals(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR))
                .mapToDouble(entry -> entry.modifier().amount())
                .sum();
        helper.assertTrue(total == 9, "Migrated armor can upgrade to +3");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void upgradeLevelsCostsBonusesAndHammerWear(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = menu(helper, player, false);
        menu.getSlot(2).set(new ItemStack(PoptartToolItems.HAMMER.get()));
        ItemStack item = new ItemStack(Items.DIAMOND_SWORD);
        int base = item.getMaxDamage();
        for (int level = 1; level <= 3; level++) {
            menu.getSlot(0).set(item);
            menu.getSlot(1).set(new ItemStack(Items.DIAMOND, level + 3));
            menu.createResult();
            var result = menu.getSlot(3).getItem().copy();
            helper.assertTrue(!result.isEmpty() && AnvilMenu.getUpgradeCount(result) == level, "Upgrade level matches");
            helper.assertTrue(
                    result.getMaxDamage() == Math.round(base * (1 + .2 * level)), "Durability increases from base");
            helper.assertTrue(menu.getSlot(3).mayPickup(player), "Stored hammer unlocks result");
            menu.onTake(player, result);
            helper.assertTrue(menu.getSlot(1).getItem().isEmpty(), "Exact upgrade material cost consumed");
            helper.assertTrue(
                    menu.getSlot(2).getItem().getDamageValue() == level, "One hammer durability per operation");
            item = result;
        }
        helper.assertTrue(player.experienceLevel == 0, "No XP required or consumed");
        menu.getSlot(0).set(item);
        menu.getSlot(1).set(new ItemStack(Items.DIAMOND, 64));
        menu.createResult();
        helper.assertTrue(menu.getSlot(3).getItem().isEmpty(), "Fourth upgrade rejected on undamaged item");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void repairsModificationsAndSoap(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = menu(helper, player, false);
        var sword = new ItemStack(Items.IRON_SWORD);
        sword.setDamageValue(200);
        menu.getSlot(0).set(sword);
        menu.getSlot(1).set(new ItemStack(Items.IRON_INGOT));
        menu.createResult();
        helper.assertTrue(
                menu.getSlot(3).getItem().getDamageValue() == 200 - (int) (sword.getMaxDamage() * .35),
                "One material repairs 35 percent");
        helper.assertTrue(!menu.getSlot(3).mayPickup(player), "Hammer required");
        menu.getSlot(0).set(new ItemStack(Items.IRON_CHESTPLATE));
        for (var modifier :
                new net.minecraft.world.item.Item[] {Items.PHANTOM_MEMBRANE, Items.SLIME_BALL, Items.MAGMA_CREAM}) {
            menu.getSlot(1).set(new ItemStack(modifier));
            menu.createResult();
            helper.assertTrue(!menu.getSlot(3).getItem().isEmpty(), "Modification produces result");
            menu.getSlot(0).set(menu.getSlot(3).getItem().copy());
        }
        helper.assertTrue(
                AnvilMenu.getModifications(menu.getSlot(0).getItem()).size() == 3, "Three modifications coexist");
        helper.assertTrue(
                !menu.getSlot(0)
                        .getItem()
                        .getItem()
                        .onDroppedByPlayer(menu.getSlot(0).getItem(), player),
                "Sticky items reject manual dropping");
        helper.assertTrue(
                menu.getSlot(0).getItem().has(net.minecraft.core.component.DataComponents.FIRE_RESISTANT),
                "Magma cream protects item");
        menu.getSlot(1).set(new ItemStack(net.mehvahdjukaar.supplementaries.reg.ModRegistry.SOAP.get()));
        menu.createResult();
        var washed = menu.getSlot(3).getItem();
        helper.assertTrue(
                !washed.isEmpty()
                        && !AnvilMenu.hasAnyModifications(washed)
                        && !washed.has(net.minecraft.core.component.DataComponents.FIRE_RESISTANT),
                "Soap clears modifications and fire resistance");
        var netherite = new ItemStack(Items.NETHERITE_CHESTPLATE);
        AnvilMenu.addModification(netherite, "fire_resistant");
        menu.getSlot(0).set(netherite);
        menu.getSlot(1).set(new ItemStack(net.mehvahdjukaar.supplementaries.reg.ModRegistry.SOAP.get()));
        menu.createResult();
        helper.assertTrue(
                menu.getSlot(3).getItem().has(net.minecraft.core.component.DataComponents.FIRE_RESISTANT),
                "Soap does not strip native netherite fire protection");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void sharedHammerAndPrimitiveWear(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = menu(helper, player, true);
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        var second = new AnvilMenu(2, player.getInventory(), ContainerLevelAccess.create(helper.getLevel(), pos));
        menu.getSlot(2).set(new ItemStack(PoptartToolItems.HAMMER.get()));
        helper.assertTrue(second.hasWorkingHammer(), "Two menus share block hammer");
        var removed = second.getSlot(2).remove(1);
        helper.assertTrue(
                !removed.isEmpty() && !menu.hasWorkingHammer(), "Taking hammer in one menu removes it in both");
        menu.getSlot(2).set(removed);
        menu.removed(player);
        helper.assertTrue(
                ((AnvilBlockEntity) helper.getLevel().getBlockEntity(pos))
                        .hammer()
                        .is(PoptartToolItems.HAMMER.get()),
                "Closing menu preserves hammer on block");
        var wearRandom = new net.minecraft.world.level.levelgen.LegacyRandomSource(42) {
            @Override
            public float nextFloat() {
                return 0;
            }
        };
        for (int i = 0; i < 3; i++) {
            var state = helper.getLevel().getBlockState(pos);
            ((AnvilBlock) state.getBlock()).maybeDamage(helper.getLevel(), pos, state, wearRandom);
        }
        helper.assertTrue(
                helper.getLevel().getBlockState(pos).isAir(),
                "Primitive anvil breaks after three successful wear rolls");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void upgradedItemsWearOutInsteadOfBreakingAndTagsExcludeRings(GameTestHelper helper) {
        var sword = new ItemStack(Items.IRON_SWORD);
        AnvilMenu.applyUpgrades(sword, 3);
        sword.hurtAndBreak(100000, helper.getLevel(), null, item -> {});
        helper.assertTrue(
                !sword.isEmpty() && AnvilMenu.isWornOut(sword), "Upgraded item survives damage at one durability");
        helper.assertTrue(sword.getDestroySpeed(Blocks.COBWEB.defaultBlockState()) == 1, "Worn item mines like a fist");
        helper.assertTrue(
                new ItemStack(PoptartToolItems.STEEL_SWORD.get()).is(PoptartCoreTags.UPGRADEABLE),
                "Our weapons qualify through tags");
        helper.assertTrue(
                !new ItemStack(dev.poptartking.poptartcore.registry.PoptartRingItems.RUSTED_IRON_RING.get())
                        .is(PoptartCoreTags.UPGRADEABLE),
                "Accessories excluded");
        var bow = new ItemStack(Items.BOW);
        int base = bow.getMaxDamage();
        AnvilMenu.applyUpgrades(bow, 1);
        helper.assertTrue(
                bow.getMaxDamage() == Math.round(base * 1.2),
                "Ranged weapons gain durability without fake melee attributes");
        helper.succeed();
    }
}
