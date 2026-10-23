package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.beekeeping.BeekeeperArmorItem;
import dev.poptartking.poptartcore.catalog.CatalogItemDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

public final class PoptartArmorItems {
    private PoptartArmorItems() {}

    public static final CatalogItemDefinition<ArmorItem> MINING_HELMET = PoptartCatalog.item(
                    "mining_helmet",
                    () -> new ArmorItem(
                            PoptartCoreArmorMaterials.MINING_ARMOR_MATERIAL,
                            ArmorItem.Type.HELMET,
                            new Item.Properties().durability(100)))
            .tags(ItemTags.HEAD_ARMOR);

    public static final CatalogItemDefinition<ArmorItem> RAW_HIDE_HELMET = PoptartCatalog.item(
                    "raw_hide_helmet",
                    () -> new ArmorItem(
                            PoptartCoreArmorMaterials.RAW_HIDE_ARMOR_MATERIAL,
                            ArmorItem.Type.HELMET,
                            new Item.Properties().durability(140)))
            .tags(ItemTags.HEAD_ARMOR);

    public static final CatalogItemDefinition<ArmorItem> RAW_HIDE_CHESTPLATE = PoptartCatalog.item(
                    "raw_hide_chestplate",
                    () -> new ArmorItem(
                            PoptartCoreArmorMaterials.RAW_HIDE_ARMOR_MATERIAL,
                            ArmorItem.Type.CHESTPLATE,
                            new Item.Properties().durability(140)))
            .tags(ItemTags.CHEST_ARMOR);

    public static final CatalogItemDefinition<ArmorItem> RAW_HIDE_LEGGINGS = PoptartCatalog.item(
                    "raw_hide_leggings",
                    () -> new ArmorItem(
                            PoptartCoreArmorMaterials.RAW_HIDE_ARMOR_MATERIAL,
                            ArmorItem.Type.LEGGINGS,
                            new Item.Properties().durability(140)))
            .tags(ItemTags.LEG_ARMOR);

    public static final CatalogItemDefinition<BeekeeperArmorItem> BEEKEEPER_HELMET = PoptartCatalog.item(
                    "beekeeper_helmet",
                    () -> new BeekeeperArmorItem(ArmorItem.Type.HELMET, new Item.Properties().durability(200)))
            .withName("Beekeeper Hood")
            .tags(ItemTags.HEAD_ARMOR);

    public static final CatalogItemDefinition<BeekeeperArmorItem> BEEKEEPER_CHESTPLATE = PoptartCatalog.item(
                    "beekeeper_chestplate",
                    () -> new BeekeeperArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(300)))
            .withName("Beekeeper Tunic")
            .tags(ItemTags.CHEST_ARMOR);

    public static final CatalogItemDefinition<BeekeeperArmorItem> BEEKEEPER_LEGGINGS = PoptartCatalog.item(
                    "beekeeper_leggings",
                    () -> new BeekeeperArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties().durability(260)))
            .withName("Beekeeper Skirt")
            .tags(ItemTags.LEG_ARMOR);

    public static final CatalogItemDefinition<BeekeeperArmorItem> BEEKEEPER_BOOTS = PoptartCatalog.item(
                    "beekeeper_boots",
                    () -> new BeekeeperArmorItem(ArmorItem.Type.BOOTS, new Item.Properties().durability(200)))
            .tags(ItemTags.FOOT_ARMOR);

    public static final CatalogItemDefinition<ArmorItem> STEEL_HELMET = PoptartCatalog.item(
                    "steel_helmet",
                    () -> new ArmorItem(
                            PoptartCoreArmorMaterials.STEEL_ARMOR_MATERIAL,
                            ArmorItem.Type.HELMET,
                            new Item.Properties().durability(275)))
            .tags(ItemTags.HEAD_ARMOR);

    public static final CatalogItemDefinition<ArmorItem> STEEL_CHESTPLATE = PoptartCatalog.item(
                    "steel_chestplate",
                    () -> new ArmorItem(
                            PoptartCoreArmorMaterials.STEEL_ARMOR_MATERIAL,
                            ArmorItem.Type.CHESTPLATE,
                            new Item.Properties().durability(400)))
            .tags(ItemTags.CHEST_ARMOR);

    public static final CatalogItemDefinition<ArmorItem> STEEL_LEGGINGS = PoptartCatalog.item(
                    "steel_leggings",
                    () -> new ArmorItem(
                            PoptartCoreArmorMaterials.STEEL_ARMOR_MATERIAL,
                            ArmorItem.Type.LEGGINGS,
                            new Item.Properties().durability(375)))
            .tags(ItemTags.LEG_ARMOR);

    public static final CatalogItemDefinition<ArmorItem> STEEL_BOOTS = PoptartCatalog.item(
                    "steel_boots",
                    () -> new ArmorItem(
                            PoptartCoreArmorMaterials.STEEL_ARMOR_MATERIAL,
                            ArmorItem.Type.BOOTS,
                            new Item.Properties().durability(325)))
            .tags(ItemTags.FOOT_ARMOR);

    public static void initialize() {}
}
