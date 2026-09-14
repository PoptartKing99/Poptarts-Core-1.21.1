package dev.poptartking.poptartcore.catalog;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PoptartCatalogGameTests {
    private PoptartCatalogGameTests() {}

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void ordinaryMetalIngredientsUseCatalogRegistration(GameTestHelper helper) {
        Map<String, String> expectedNames = Map.ofEntries(
                Map.entry("mining_helmet", "Mining Helmet"),
                Map.entry("raw_hide_helmet", "Raw Hide Helmet"),
                Map.entry("raw_hide_chestplate", "Raw Hide Chestplate"),
                Map.entry("raw_hide_leggings", "Raw Hide Leggings"),
                Map.entry("beekeeper_helmet", "Beekeeper Hood"),
                Map.entry("beekeeper_chestplate", "Beekeeper Tunic"),
                Map.entry("beekeeper_leggings", "Beekeeper Skirt"),
                Map.entry("beekeeper_boots", "Beekeeper Boots"),
                Map.entry("steel_helmet", "Steel Helmet"),
                Map.entry("steel_chestplate", "Steel Chestplate"),
                Map.entry("steel_leggings", "Steel Leggings"),
                Map.entry("steel_boots", "Steel Boots"),
                Map.entry("steel_ingot", "Steel Ingot"),
                Map.entry("steel_nugget", "Steel Nugget"),
                Map.entry("steel_plate", "Steel Plate"),
                Map.entry("coal_coke", "Coal Coke"),
                Map.entry("wax", "Wax"),
                Map.entry("redstone_circuit", "Redstone Circuit"),
                Map.entry("firestarter", "Firestarter"),
                Map.entry("bee_smoker", "Bee Smoker"),
                Map.entry("repeating_crossbow", "Repeating Crossbow"),
                Map.entry("hammer", "Hammer"),
                Map.entry("unfired_ingot_mould", "Unfired Ingot Mould"),
                Map.entry("ingot_mould", "Ingot Mould"),
                Map.entry("unfired_plate_mould", "Unfired Plate Mould"),
                Map.entry("plate_mould", "Plate Mould"),
                Map.entry("bronze_ingot", "Bronze Ingot"),
                Map.entry("bronze_nugget", "Bronze Nugget"),
                Map.entry("bronze_plate", "Bronze Plate"),
                Map.entry("bronze_sword", "Bronze Sword"),
                Map.entry("bronze_pickaxe", "Bronze Pickaxe"),
                Map.entry("bronze_axe", "Bronze Axe"),
                Map.entry("bronze_shovel", "Bronze Shovel"),
                Map.entry("bronze_knife", "Bronze Knife"),
                Map.entry("steel_sword", "Steel Sword"),
                Map.entry("steel_pickaxe", "Steel Pickaxe"),
                Map.entry("steel_axe", "Steel Axe"),
                Map.entry("steel_shovel", "Steel Shovel"),
                Map.entry("steel_knife", "Steel Knife"),
                Map.entry("bone_pick", "Bone Pick"),
                Map.entry("flint_axe", "Flint Axe"),
                Map.entry("flint_shovel", "Flint Shovel"),
                Map.entry("tin_ingot", "Tin Ingot"),
                Map.entry("tin_nugget", "Tin Nugget"),
                Map.entry("raw_tin", "Raw Tin"),
                Map.entry("lead_ingot", "Lead Ingot"),
                Map.entry("lead_nugget", "Lead Nugget"),
                Map.entry("raw_lead", "Raw Lead"),
                Map.entry("silver_ingot", "Silver Ingot"),
                Map.entry("silver_nugget", "Silver Nugget"),
                Map.entry("raw_silver", "Raw Silver"));
        Set<String> handheldItems = Set.of(
                "bronze_sword",
                "bronze_pickaxe",
                "bronze_axe",
                "bronze_shovel",
                "bronze_knife",
                "steel_sword",
                "steel_pickaxe",
                "steel_axe",
                "steel_shovel",
                "steel_knife",
                "bone_pick",
                "flint_axe",
                "flint_shovel",
                "hammer");
        Set<String> customModelItems = Set.of("bee_smoker", "repeating_crossbow");

        helper.assertTrue(
                PoptartCatalog.items().size() == expectedNames.size(),
                "Poptart Catalog does not contain exactly the expected metal ingredients");
        for (CatalogItemDefinition<?> definition : PoptartCatalog.items()) {
            helper.assertTrue(
                    expectedNames.containsKey(definition.id()), "Unexpected catalog item: " + definition.id());
            helper.assertTrue(
                    expectedNames.get(definition.id()).equals(definition.displayName()),
                    "Catalog has the wrong name for " + definition.id());
            CatalogItemModel expectedModel = customModelItems.contains(definition.id())
                    ? CatalogItemModel.CUSTOM
                    : handheldItems.contains(definition.id()) ? CatalogItemModel.HANDHELD : CatalogItemModel.GENERATED;
            helper.assertTrue(
                    definition.model() == expectedModel, "Catalog has the wrong model type for " + definition.id());
            helper.assertTrue(
                    PoptartCore.location(definition.id()).equals(BuiltInRegistries.ITEM.getKey(definition.get())),
                    "Catalog item has the wrong registry ID: " + definition.id());
        }
        Map<String, String> expectedBlockNames = Map.of(
                "tin_block", "Block of Tin",
                "lead_block", "Block of Lead",
                "silver_block", "Block of Silver",
                "wax_block", "Wax Block",
                "coal_coke_block", "Coal Coke Block",
                "bronze_block", "Block of Bronze",
                "steel_block", "Block of Steel");
        helper.assertTrue(
                PoptartCatalog.blocks().size() == expectedBlockNames.size(),
                "Poptart Catalog does not contain exactly the expected storage blocks");
        for (CatalogBlockDefinition<?> definition : PoptartCatalog.blocks()) {
            helper.assertTrue(
                    expectedBlockNames.get(definition.id()).equals(definition.displayName()),
                    "Catalog has the wrong block name for " + definition.id());
            helper.assertTrue(
                    PoptartCore.location(definition.id()).equals(BuiltInRegistries.BLOCK.getKey(definition.get())),
                    "Catalog block has the wrong registry ID: " + definition.id());
            helper.assertTrue(
                    PoptartCore.location(definition.id())
                            .equals(BuiltInRegistries.ITEM.getKey(
                                    definition.item().get())),
                    "Catalog block item has the wrong registry ID: " + definition.id());
            helper.assertTrue(
                    definition.storageRecipes() != null,
                    "Catalog storage block is missing its packing recipes: " + definition.id());
        }
        helper.assertTrue(
                PoptartCoreBlocks.WAX_BLOCK.item().get().getBlock() == PoptartCoreBlocks.WAX_BLOCK.get(),
                "Catalog block item does not point to its registered block");
        helper.assertTrue(
                PoptartCoreItems.TIN_NUGGET.item().get() == PoptartCoreItems.TIN_NUGGET.get(),
                "Catalog definition does not return its registered item");
        helper.assertTrue(
                PoptartCoreItems.INGOT_MOULD.get().getDefaultInstance().getMaxDamage() == 32
                        && PoptartCoreItems.PLATE_MOULD
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 32,
                "Catalog moulds do not preserve their durability");
        helper.assertTrue(
                PoptartCoreItems.WAX.get() instanceof dev.poptartking.poptartcore.wax.WaxItem,
                "Catalog wax does not preserve its custom item class");
        helper.assertTrue(
                PoptartCoreItems.FIRESTARTER.get().getDefaultInstance().getMaxDamage() == 3,
                "Catalog firestarter does not preserve its durability");
        helper.assertTrue(
                PoptartCoreItems.HAMMER.get().getDefaultInstance().getMaxDamage() == 720
                        && PoptartCoreItems.HAMMER.get().getDefaultMaxStackSize() == 1,
                "Catalog hammer does not preserve its durability and stack size");
        helper.assertTrue(
                PoptartCoreItems.BEE_SMOKER.get().getDefaultInstance().getMaxDamage() == 360
                        && PoptartCoreItems.BEE_SMOKER.get().getDefaultMaxStackSize() == 1,
                "Catalog Bee Smoker does not preserve its durability and stack size");
        helper.assertTrue(
                PoptartCoreItems.REPEATING_CROSSBOW.get().getDefaultInstance().getMaxDamage() == 300
                        && PoptartCoreItems.REPEATING_CROSSBOW.get().getDefaultMaxStackSize() == 1
                        && net.minecraft.world.item.component.ChargedProjectiles.EMPTY.equals(
                                PoptartCoreItems.REPEATING_CROSSBOW
                                        .get()
                                        .getDefaultInstance()
                                        .get(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES)),
                "Catalog Repeating Crossbow does not preserve its durability, stack size, and charge data");
        helper.assertTrue(
                PoptartCoreItems.MINING_HELMET.get().getType() == net.minecraft.world.item.ArmorItem.Type.HELMET
                        && PoptartCoreItems.MINING_HELMET.get().getMaterial()
                                == dev.poptartking.poptartcore.registry.PoptartCoreArmorMaterials.MINING_ARMOR_MATERIAL
                        && PoptartCoreItems.MINING_HELMET
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 100,
                "Catalog Mining Helmet does not preserve its slot, material, and durability");
        helper.assertTrue(
                PoptartCoreItems.RAW_HIDE_HELMET.get().getType() == net.minecraft.world.item.ArmorItem.Type.HELMET
                        && PoptartCoreItems.RAW_HIDE_CHESTPLATE.get().getType()
                                == net.minecraft.world.item.ArmorItem.Type.CHESTPLATE
                        && PoptartCoreItems.RAW_HIDE_LEGGINGS.get().getType()
                                == net.minecraft.world.item.ArmorItem.Type.LEGGINGS
                        && PoptartCoreItems.RAW_HIDE_HELMET.get().getMaterial()
                                == dev.poptartking.poptartcore.registry.PoptartCoreArmorMaterials
                                        .RAW_HIDE_ARMOR_MATERIAL
                        && PoptartCoreItems.RAW_HIDE_CHESTPLATE.get().getMaterial()
                                == dev.poptartking.poptartcore.registry.PoptartCoreArmorMaterials
                                        .RAW_HIDE_ARMOR_MATERIAL
                        && PoptartCoreItems.RAW_HIDE_LEGGINGS.get().getMaterial()
                                == dev.poptartking.poptartcore.registry.PoptartCoreArmorMaterials
                                        .RAW_HIDE_ARMOR_MATERIAL
                        && PoptartCoreItems.RAW_HIDE_HELMET
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 140
                        && PoptartCoreItems.RAW_HIDE_CHESTPLATE
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 140
                        && PoptartCoreItems.RAW_HIDE_LEGGINGS
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 140,
                "Catalog Raw Hide armor does not preserve its slots, material, and durability");
        helper.assertTrue(
                PoptartCoreItems.BEEKEEPER_HELMET.get().getType() == net.minecraft.world.item.ArmorItem.Type.HELMET
                        && PoptartCoreItems.BEEKEEPER_CHESTPLATE.get().getType()
                                == net.minecraft.world.item.ArmorItem.Type.CHESTPLATE
                        && PoptartCoreItems.BEEKEEPER_LEGGINGS.get().getType()
                                == net.minecraft.world.item.ArmorItem.Type.LEGGINGS
                        && PoptartCoreItems.BEEKEEPER_BOOTS.get().getType()
                                == net.minecraft.world.item.ArmorItem.Type.BOOTS
                        && PoptartCoreItems.BEEKEEPER_HELMET
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 200
                        && PoptartCoreItems.BEEKEEPER_CHESTPLATE
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 300
                        && PoptartCoreItems.BEEKEEPER_LEGGINGS
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 260
                        && PoptartCoreItems.BEEKEEPER_BOOTS
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 200,
                "Catalog Beekeeper armor does not preserve its slots and durability");
        helper.assertTrue(
                PoptartCoreItems.STEEL_HELMET.get().getType() == net.minecraft.world.item.ArmorItem.Type.HELMET
                        && PoptartCoreItems.STEEL_CHESTPLATE.get().getType()
                                == net.minecraft.world.item.ArmorItem.Type.CHESTPLATE
                        && PoptartCoreItems.STEEL_LEGGINGS.get().getType()
                                == net.minecraft.world.item.ArmorItem.Type.LEGGINGS
                        && PoptartCoreItems.STEEL_BOOTS.get().getType() == net.minecraft.world.item.ArmorItem.Type.BOOTS
                        && PoptartCoreItems.STEEL_HELMET.get().getMaterial()
                                == dev.poptartking.poptartcore.registry.PoptartCoreArmorMaterials.STEEL_ARMOR_MATERIAL
                        && PoptartCoreItems.STEEL_CHESTPLATE
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 400
                        && PoptartCoreItems.STEEL_LEGGINGS
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 375
                        && PoptartCoreItems.STEEL_BOOTS
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 325
                        && PoptartCoreItems.STEEL_HELMET
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 275,
                "Catalog Steel armor does not preserve its slots, material, and durability");
        helper.succeed();
    }
}
