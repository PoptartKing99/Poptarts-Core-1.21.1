package dev.poptartking.poptartcore.beekeeping;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.PlayerAccessoryHandler;
import dev.poptartking.poptartcore.registry.PoptartArmorItems;
import dev.poptartking.poptartcore.registry.PoptartRingItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BeekeeperArmorGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void armorProtectionAndMaterialRemainUnchanged(GameTestHelper helper) {
        ArmorItem[] armor = {
            PoptartArmorItems.BEEKEEPER_HELMET.get(), PoptartArmorItems.BEEKEEPER_CHESTPLATE.get(),
            PoptartArmorItems.BEEKEEPER_LEGGINGS.get(), PoptartArmorItems.BEEKEEPER_BOOTS.get()
        };
        int[] protection = {1, 3, 2, 1};
        for (int i = 0; i < armor.length; i++) {
            var material = armor[i].getMaterial().value();
            helper.assertTrue(armor[i].getDefense() == protection[i], "Original armor protection for piece " + i);
            helper.assertTrue(material.equipSound().equals(SoundEvents.ARMOR_EQUIP_LEATHER), "Leather equip sound");
            helper.assertTrue(
                    material.toughness() == 0 && material.knockbackResistance() == 0, "No extra armor bonuses");
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void arthropodDamageReductionScalesWithPieces(GameTestHelper helper) {
        for (int pieces = 0; pieces <= 4; pieces++) {
            for (var type : new EntityType<?>[] {EntityType.CAVE_SPIDER, EntityType.ZOMBIE}) {
                var player = helper.makeMockPlayer(GameType.SURVIVAL);
                equip(player, pieces);
                var source = player.damageSources()
                        .mobAttack((net.minecraft.world.entity.LivingEntity) type.create(helper.getLevel()));
                var event = new net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent(
                        player, new net.neoforged.neoforge.common.damagesource.DamageContainer(source, 10));
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);
                float expected = type == EntityType.CAVE_SPIDER ? 10 * (1 - .05F * pieces) : 10;
                helper.assertTrue(
                        Math.abs(event.getAmount() - expected) < .0001,
                        "Only arthropod damage reduced for " + pieces + " pieces");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void actualBeeStingsRespectProtectionAndWear(GameTestHelper helper) {
        for (int pieces = 0; pieces <= 4; pieces++) {
            for (long seed : new long[] {0, 4096, 12345, 987654321}) {
                var player = helper.makeMockPlayer(GameType.SURVIVAL);
                equip(player, pieces);
                player.getRandom().setSeed(seed);
                boolean blocked = pieces > 0 && player.getRandom().nextFloat() < .25F * pieces;
                player.getRandom().setSeed(seed);
                var bee = EntityType.BEE.create(helper.getLevel());
                boolean attacked = bee.doHurtTarget(player);
                helper.assertTrue(attacked != blocked, "Bee sting result for " + pieces + " pieces and seed " + seed);
                if (blocked) {
                    helper.assertTrue(
                            player.getHealth() == player.getMaxHealth() && !bee.hasStung(),
                            "Blocked sting must hurt neither player nor bee");
                    for (var stack : player.getArmorSlots()) {
                        if (!stack.isEmpty())
                            helper.assertTrue(stack.getDamageValue() == 1, "Blocked sting wears each armor piece once");
                    }
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void beekeeperArmorPreservesInfinitePoison(GameTestHelper helper) {
        var spider = EntityType.CAVE_SPIDER.create(helper.getLevel());
        for (int pieces = 1; pieces <= 4; pieces++) {
            for (boolean withRing : new boolean[] {false, true}) {
                var player = helper.makeMockPlayer(GameType.SURVIVAL);
                equip(player, pieces);
                if (withRing) {
                    new PlayerAccessoryHandler(player)
                            .setStackInSlot(0, new ItemStack(PoptartRingItems.RING_OF_THE_AFFLICTED.get()));
                }
                player.addEffect(new MobEffectInstance(MobEffects.POISON, MobEffectInstance.INFINITE_DURATION), spider);
                var poison = player.getEffect(MobEffects.POISON);
                helper.assertTrue(
                        poison != null && poison.isInfiniteDuration(),
                        "Arthropod poison stays infinite for every armor count, with or without the ring");
                helper.assertTrue(
                        poison.tick(player, () -> {}) && poison.isInfiniteDuration(),
                        "Infinite poison remains active after ticking");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void beekeeperArmorKeepsTimedPoisonReduction(GameTestHelper helper) {
        var spider = EntityType.CAVE_SPIDER.create(helper.getLevel());
        int[] expectedDurations = {128, 108, 89, 70, 51};
        for (int pieces = 0; pieces <= 4; pieces++) {
            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            equip(player, pieces);
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 128), spider);
            helper.assertTrue(
                    player.getEffect(MobEffects.POISON).getDuration() == expectedDurations[pieces],
                    "Timed arthropod poison keeps its existing reduction for " + pieces + " pieces");
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void beekeeperArmorOnlyShortensArthropodPoison(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        equip(player, 4);
        var zombie = EntityType.ZOMBIE.create(helper.getLevel());
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 128), zombie);
        helper.assertTrue(player.getEffect(MobEffects.POISON).getDuration() == 128, "Other sources are unchanged");
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 128));
        helper.assertTrue(player.getEffect(MobEffects.POISON).getDuration() == 128, "Unattributed poison is unchanged");

        var spider = EntityType.CAVE_SPIDER.create(helper.getLevel());
        player.addEffect(new MobEffectInstance(MobEffects.WITHER, 128), spider);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 128), spider);
        helper.assertTrue(
                player.getEffect(MobEffects.WITHER).getDuration() == 128, "Other harmful effects are unchanged");
        helper.assertTrue(
                player.getEffect(MobEffects.MOVEMENT_SPEED).getDuration() == 128, "Beneficial effects are unchanged");
        helper.succeed();
    }

    private static void equip(Player player, int pieces) {
        Item[] armor = {
            PoptartArmorItems.BEEKEEPER_HELMET.get(),
            PoptartArmorItems.BEEKEEPER_CHESTPLATE.get(),
            PoptartArmorItems.BEEKEEPER_LEGGINGS.get(),
            PoptartArmorItems.BEEKEEPER_BOOTS.get()
        };
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int piece = 0; piece < pieces; piece++) player.setItemSlot(slots[piece], new ItemStack(armor[piece]));
    }
}
