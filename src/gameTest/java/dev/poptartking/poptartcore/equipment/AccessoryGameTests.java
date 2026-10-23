package dev.poptartking.poptartcore.equipment;

import com.farcr.ragdoll.api.inventory.CorpseSlots;
import com.farcr.ragdoll.screen.CorpseMenu;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.integration.ragdoll.AccessoryCorpseProvider;
import dev.poptartking.poptartcore.integration.ragdoll.CorpseAccessorySlot;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class AccessoryGameTests {
    private AccessoryGameTests() {}

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void rustedRingArmorDurabilityAndDuplicateRules(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var ring = new ItemStack(dev.poptartking.poptartcore.registry.PoptartRingItems.RUSTED_IRON_RING.get());
        var item = (EquipmentRing) ring.getItem();
        double armor = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
        helper.assertTrue(
                ring.getMaxDamage() == 327 && item.isValidRepairItem(ring, new ItemStack(Items.IRON_INGOT)),
                "Ring must match Wayfarer durability and repair material");
        player.getInventory().setItem(9, ring);
        player.inventoryMenu.quickMoveStack(player, 9);
        EquipmentRing.reconcile(player);
        helper.assertTrue(
                player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR) == armor + 4,
                "Equipped ring must add four armor");
        var second = new ItemStack(item);
        helper.assertTrue(!new PlayerAccessoryHandler(player).isItemValid(1, second), "Second copy must be rejected");
        player.getInventory().setItem(10, second);
        player.inventoryMenu.quickMoveStack(player, 10);
        helper.assertTrue(
                AccessoryInventory.get(player).getStackInSlot(1).isEmpty(),
                "Shift-click must not equip duplicate ring");
        var equipped = AccessoryInventory.get(player).getStackInSlot(0);
        EquipmentRing.hurt(player, player.damageSources().generic(), 8);
        helper.assertTrue(equipped.getDamageValue() == 2, "Eight damage must wear ring by two points");
        equipped.setDamageValue(326);
        EquipmentRing.hurt(player, player.damageSources().generic(), 4);
        helper.assertTrue(
                AccessoryInventory.get(player).getStackInSlot(0).isEmpty(),
                "Worn-out ring must unequip instead of disappearing");
        helper.assertTrue(
                player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR) == armor,
                "Worn-out ring must stop adding armor");
        var recovered = player.getInventory().items.stream()
                .filter(stack -> stack.is(item) && stack.getDamageValue() == 327)
                .findFirst()
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(!recovered.isEmpty(), "Worn-out ring must be returned for repair");
        helper.assertTrue(
                !new PlayerAccessoryHandler(player).isItemValid(0, recovered),
                "Worn-out ring must not equip until repaired");
        recovered.setDamageValue(0);
        new PlayerAccessoryHandler(player).setStackInSlot(1, recovered.copy());
        helper.assertTrue(
                player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR) == armor + 4,
                "Repaired ring must work in either slot");
        new PlayerAccessoryHandler(player).setStackInSlot(1, ItemStack.EMPTY);
        helper.assertTrue(
                player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR) == armor,
                "Removing ring must remove armor bonus");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void beltQuiverSuppliesAndConsumesArrows(GameTestHelper helper) {
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(
                helper.getLevel(), new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "belt-quiver-test"));
        var quiver = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                net.minecraft.resources.ResourceLocation.parse("supplementaries:quiver")));
        var item = (net.mehvahdjukaar.supplementaries.common.items.QuiverItem) quiver.getItem();
        item.modify(quiver, content -> {
            content.setStackInSlot(0, new ItemStack(Items.ARROW, 3));
            return true;
        });
        var inventory = AccessoryInventory.get(player);
        helper.assertTrue(inventory.isItemValid(5, quiver), "Belt slot must accept quiver");
        inventory.setStackInSlot(5, quiver);
        helper.assertTrue(
                net.mehvahdjukaar.supplementaries.common.items.QuiverItem.findActiveQuiver(player) == quiver,
                "Active quiver must be the actual belt stack");
        helper.assertTrue(
                net.mehvahdjukaar.supplementaries.common.items.QuiverItem.findActiveQuiverSlot(player)
                                .get(player)
                        == quiver,
                "Native slot reference must resolve belt quiver without recursion");
        var bow = new ItemStack(Items.BOW);
        ((net.mehvahdjukaar.supplementaries.common.utils.IQuiverPlayer) player)
                .supplementaries$setQuiverSlot(
                        net.mehvahdjukaar.supplementaries.common.items.QuiverItem.findActiveQuiverSlot(player));
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, bow);
        helper.assertTrue(player.getProjectile(bow).is(Items.ARROW), "Bow must find arrows in belt quiver");
        ((net.minecraft.world.item.BowItem) Items.BOW).releaseUsing(bow, player.level(), player, 71980);
        var content = quiver.get(net.mehvahdjukaar.supplementaries.reg.ModComponents.QUIVER_CONTENT.get());
        helper.assertTrue(content.getSelected().getCount() == 2, "Firing bow must consume exactly one stored arrow");
        inventory.setStackInSlot(5, ItemStack.EMPTY);
        helper.assertTrue(
                net.mehvahdjukaar.supplementaries.common.items.QuiverItem.findActiveQuiver(player)
                        .isEmpty(),
                "Unequipping quiver must stop supplying arrows");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void agilityCapeEquipsAndSyncsWithoutChangingHealth(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (var definition : List.of(dev.poptartking.poptartcore.registry.PoptartCapeItems.AGILITY_CAPE)) {
            var cape = new ItemStack(definition.get());
            var inventory = AccessoryInventory.get(player);
            helper.assertTrue(cape.getMaxStackSize() == 1, "Cape must not stack");
            helper.assertTrue(inventory.isItemValid(AgilityCape.SLOT, cape), "Cape slot must accept Agility Cape");
            helper.assertTrue(
                    !inventory.isItemValid(AccessoryInventory.UTILITY_SLOT, cape), "Utility slot must reject cape");
            float maxHealth = player.getMaxHealth();
            player.getInventory().setItem(9, cape);
            player.inventoryMenu.quickMoveStack(player, 9);
            helper.assertTrue(AgilityCape.equipped(player), "Shift-click must equip cape");
            AgilityCape.sync(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
            helper.assertTrue(
                    player.getData(dev.poptartking.poptartcore.registry.PoptartCoreAttachments.CAPE_VISUAL)
                            .is(dev.poptartking.poptartcore.registry.PoptartCapeItems.AGILITY_CAPE.get()),
                    "Equipped cape must sync appearance");
            helper.assertTrue(
                    AgilityCape.texture(new ItemStack(definition.get()))
                            .equals(PoptartCore.location("textures/entity/cape/"
                                    + CapeColors.color(new ItemStack(definition.get()))
                                            .getName() + ".png")),
                    "Each saved cape color must select its worn and elytra texture");
            helper.assertTrue(player.getMaxHealth() == maxHealth, "Cosmetic cape must not change health");
            inventory.setStackInSlot(AgilityCape.SLOT, ItemStack.EMPTY);
            AgilityCape.sync(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
            helper.assertTrue(
                    player.getData(dev.poptartking.poptartcore.registry.PoptartCoreAttachments.CAPE_VISUAL)
                            .isEmpty(),
                    "Unequipping cape must clear appearance");
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void rightClickEquipsAllCapesWithoutReplacingExistingCape(GameTestHelper helper) {
        for (var definition : List.of(dev.poptartking.poptartcore.registry.PoptartCapeItems.AGILITY_CAPE)) {
            for (var hand : net.minecraft.world.InteractionHand.values()) {
                var player = helper.makeMockPlayer(GameType.SURVIVAL);
                player.setItemInHand(hand, new ItemStack(definition.get()));
                var event =
                        new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem(player, hand);
                AgilityCape.equipOnRightClick(event);
                helper.assertTrue(
                        event.isCanceled() && event.getCancellationResult().consumesAction(),
                        "Right-click must handle cape equip");
                helper.assertTrue(
                        AccessoryInventory.get(player)
                                .getStackInSlot(AgilityCape.SLOT)
                                .is(dev.poptartking.poptartcore.registry.PoptartCapeItems.AGILITY_CAPE.get()),
                        "Cape must enter cape slot from either hand");
                helper.assertTrue(
                        player.getItemInHand(hand).isEmpty(), "Survival equip must transfer cape out of hand");
                helper.assertTrue(
                        player.getData(dev.poptartking.poptartcore.registry.PoptartCoreAttachments.CAPE_VISUAL)
                                .is(dev.poptartking.poptartcore.registry.PoptartCapeItems.AGILITY_CAPE.get()),
                        "Appearance must sync on equip");
                player.setItemInHand(hand, new ItemStack(definition.get()));
                var blocked =
                        new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem(player, hand);
                AgilityCape.equipOnRightClick(blocked);
                helper.assertTrue(
                        !blocked.isCanceled() && player.getItemInHand(hand).getCount() == 1,
                        "Occupied slot must preserve held cape");
            }
        }
        var creative = helper.makeMockPlayer(GameType.CREATIVE);
        creative.getAbilities().instabuild = true;
        var capeItem = dev.poptartking.poptartcore.registry.PoptartCapeItems.AGILITY_CAPE.get();
        creative.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(capeItem));
        AgilityCape.equipOnRightClick(new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem(
                creative, net.minecraft.world.InteractionHand.MAIN_HAND));
        helper.assertTrue(
                creative.getMainHandItem().getCount() == 1 && AgilityCape.equipped(creative),
                "Creative equip must preserve held item");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void corpseBacktankVisualUsesCapturedSlotAndClearsAfterRecovery(GameTestHelper helper) {
        int base = CorpseSlots.baseSlot(AccessoryCorpseProvider.ID);
        helper.assertTrue(base >= 41, "Corpse accessory provider must be registered");
        var items = new java.util.ArrayList<ItemStack>(
                java.util.Collections.nCopies(base + AccessoryInventory.SIZE, ItemStack.EMPTY));
        int back = base + AccessoryInventory.BACK_SLOT;
        for (var tank : List.of(
                com.simibubi.create.AllItems.COPPER_BACKTANK.asStack(),
                com.simibubi.create.AllItems.NETHERITE_BACKTANK.asStack())) {
            items.set(back, tank);
            var visual = dev.poptartking.poptartcore.integration.create.BackSlotBacktank.corpseVisualStack(items);
            helper.assertTrue(
                    ItemStack.matches(visual, tank) && visual != tank,
                    "Corpse must render a copy of its captured backtank");
            items.set(back, ItemStack.EMPTY);
            helper.assertTrue(
                    dev.poptartking.poptartcore.integration.create.BackSlotBacktank.corpseVisualStack(items)
                            .isEmpty(),
                    "Recovering the tank must clear its corpse visual");
        }
        items.set(back, new ItemStack(Items.PAPER));
        helper.assertTrue(
                dev.poptartking.poptartcore.integration.create.BackSlotBacktank.corpseVisualStack(items)
                        .isEmpty(),
                "Other back items must not render as tanks");
        helper.assertTrue(
                dev.poptartking.poptartcore.integration.create.BackSlotBacktank.corpseVisualStack(List.of())
                        .isEmpty(),
                "Older corpses without back slots must remain supported");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void utilityTotemSavesFromFatalDamageAndIsConsumed(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var inventory = AccessoryInventory.get(player);
        helper.assertTrue(
                inventory.isItemValid(AccessoryInventory.UTILITY_SLOT, new ItemStack(Items.TOTEM_OF_UNDYING)),
                "Utility slot must accept totems");
        inventory.insertItem(AccessoryInventory.UTILITY_SLOT, new ItemStack(Items.TOTEM_OF_UNDYING), false);
        player.hurt(player.damageSources().generic(), 1000);
        helper.assertTrue(
                player.isAlive() && player.getHealth() == 1, "Utility totem must prevent actual fatal damage");
        helper.assertTrue(
                inventory.getStackInSlot(AccessoryInventory.UTILITY_SLOT).isEmpty(), "Saving must consume the totem");
        helper.assertTrue(
                player.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION)
                        && player.hasEffect(net.minecraft.world.effect.MobEffects.ABSORPTION)
                        && player.hasEffect(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE),
                "Vanilla recovery effects must apply");
        player.invulnerableTime = 0;
        player.hurt(player.damageSources().generic(), 1000);
        helper.assertTrue(!player.isAlive(), "Spent utility totem must not save a second time");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void utilityTotemPreservesHeldPriorityAndVanillaBypass(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var inventory = AccessoryInventory.get(player);
        inventory.setStackInSlot(AccessoryInventory.UTILITY_SLOT, new ItemStack(Items.TOTEM_OF_UNDYING));
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
        player.hurt(player.damageSources().generic(), 1000);
        helper.assertTrue(player.isAlive() && player.getOffhandItem().isEmpty(), "Held totem must save first");
        helper.assertTrue(
                !inventory.getStackInSlot(AccessoryInventory.UTILITY_SLOT).isEmpty(),
                "Held save must preserve utility totem");
        player.invulnerableTime = 0;
        player.hurt(player.damageSources().fellOutOfWorld(), 1000);
        helper.assertTrue(!player.isAlive(), "Utility totem must not override vanilla void damage restriction");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void backtankCannotOccupyBothEquipmentSlots(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var copper = com.simibubi.create.AllItems.COPPER_BACKTANK.asStack();
        var netherite = com.simibubi.create.AllItems.NETHERITE_BACKTANK.asStack();
        var chest = net.minecraft.world.entity.EquipmentSlot.CHEST;
        var handler = new PlayerAccessoryHandler(player);
        player.setItemSlot(chest, copper);
        helper.assertTrue(
                !handler.isItemValid(AccessoryInventory.BACK_SLOT, netherite), "Chest tank must block back-slot tank");
        helper.assertTrue(
                handler.insertItem(AccessoryInventory.BACK_SLOT, netherite, false) == netherite,
                "Blocked insertion must return the unchanged stack");
        helper.assertTrue(
                AccessoryInventory.get(player)
                        .getStackInSlot(AccessoryInventory.BACK_SLOT)
                        .isEmpty(),
                "Rejected tank must not be stored");
        player.getInventory().setItem(9, netherite.copy());
        player.inventoryMenu.quickMoveStack(player, 9);
        helper.assertTrue(
                AccessoryInventory.get(player)
                        .getStackInSlot(AccessoryInventory.BACK_SLOT)
                        .isEmpty(),
                "Shift-click must not equip a second backtank");
        player.setItemSlot(chest, ItemStack.EMPTY);
        handler.insertItem(AccessoryInventory.BACK_SLOT, netherite, false);
        helper.assertTrue(
                !copper.canEquip(chest, player), "Back tank must block chest-slot placement and dispenser equip");
        helper.assertTrue(
                !player.inventoryMenu.getSlot(6).mayPlace(copper), "Chest menu slot must reject a second tank");
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, copper);
        var result = copper.use(player.level(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        helper.assertTrue(
                result.getResult() == net.minecraft.world.InteractionResult.FAIL,
                "Right-click must reject a second tank without consuming it");
        helper.assertTrue(
                player.getItemBySlot(chest).isEmpty() && player.getMainHandItem() == copper,
                "Failed right-click must preserve both equipment and held stack");
        helper.assertTrue(
                new ItemStack(Items.DIAMOND_CHESTPLATE).canEquip(chest, player),
                "Normal chestplates must remain allowed");
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, ItemStack.EMPTY);
        helper.assertTrue(copper.canEquip(chest, player), "Removing the back tank must unblock chest tank equip");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void backtankArmorUsesHigherStatAndClearsOnRemoval(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var chestSlot = net.minecraft.world.entity.EquipmentSlot.CHEST;
        var attributes = List.of(
                net.minecraft.world.entity.ai.attributes.Attributes.ARMOR,
                net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS,
                net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE);
        for (var tank : List.of(
                com.simibubi.create.AllItems.COPPER_BACKTANK.asStack(),
                com.simibubi.create.AllItems.NETHERITE_BACKTANK.asStack())) {
            AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, tank);
            for (var chestplate : List.of(
                    ItemStack.EMPTY,
                    new ItemStack(Items.LEATHER_CHESTPLATE),
                    new ItemStack(Items.DIAMOND_CHESTPLATE),
                    new ItemStack(Items.NETHERITE_CHESTPLATE))) {
                player.setItemSlot(chestSlot, chestplate);
                refreshEquipment(player);
                dev.poptartking.poptartcore.integration.create.BackSlotBacktank.updateArmor(player);
                for (var attribute : attributes) {
                    double[] expected = {0, 0};
                    tank.forEachModifier(chestSlot, (type, modifier) -> {
                        if (type.equals(attribute)) expected[0] += modifier.amount();
                    });
                    chestplate.forEachModifier(chestSlot, (type, modifier) -> {
                        if (type.equals(attribute)) expected[1] += modifier.amount();
                    });
                    helper.assertTrue(
                            Math.abs(player.getAttributeValue(attribute) - Math.max(expected[0], expected[1]))
                                    < 0.00001,
                            "Equipment must use the higher stat, not sum both: " + attribute.getRegisteredName());
                    dev.poptartking.poptartcore.integration.create.BackSlotBacktank.updateArmor(player);
                    helper.assertTrue(
                            Math.abs(player.getAttributeValue(attribute) - Math.max(expected[0], expected[1]))
                                    < 0.00001,
                            "Repeated updates must not stack bonuses");
                }
            }
        }
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, ItemStack.EMPTY);
        player.setItemSlot(chestSlot, ItemStack.EMPTY);
        refreshEquipment(player);
        dev.poptartking.poptartcore.integration.create.BackSlotBacktank.updateArmor(player);
        for (var attribute : attributes)
            helper.assertTrue(player.getAttributeValue(attribute) == 0, "Unequipping must remove all backtank bonuses");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void duplicateBacktankIsReturnedWithoutLosingAir(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var copper = com.simibubi.create.AllItems.COPPER_BACKTANK.asStack();
        copper.set(com.simibubi.create.AllDataComponents.BACKTANK_AIR, 123);
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, copper);
        player.setItemSlot(
                net.minecraft.world.entity.EquipmentSlot.CHEST,
                com.simibubi.create.AllItems.NETHERITE_BACKTANK.asStack());
        dev.poptartking.poptartcore.integration.create.BackSlotBacktank.sync(
                new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
        helper.assertTrue(
                AccessoryInventory.get(player)
                        .getStackInSlot(AccessoryInventory.BACK_SLOT)
                        .isEmpty(),
                "Old duplicate equipment must be repaired");
        helper.assertTrue(
                player.getInventory().items.stream()
                        .anyMatch(stack -> stack.is(com.simibubi.create.AllItems.COPPER_BACKTANK.get())
                                && stack.getOrDefault(com.simibubi.create.AllDataComponents.BACKTANK_AIR, 0) == 123),
                "Returned tank must preserve its stored air");
        helper.succeed();
    }

    private static void refreshEquipment(Player player) {
        // Exercise vanilla attribute removal/addition without ticking unrelated mod player hooks.
        try {
            var update = net.minecraft.world.entity.LivingEntity.class.getDeclaredMethod("detectEquipmentUpdates");
            update.setAccessible(true);
            update.invoke(player);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not run vanilla equipment update", exception);
        }
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void backtanksProvideAirWithoutReplacingChestArmor(GameTestHelper helper) {
        var player = new TestPlayer(helper);
        var equipment = AccessoryInventory.get(player);
        var chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, chestplate);
        for (var item : List.of(
                com.simibubi.create.AllItems.COPPER_BACKTANK.get(),
                com.simibubi.create.AllItems.NETHERITE_BACKTANK.get())) {
            var tank = new ItemStack(item);
            int capacity = com.simibubi.create.content.equipment.armor.BacktankUtil.maxAir(tank);
            tank.set(com.simibubi.create.AllDataComponents.BACKTANK_AIR, capacity);
            helper.assertTrue(
                    equipment.isItemValid(AccessoryInventory.BACK_SLOT, tank), "Back slot must accept both backtanks");
            equipment.setStackInSlot(AccessoryInventory.BACK_SLOT, tank);
            var supplied = com.simibubi.create.content.equipment.armor.BacktankUtil.getAllWithAir(player);
            helper.assertTrue(
                    supplied.size() == 1 && supplied.getFirst() == tank,
                    "Create must consume the stored tank, not a copy");
            helper.assertTrue(
                    com.simibubi.create.content.equipment.armor.BacktankUtil.canAbsorbDamage(player, 100),
                    "Back slot must power air tools");
            helper.assertTrue(
                    com.simibubi.create.content.equipment.armor.BacktankUtil.getAir(tank) < capacity,
                    "Tool use must consume back-slot air");
            helper.assertTrue(
                    com.simibubi.create.content.equipment.armor.BacktankItem.getWornBy(player) == item,
                    "Create worn-model lookup must find back slot");
            helper.assertTrue(
                    player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST) == chestplate,
                    "Backtank must not replace chest armor");
            helper.assertTrue(
                    dev.poptartking.poptartcore.integration.create.BackSlotBacktank.visualChestStack(player, chestplate)
                            == chestplate,
                    "Chest armor must take visual priority over the backtank");
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, ItemStack.EMPTY);
            helper.assertTrue(
                    dev.poptartking.poptartcore.integration.create.BackSlotBacktank.visualChestStack(
                                    player, ItemStack.EMPTY)
                            == tank,
                    "Empty chest slot must display the full backtank armor");
            helper.assertTrue(
                    player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST)
                            .isEmpty(),
                    "Selecting the backtank appearance must not equip chest armor");
            player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, chestplate);
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void backtankSupportsDivingUntilEmpty(GameTestHelper helper) {
        var player = new TestPlayer(helper);
        var tank = com.simibubi.create.AllItems.COPPER_BACKTANK.asStack();
        tank.set(com.simibubi.create.AllDataComponents.BACKTANK_AIR, 100);
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, tank);
        player.setItemSlot(
                net.minecraft.world.entity.EquipmentSlot.HEAD,
                com.simibubi.create.AllItems.COPPER_DIVING_HELMET.asStack());
        var breathing = new net.neoforged.neoforge.event.entity.living.LivingBreatheEvent(player, false, 1, 0);
        com.simibubi.create.content.equipment.armor.DivingHelmetItem.breatheUnderwater(breathing);
        helper.assertTrue(breathing.canBreathe(), "Diving helmet must use back-slot air");
        tank.set(com.simibubi.create.AllDataComponents.BACKTANK_AIR, 0);
        var depleted = new net.neoforged.neoforge.event.entity.living.LivingBreatheEvent(player, false, 1, 0);
        com.simibubi.create.content.equipment.armor.DivingHelmetItem.breatheUnderwater(depleted);
        helper.assertTrue(!depleted.canBreathe(), "Empty tank must not provide free breathing");
        helper.assertTrue(
                com.simibubi.create.content.equipment.armor.BacktankUtil.getAllWithAir(player)
                        .isEmpty(),
                "Empty tanks must not power tools");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void backtankSnapshotTracksChangesAndRemoval(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var tank = com.simibubi.create.AllItems.NETHERITE_BACKTANK.asStack();
        tank.set(com.simibubi.create.AllDataComponents.BACKTANK_AIR, 100);
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, tank);
        var tick = new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player);
        dev.poptartking.poptartcore.integration.create.BackSlotBacktank.sync(tick);
        var snapshot = player.getData(dev.poptartking.poptartcore.registry.PoptartCoreAttachments.BACKTANK_VISUAL);
        helper.assertTrue(
                ItemStack.matches(snapshot, tank) && snapshot != tank,
                "Network visual must be an independent snapshot");
        tank.set(com.simibubi.create.AllDataComponents.BACKTANK_AIR, 99);
        helper.assertTrue(
                com.simibubi.create.content.equipment.armor.BacktankUtil.getAir(snapshot) == 100,
                "Air changes must not silently mutate network snapshot");
        dev.poptartking.poptartcore.integration.create.BackSlotBacktank.sync(tick);
        helper.assertTrue(
                com.simibubi.create.content.equipment.armor.BacktankUtil.getAir(player.getData(
                                dev.poptartking.poptartcore.registry.PoptartCoreAttachments.BACKTANK_VISUAL))
                        == 99,
                "Air changes must update snapshot");
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, ItemStack.EMPTY);
        dev.poptartking.poptartcore.integration.create.BackSlotBacktank.sync(tick);
        helper.assertTrue(
                player.getData(dev.poptartking.poptartcore.registry.PoptartCoreAttachments.BACKTANK_VISUAL)
                        .isEmpty(),
                "Removing tank must clear visual");
        helper.assertTrue(
                com.simibubi.create.content.equipment.armor.BacktankItem.getWornBy(player) == null,
                "Removing tank must clear worn-model lookup");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void utilitySpyglassRequiresHeldInputAndEquippedItem(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var equipment = AccessoryInventory.get(player);
        helper.assertTrue(!UtilityWield.start(player), "Empty Utility must not wield");
        equipment.setStackInSlot(0, new ItemStack(Items.SPYGLASS));
        helper.assertTrue(!UtilityWield.start(player), "Spyglass outside Utility must not wield");
        equipment.setStackInSlot(0, ItemStack.EMPTY);
        equipment.setStackInSlot(AccessoryInventory.UTILITY_SLOT, new ItemStack(Items.SPYGLASS));
        helper.assertTrue(UtilityWield.start(player), "Equipped spyglass must support held input");
        player.getMainHandItem().use(player.level(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        helper.assertTrue(player.isScoping(), "Native spyglass use must scope");
        UtilityWield.stop(player);
        helper.assertTrue(!player.isScoping(), "Release must stop native scope");
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
        player.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
        helper.assertTrue(!UtilityWield.start(player), "Using a held item must take precedence");
        player.stopUsingItem();
        helper.assertTrue(UtilityWield.start(player), "Utility must be available after held item use");
        equipment.setStackInSlot(AccessoryInventory.UTILITY_SLOT, ItemStack.EMPTY);
        UtilityWield.tick(player);
        helper.assertTrue(!UtilityWield.isWielding(player), "Removing spyglass must stop wielding");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void spyglassEquipsInUtilityOnly(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var equipment = AccessoryInventory.get(player);
        var spyglass = new ItemStack(Items.SPYGLASS);
        for (int i = 0; i < AccessoryInventory.SIZE; i++) {
            helper.assertTrue(
                    equipment.isItemValid(i, spyglass) == (i == 4), "Spyglass eligibility must be limited to Utility");
        }
        player.getInventory().setItem(9, spyglass);
        player.inventoryMenu.quickMoveStack(player, 9);
        helper.assertTrue(
                equipment.getStackInSlot(4).is(Items.SPYGLASS), "Shift-click did not equip spyglass in Utility");
        helper.assertTrue(player.getInventory().getItem(9).isEmpty(), "Equipping duplicated spyglass");
        helper.succeed();
    }

    private static final class TestPlayer extends net.minecraft.server.level.ServerPlayer {
        TestPlayer(GameTestHelper helper) {
            this(
                    helper,
                    net.minecraft.server.network.CommonListenerCookie.createInitial(
                            new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "equipment-test"), false));
        }

        private TestPlayer(GameTestHelper helper, net.minecraft.server.network.CommonListenerCookie cookie) {
            super(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
            var connection =
                    new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
            new io.netty.channel.embedded.EmbeddedChannel(connection);
            new net.minecraft.server.network.ServerGamePacketListenerImpl(
                    helper.getLevel().getServer(), connection, this, cookie);
        }

        void dropEquipmentForTest() {
            super.dropEquipment();
        }
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void creativePacketsRespectCategoryCountAndPermission(GameTestHelper helper) {
        var player = new TestPlayer(helper);
        player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        var bandage = new ItemStack(com.farcr.nomansland.common.registry.items.NMLItems.BANDAGE.get(), 16);
        player.connection.handleSetCreativeModeSlot(
                new net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket(50, bandage));
        helper.assertTrue(
                AccessoryInventory.get(player)
                                .getStackInSlot(AccessoryInventory.UTILITY_SLOT)
                                .getCount()
                        == 16,
                "Creative utility slot accepts 16 bandages");
        player.connection.handleSetCreativeModeSlot(
                new net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket(
                        50, bandage.copyWithCount(17)));
        helper.assertTrue(
                AccessoryInventory.get(player)
                                .getStackInSlot(AccessoryInventory.UTILITY_SLOT)
                                .getCount()
                        == 16,
                "Creative packet rejects oversized bandage stack");
        player.connection.handleSetCreativeModeSlot(
                new net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket(
                        50, new ItemStack(Items.TOTEM_OF_UNDYING, 2)));
        helper.assertTrue(
                AccessoryInventory.get(player)
                                .getStackInSlot(AccessoryInventory.UTILITY_SLOT)
                                .getCount()
                        == 16,
                "Creative packet rejects stacks of other utility items");
        player.connection.handleSetCreativeModeSlot(
                new net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket(
                        46, new ItemStack(Items.STONE)));
        helper.assertTrue(
                AccessoryInventory.get(player).getStackInSlot(0).is(Items.STONE), "Creative packet did not equip ring");
        player.connection.handleSetCreativeModeSlot(
                new net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket(
                        48, new ItemStack(Items.STONE)));
        helper.assertTrue(
                AccessoryInventory.get(player).getStackInSlot(2).isEmpty(), "Creative packet bypassed category rules");
        player.connection.handleSetCreativeModeSlot(
                new net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket(
                        47, new ItemStack(Items.STONE, 2)));
        helper.assertTrue(
                AccessoryInventory.get(player).getStackInSlot(1).isEmpty(),
                "Creative packet bypassed single-item limit");
        var copper = com.simibubi.create.AllItems.COPPER_BACKTANK.asStack();
        var netherite = com.simibubi.create.AllItems.NETHERITE_BACKTANK.asStack();
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, copper);
        player.connection.handleSetCreativeModeSlot(
                new net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket(6, netherite));
        helper.assertTrue(
                player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST)
                        .isEmpty(),
                "Creative chest packet must not equip a second tank");
        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, ItemStack.EMPTY);
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, netherite);
        player.connection.handleSetCreativeModeSlot(
                new net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket(52, copper));
        helper.assertTrue(
                AccessoryInventory.get(player)
                        .getStackInSlot(AccessoryInventory.BACK_SLOT)
                        .isEmpty(),
                "Creative back-slot packet must not equip a second tank");
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.connection.handleSetCreativeModeSlot(
                new net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket(
                        47, new ItemStack(Items.STONE)));
        helper.assertTrue(
                AccessoryInventory.get(player).getStackInSlot(1).isEmpty(),
                "Survival player used creative equipment packet");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void playerSaveRetainsEquipment(GameTestHelper helper) {
        Player original = helper.makeMockPlayer(GameType.SURVIVAL);
        AccessoryInventory.get(original).setStackInSlot(4, new ItemStack(Items.PAPER));
        var saved = original.saveWithoutId(new net.minecraft.nbt.CompoundTag());
        Player loaded = helper.makeMockPlayer(GameType.SURVIVAL);
        loaded.load(saved);
        helper.assertTrue(
                AccessoryInventory.get(loaded).getStackInSlot(4).is(Items.PAPER), "Player save/load lost accessories");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void fallbackEquipmentDropSpawnsOneItem(GameTestHelper helper) {
        var rules = helper.getLevel().getGameRules();
        boolean keep = rules.getBoolean(GameRules.RULE_KEEPINVENTORY);
        try {
            rules.getRule(GameRules.RULE_KEEPINVENTORY)
                    .set(false, helper.getLevel().getServer());
            var player = new TestPlayer(helper);
            var position = helper.absolutePos(new net.minecraft.core.BlockPos(4, 2, 4));
            player.setPos(position.getX(), position.getY(), position.getZ());
            AccessoryInventory.get(player).setStackInSlot(0, new ItemStack(Items.PAPER));
            player.dropEquipmentForTest();
            helper.assertTrue(
                    AccessoryInventory.get(player).getStackInSlot(0).isEmpty(), "Death did not clear equipment");
            int drops = helper
                    .getLevel()
                    .getEntitiesOfClass(
                            net.minecraft.world.entity.item.ItemEntity.class,
                            player.getBoundingBox().inflate(3))
                    .stream()
                    .filter(entity -> entity.getItem().is(Items.PAPER))
                    .mapToInt(entity -> entity.getItem().getCount())
                    .sum();
            helper.assertTrue(drops == 1, "Expected one accessory ground drop, got " + drops);
        } finally {
            rules.getRule(GameRules.RULE_KEEPINVENTORY)
                    .set(keep, helper.getLevel().getServer());
        }
        helper.succeed();
    }

    @SubscribeEvent
    public static void template(ServerAboutToStartEvent event) throws Exception {
        event.getServer()
                .getStructureManager()
                .getOrCreate(PoptartCore.location("millstone_test"))
                .load(
                        event.getServer()
                                .registryAccess()
                                .lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK),
                        TagParser.parseTag("{size:[9,5,9],palette:[{Name:\"minecraft:air\"}],blocks:[],entities:[]}"));
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void shiftClickEquipsOnlyOneItemPerMatchingSlot(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().setItem(9, new ItemStack(Items.STONE, 3));
        player.inventoryMenu.quickMoveStack(player, 9);
        player.inventoryMenu.quickMoveStack(player, 9);
        var equipment = AccessoryInventory.get(player);
        helper.assertTrue(equipment.getStackInSlot(0).getCount() == 1, "First ring slot did not equip one item");
        helper.assertTrue(equipment.getStackInSlot(1).getCount() == 1, "Second ring slot did not equip one item");
        helper.assertTrue(player.getInventory().getItem(9).getCount() == 1, "Shift-click duplicated or lost rings");
        helper.assertTrue(
                !player.inventoryMenu.slots.get(48).mayPlace(new ItemStack(Items.STONE)), "Ring accepted in cape slot");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void slotsPersistAndRejectUnassignedItems(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = player.inventoryMenu;
        List<net.minecraft.world.inventory.Slot> slots = menu.slots.stream()
                .filter(slot -> slot instanceof AccessorySlot)
                .toList();
        helper.assertTrue(slots.size() == AccessoryInventory.SIZE, "Expected seven equipment slots");
        for (int i = 0; i < AccessoryInventory.SIZE; i++) {
            helper.assertTrue(
                    slots.get(i).x == AccessoryLayout.x(i) && slots.get(i).y == AccessoryLayout.y(i),
                    "Equipment position incorrect");
            helper.assertTrue(
                    slots.get(i).getMaxStackSize(new ItemStack(Items.PAPER)) == 1,
                    "Non-bandage equipment must hold one item");
            helper.assertTrue(!slots.get(i).mayPlace(new ItemStack(Items.DIRT)), "Unassigned items must be rejected");
            slots.get(i).set(new ItemStack(Items.PAPER));
        }
        var saved =
                AccessoryInventory.get(player).serializeNBT(helper.getLevel().registryAccess());
        var loaded = new AccessoryInventory();
        loaded.deserializeNBT(helper.getLevel().registryAccess(), saved);
        for (int i = 0; i < AccessoryInventory.SIZE; i++)
            helper.assertTrue(loaded.getStackInSlot(i).is(Items.PAPER), "Saved equipment was lost");
        var legacy = saved.copy();
        legacy.putInt("Size", 5);
        loaded.deserializeNBT(helper.getLevel().registryAccess(), legacy);
        helper.assertTrue(loaded.getSlots() == 7, "Old saves must gain belt and back slots");
        helper.assertTrue(loaded.getStackInSlot(3).is(Items.PAPER), "Renaming pendant must preserve its item");
        var replacement = helper.makeMockPlayer(GameType.SURVIVAL);
        AccessoryInventory.copy(player, replacement);
        AccessoryInventory.get(player).setStackInSlot(0, ItemStack.EMPTY);
        helper.assertTrue(
                AccessoryInventory.get(replacement).getStackInSlot(0).is(Items.PAPER),
                "Clone did not copy equipment independently");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void relocatedCraftingStillProducesResults(GameTestHelper helper) {
        var player = new TestPlayer(helper);
        var menu = player.inventoryMenu;
        helper.assertTrue(
                menu.slots.get(0).x == 134 && menu.slots.get(0).y == 59, "Crafting result position incorrect");
        for (int i = 0; i < 4; i++) {
            var slot = menu.slots.get(i + 1);
            helper.assertTrue(
                    slot.x == 125 + (i % 2) * 18 && slot.y == 13 + (i / 2) * 18, "Crafting input position incorrect");
            slot.set(new ItemStack(Items.OAK_PLANKS));
        }
        helper.assertTrue(menu.slots.get(0).getItem().is(Items.CRAFTING_TABLE), "Moved crafting grid must still craft");
        helper.assertTrue(
                menu.slots.get(45).x == 77 && menu.slots.get(45).y == 62,
                "Vanilla offhand must remain the shield slot");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void shiftClickUnequipsWithoutChangingVanillaSlots(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        AccessoryInventory.get(player).setStackInSlot(0, new ItemStack(Items.PAPER));
        var slot = player.inventoryMenu.slots.stream()
                .filter(candidate -> candidate instanceof AccessorySlot)
                .findFirst()
                .orElseThrow();
        helper.assertTrue(slot.index == 46, "Vanilla inventory indices changed");
        player.inventoryMenu.quickMoveStack(player, slot.index);
        helper.assertTrue(AccessoryInventory.get(player).getStackInSlot(0).isEmpty(), "Shift-click did not unequip");
        helper.assertTrue(player.getInventory().countItem(Items.PAPER) == 1, "Shift-click lost or duplicated an item");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void corpseCaptureRestoresEquipmentOnce(GameTestHelper helper) {
        var rules = helper.getLevel().getGameRules();
        boolean keep = rules.getBoolean(GameRules.RULE_KEEPINVENTORY);
        try {
            rules.getRule(GameRules.RULE_KEEPINVENTORY)
                    .set(false, helper.getLevel().getServer());
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            AccessoryInventory.get(player).setStackInSlot(2, new ItemStack(Items.PAPER));
            AccessoryInventory.get(player).setStackInSlot(6, new ItemStack(Items.PAPER));
            int base = CorpseSlots.baseSlot(AccessoryCorpseProvider.ID);
            helper.assertTrue(base >= 41, "Corpse provider not registered");
            var captured = CorpseSlots.capture(player);
            helper.assertTrue(captured.get(base + 6).is(Items.PAPER), "Corpse did not capture back slot");
            helper.assertTrue(
                    AccessoryInventory.get(player).getStackInSlot(6).isEmpty(), "Capture duplicated back equipment");
            helper.assertTrue(
                    CorpseSlots.restore(player, base + 6, captured.get(base + 6))
                            .isEmpty(),
                    "Recovery did not restore back equipment");
            helper.assertTrue(captured.get(base + 2).is(Items.PAPER), "Corpse did not capture cape slot");
            helper.assertTrue(
                    AccessoryInventory.get(player).getStackInSlot(2).isEmpty(), "Capture duplicated equipment");
            helper.assertTrue(
                    CorpseSlots.capture(player).get(base + 2).isEmpty(), "Second capture duplicated equipment");
            var corpse = new SimpleContainer(captured.toArray(ItemStack[]::new));
            var menu = new CorpseMenu(null, 1, player.getInventory(), corpse);
            helper.assertTrue(
                    menu.getSlot(39).x == 23 && menu.getSlot(39).y == 19,
                    "Corpse helmet must match upper-left texture slot");
            helper.assertTrue(
                    menu.getSlot(40).x == 113 && menu.getSlot(40).y == 73,
                    "Corpse offhand must match lower-left equipment slot");
            helper.assertTrue(
                    menu.getSlot(9).x == 11 && menu.getSlot(9).y == 95, "Corpse main inventory must match texture");
            helper.assertTrue(menu.getSlot(0).x == 11 && menu.getSlot(0).y == 153, "Corpse hotbar must match texture");
            helper.assertTrue(
                    menu.getSlot(50).x == 11 && menu.getSlot(50).y == 192, "Player inventory must match lower panel");
            helper.assertTrue(
                    menu.getSlot(41).x == 11 && menu.getSlot(41).y == 250, "Player hotbar must match lower panel");
            int[][] equipmentPositions = {{131, 37}, {131, 55}, {113, 37}, {131, 19}, {131, 73}, {113, 55}, {113, 19}};
            for (int i = 0; i < AccessoryInventory.SIZE; i++) {
                var slot = menu.getSlot(77 + i);
                helper.assertTrue(
                        slot.x == equipmentPositions[i][0] && slot.y == equipmentPositions[i][1],
                        "Corpse equipment slot does not match texture: " + i);
            }
            helper.assertTrue(
                    menu.slots.stream()
                                    .filter(slot -> slot instanceof CorpseAccessorySlot)
                                    .count()
                            == AccessoryInventory.SIZE,
                    "Corpse menu hides equipment");
            helper.assertTrue(
                    CorpseSlots.restore(player, base + 2, captured.get(base + 2))
                            .isEmpty(),
                    "Recovery did not equip item");
            helper.assertTrue(
                    AccessoryInventory.get(player).getStackInSlot(2).is(Items.PAPER), "Recovered cape missing");
            helper.assertTrue(
                    !CorpseSlots.restore(player, base + 2, new ItemStack(Items.PAPER))
                            .isEmpty(),
                    "Occupied slot was overwritten");
        } finally {
            rules.getRule(GameRules.RULE_KEEPINVENTORY)
                    .set(keep, helper.getLevel().getServer());
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void keepInventoryDoesNotCopyAccessoriesIntoCorpse(GameTestHelper helper) {
        var rules = helper.getLevel().getGameRules();
        boolean keep = rules.getBoolean(GameRules.RULE_KEEPINVENTORY);
        try {
            rules.getRule(GameRules.RULE_KEEPINVENTORY)
                    .set(true, helper.getLevel().getServer());
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            AccessoryInventory.get(player).setStackInSlot(3, new ItemStack(Items.PAPER));
            var captured = CorpseSlots.capture(player);
            int base = CorpseSlots.baseSlot(AccessoryCorpseProvider.ID);
            helper.assertTrue(captured.get(base + 3).isEmpty(), "keepInventory equipment duplicated into corpse");
            helper.assertTrue(
                    AccessoryInventory.get(player).getStackInSlot(3).is(Items.PAPER),
                    "keepInventory equipment was cleared");
        } finally {
            rules.getRule(GameRules.RULE_KEEPINVENTORY)
                    .set(keep, helper.getLevel().getServer());
        }
        helper.succeed();
    }
}
