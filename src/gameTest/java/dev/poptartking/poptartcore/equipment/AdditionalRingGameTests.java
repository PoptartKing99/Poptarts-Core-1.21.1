package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartMaterialItems;
import dev.poptartking.poptartcore.registry.PoptartRarities;
import dev.poptartking.poptartcore.registry.PoptartRingItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class AdditionalRingGameTests {
    @SubscribeEvent
    public static void arenaTemplate(ServerAboutToStartEvent event) throws Exception {
        event.getServer()
                .getStructureManager()
                .getOrCreate(PoptartCore.location("ring_arena"))
                .load(
                        event.getServer()
                                .registryAccess()
                                .lookupOrThrow(net.minecraft.core.registries.Registries.BLOCK),
                        TagParser.parseTag(
                                "{size:[21,5,21],palette:[{Name:\"minecraft:air\"}],blocks:[],entities:[]}"));
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void newRingEquipRepairAndDurability(GameTestHelper helper) {
        Item[] rings = {
            PoptartRingItems.TINY_HORSESHOE.get(),
            PoptartRingItems.THORN_RING.get(),
            PoptartRingItems.RING_OF_THE_AFFLICTED.get(),
            PoptartRingItems.MERCURY_GEMSTONE_RING.get(),
            PoptartRingItems.ANCIENT_BLESSING.get(),
            PoptartRingItems.SUNLIGHT_RING.get(),
            PoptartRingItems.MALEVOLENT_RING.get()
        };
        Item[] repairs = {
            Items.IRON_INGOT,
            PoptartMaterialItems.SILVER_INGOT.get(),
            PoptartMaterialItems.BONE_ASH.get(),
            null,
            null,
            Items.GOLD_INGOT,
            PoptartMaterialItems.CHTHONIC_CALCULUS.get()
        };
        int[] durability = {527, 547, 927, 927, 0, 327, 527};
        for (int i = 0; i < rings.length; i++) {
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            var stack = new ItemStack(rings[i]);
            helper.assertTrue(
                    stack.getMaxDamage() == durability[i] && stack.getMaxStackSize() == 1,
                    "Source durability: " + rings[i]);
            helper.assertTrue(!rings[i].isValidRepairItem(stack, new ItemStack(Items.DIRT)), "Reject incorrect repair");
            if (repairs[i] != null)
                helper.assertTrue(
                        rings[i].isValidRepairItem(stack, new ItemStack(repairs[i])), "Source repair material");
            else
                helper.assertTrue(
                        !rings[i].isValidRepairItem(stack, new ItemStack(Items.IRON_INGOT)),
                        "No material repair for Mercury or Ancient Blessing");
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            var result = rings[i].use(player.level(), player, InteractionHand.MAIN_HAND);
            helper.assertTrue(
                    result.getResult().consumesAction()
                            && EquipmentRing.canEquip(
                                    player, 1, new ItemStack(PoptartRingItems.RUSTED_IRON_RING.get())),
                    "Right-click equips");
            var handler = new PlayerAccessoryHandler(player);
            helper.assertTrue(
                    handler.getStackInSlot(0).is(rings[i]) && !handler.isItemValid(1, new ItemStack(rings[i])),
                    "Ring slot and duplicate protection");
            EquipmentRing.hurt(player, player.damageSources().generic(), 4);
            helper.assertTrue(
                    handler.getStackInSlot(0).getDamageValue() == (durability[i] == 0 ? 0 : 1),
                    "Only damageable rings wear");
        }
        helper.assertTrue(
                new ItemStack(PoptartMaterialItems.CHTHONIC_CALCULUS.get()).getRarity()
                        == PoptartRarities.MALEVOLENT.getValue(),
                "Source malevolent rarity");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void afflictedShortensOnlyHarmfulEffects(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var handler = new PlayerAccessoryHandler(player);
        handler.setStackInSlot(0, new ItemStack(PoptartRingItems.RING_OF_THE_AFFLICTED.get()));
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100));
        helper.assertTrue(
                player.getEffect(MobEffects.POISON).getDuration() == 70, "Harmful duration reduced 30 percent");
        helper.assertTrue(player.getEffect(MobEffects.MOVEMENT_SPEED).getDuration() == 100, "Beneficial unchanged");
        player.removeAllEffects();
        handler.setStackInSlot(0, ItemStack.EMPTY);
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 100));
        helper.assertTrue(player.getEffect(MobEffects.POISON).getDuration() == 100, "Unequipped harmful unchanged");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void mercuryWitherUsesHungerBeforeDeath(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        new PlayerAccessoryHandler(player)
                .setStackInSlot(0, new ItemStack(PoptartRingItems.MERCURY_GEMSTONE_RING.get()));
        player.setHealth(20);
        player.hurt(player.damageSources().wither(), 10);
        helper.assertTrue(player.getHealth() == 13, "Actual wither damage reduced 30 percent");
        player.invulnerableTime = 0;
        player.setHealth(2);
        player.getFoodData().setFoodLevel(20);
        player.hurt(player.damageSources().wither(), 10);
        helper.assertTrue(
                player.getHealth() == 1 && player.getFoodData().getFoodLevel() == 14,
                "Six food points protect half a heart");
        player.invulnerableTime = 0;
        player.getFoodData().setFoodLevel(0);
        player.hurt(player.damageSources().wither(), 10);
        helper.assertTrue(!player.isAlive(), "No hunger means no immortality");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void ancientBlessingHealsWithoutDurability(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var handler = new PlayerAccessoryHandler(player);
        handler.setStackInSlot(0, new ItemStack(PoptartRingItems.ANCIENT_BLESSING.get()));
        player.setHealth(10);
        for (int i = 0; i < 399; i++)
            EquipmentRing.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
        helper.assertTrue(player.getHealth() == 10, "No early healing");
        EquipmentRing.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
        helper.assertTrue(
                player.getHealth() == 11 && handler.getStackInSlot(0).is(PoptartRingItems.ANCIENT_BLESSING.get()),
                "Half-heart heal every 400 ticks; remains equipped");
        handler.setStackInSlot(0, ItemStack.EMPTY);
        for (int i = 0; i < 400; i++) RingEffects.tick(player);
        helper.assertTrue(player.getHealth() == 11, "Healing stops when removed");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void sunlightChargeWakeAndRemoval(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var handler = new PlayerAccessoryHandler(player);
        var ring = new ItemStack(PoptartRingItems.SUNLIGHT_RING.get());
        handler.setStackInSlot(0, ring);
        RingEffects.tick(player);
        helper.assertTrue(
                player.getAbsorptionAmount() == 8 && player.getMaxAbsorption() == 8,
                "New ring grants four absorption hearts");
        player.hurt(player.damageSources().generic(), 3);
        RingEffects.tick(player);
        helper.assertTrue(player.getAbsorptionAmount() == 5, "Ticks do not refill spent absorption");
        var removed = handler.extractItem(0, 1, false);
        RingEffects.tick(player);
        helper.assertTrue(
                player.getMaxAbsorption() == 0 && player.getAbsorptionAmount() == 0, "Removal clears ring absorption");
        var saved = removed.save(player.registryAccess());
        handler.setStackInSlot(
                0, ItemStack.parseOptional(player.registryAccess(), (net.minecraft.nbt.CompoundTag) saved));
        RingEffects.tick(player);
        helper.assertTrue(player.getAbsorptionAmount() == 0, "Charge is saved; re-equipping cannot refill");
        NeoForge.EVENT_BUS.post(new PlayerWakeUpEvent(player, false, true));
        RingEffects.tick(player);
        helper.assertTrue(player.getAbsorptionAmount() == 8, "Wake event recharges equipped ring");
        player.setAbsorptionAmount(0);
        RingEffects.tick(player);
        helper.assertTrue(player.getMaxAbsorption() == 0, "Depleted ring removes its maximum absorption modifier");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void horseshoeBoostsAndHealsOnlyCurrentMount(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var horse = helper.spawn(EntityType.HORSE, 1, 1, 1);
        double speed = horse.getAttributeValue(Attributes.MOVEMENT_SPEED);
        horse.setHealth(horse.getMaxHealth() - 2);
        var handler = new PlayerAccessoryHandler(player);
        handler.setStackInSlot(0, new ItemStack(PoptartRingItems.TINY_HORSESHOE.get()));
        helper.assertTrue(player.startRiding(horse, true), "Mount test horse");
        player.tickCount = 199;
        RingEffects.tick(player);
        helper.assertTrue(
                Math.abs(horse.getAttributeValue(Attributes.MOVEMENT_SPEED) - speed * 1.2) < .000001,
                "Twenty percent mount speed");
        helper.assertTrue(horse.getHealth() == horse.getMaxHealth() - 2, "No early mount heal");
        player.tickCount = 200;
        RingEffects.tick(player);
        helper.assertTrue(horse.getHealth() == horse.getMaxHealth() - 1, "Mount heals one HP at ten seconds");
        handler.setStackInSlot(0, ItemStack.EMPTY);
        RingEffects.tick(player);
        helper.assertTrue(horse.getAttributeValue(Attributes.MOVEMENT_SPEED) == speed, "Unequipping removes speed");
        handler.setStackInSlot(0, new ItemStack(PoptartRingItems.TINY_HORSESHOE.get()));
        RingEffects.tick(player);
        player.stopRiding();
        RingEffects.tick(player);
        helper.assertTrue(horse.getAttributeValue(Attributes.MOVEMENT_SPEED) == speed, "Dismount removes speed");
        helper.succeed();
    }

    @GameTest(template = "ring_arena", timeoutTicks = 40)
    public static void malevolentLifestealAndHealthCost(GameTestHelper helper) {
        for (int x = 0; x < 21; x++) {
            for (int z = 0; z < 21; z++) helper.setBlock(x, 0, z, Blocks.STONE);
        }
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var handler = new PlayerAccessoryHandler(player);
        handler.setStackInSlot(0, new ItemStack(PoptartRingItems.MALEVOLENT_RING.get()));
        helper.assertTrue(player.getMaxHealth() == 16 && player.getHealth() == 16, "Two maximum hearts sacrificed");
        player.setHealth(10);
        var cow = helper.spawn(EntityType.COW, 10, 1, 10);
        cow.hurt(player.damageSources().playerAttack(player), 5);
        helper.assertTrue(player.getHealth() == 11, "Twenty percent actual damage healed");
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(cow.onGround(), "Fixture animal must settle onto the arena floor before navigation");
            player.setPos(cow.position().subtract(1, 0, 0));
            for (int i = 0; i < 10; i++) RingEffects.tick(player);
            helper.assertTrue(cow.getNavigation().getPath() != null, "Nearby animal receives escape path");
            helper.assertTrue(
                    cow.getNavigation().getPath().getTarget().getX() > cow.getX(),
                    "Escape path leads away from wearer");
            handler.setStackInSlot(0, ItemStack.EMPTY);
            helper.assertTrue(player.getMaxHealth() == 20, "Removal restores maximum hearts");
            cow.invulnerableTime = 0;
            cow.hurt(player.damageSources().playerAttack(player), 2);
            helper.assertTrue(player.getHealth() == 11, "Unequipped attacks do not heal");
            helper.succeed();
        });
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void thornPricksAndReflectsDamage(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(PoptartRingItems.THORN_RING.get()));
        PoptartRingItems.THORN_RING.get().use(player.level(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getHealth() == 19, "Right-click equip pricks half a heart");
        var cow = helper.spawn(EntityType.COW, 1, 1, 1);
        player.invulnerableTime = 0;
        player.hurt(player.damageSources().mobAttack(cow), 5);
        helper.assertTrue(cow.getHealth() == cow.getMaxHealth() - 3, "Physical hit reflects up to three HP");
        cow.invulnerableTime = 0;
        player.invulnerableTime = 0;
        player.hurt(player.damageSources().indirectMagic(cow, cow), 5);
        helper.assertTrue(cow.getHealth() == cow.getMaxHealth() - 4.5F, "Magical hit reflects up to one and a half HP");
        player.invulnerableTime = 0;
        float before = player.getHealth();
        var slot = player.inventoryMenu.slots.stream()
                .filter(s -> s instanceof AccessorySlot && s.getItem().is(PoptartRingItems.THORN_RING.get()))
                .findFirst()
                .orElseThrow();
        player.inventoryMenu.quickMoveStack(player, slot.index);
        helper.assertTrue(
                player.getHealth() == before - 1
                        && AccessoryInventory.get(player).getStackInSlot(0).isEmpty(),
                "Shift-click removal pricks once");
        helper.succeed();
    }
}
