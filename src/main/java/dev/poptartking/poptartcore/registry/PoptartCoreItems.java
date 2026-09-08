package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.crucible.CrucibleBlockItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PoptartCoreItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PoptartCore.MOD_ID);

    // Items
    public static final DeferredItem<ArmorItem> MINING_HELMET = ITEMS.register(
            "mining_helmet",
            () -> new ArmorItem(
                    PoptartCoreArmorMaterials.MINING_ARMOR_MATERIAL,
                    ArmorItem.Type.HELMET,
                    new Item.Properties().durability(100)));
    public static final DeferredItem<ArmorItem> RAW_HIDE_HELMET = ITEMS.register(
            "raw_hide_helmet",
            () -> new ArmorItem(
                    PoptartCoreArmorMaterials.RAW_HIDE_ARMOR_MATERIAL,
                    ArmorItem.Type.HELMET,
                    new Item.Properties().durability(140)));
    public static final DeferredItem<ArmorItem> RAW_HIDE_CHESTPLATE = ITEMS.register(
            "raw_hide_chestplate",
            () -> new ArmorItem(
                    PoptartCoreArmorMaterials.RAW_HIDE_ARMOR_MATERIAL,
                    ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(140)));
    public static final DeferredItem<ArmorItem> RAW_HIDE_LEGGINGS = ITEMS.register(
            "raw_hide_leggings",
            () -> new ArmorItem(
                    PoptartCoreArmorMaterials.RAW_HIDE_ARMOR_MATERIAL,
                    ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(140)));
    public static final DeferredItem<Item> STEEL_INGOT = registerItem("steel_ingot");
    public static final DeferredItem<Item> STEEL_NUGGET = registerItem("steel_nugget");
    public static final DeferredItem<Item> STEEL_PLATE = registerItem("steel_plate");
    public static final DeferredItem<Item> COAL_COKE = registerItem("coal_coke");
    public static final DeferredItem<Item> BRONZE_INGOT = registerItem("bronze_ingot");
    public static final DeferredItem<Item> BRONZE_NUGGET = registerItem("bronze_nugget");
    public static final DeferredItem<Item> BRONZE_PLATE = registerItem("bronze_plate");
    public static final DeferredItem<Item> TIN_INGOT = registerItem("tin_ingot");
    public static final DeferredItem<Item> TIN_NUGGET = registerItem("tin_nugget");
    public static final DeferredItem<Item> RAW_TIN = registerItem("raw_tin");
    public static final DeferredItem<Item> LEAD_INGOT = registerItem("lead_ingot");
    public static final DeferredItem<Item> LEAD_NUGGET = registerItem("lead_nugget");
    public static final DeferredItem<Item> RAW_LEAD = registerItem("raw_lead");
    public static final DeferredItem<Item> SILVER_INGOT = registerItem("silver_ingot");
    public static final DeferredItem<Item> SILVER_NUGGET = registerItem("silver_nugget");
    public static final DeferredItem<Item> RAW_SILVER = registerItem("raw_silver");
    public static final DeferredItem<ArmorItem> STEEL_HELMET = ITEMS.register(
            "steel_helmet",
            () -> new ArmorItem(
                    PoptartCoreArmorMaterials.STEEL_ARMOR_MATERIAL,
                    ArmorItem.Type.HELMET,
                    new Item.Properties().durability(275)));
    public static final DeferredItem<ArmorItem> STEEL_CHESTPLATE = ITEMS.register(
            "steel_chestplate",
            () -> new ArmorItem(
                    PoptartCoreArmorMaterials.STEEL_ARMOR_MATERIAL,
                    ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(400)));
    public static final DeferredItem<ArmorItem> STEEL_LEGGINGS = ITEMS.register(
            "steel_leggings",
            () -> new ArmorItem(
                    PoptartCoreArmorMaterials.STEEL_ARMOR_MATERIAL,
                    ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(375)));
    public static final DeferredItem<ArmorItem> STEEL_BOOTS = ITEMS.register(
            "steel_boots",
            () -> new ArmorItem(
                    PoptartCoreArmorMaterials.STEEL_ARMOR_MATERIAL,
                    ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(325)));
    public static final DeferredItem<CrucibleBlockItem> CRUCIBLE = ITEMS.register(
            "crucible", () -> new CrucibleBlockItem(PoptartCoreBlocks.CRUCIBLE.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> BLAST_FURNACE = ITEMS.register(
            "blast_furnace", () -> new BlockItem(PoptartCoreBlocks.BLAST_FURNACE.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> BLOOMERY = registerBlockItem("bloomery", PoptartCoreBlocks.BLOOMERY);
    public static final DeferredItem<BlockItem> IRON_BLOOM =
            registerBlockItem("iron_bloom", PoptartCoreBlocks.IRON_BLOOM);
    public static final DeferredItem<BlockItem> CLINKER_BRICKS =
            registerBlockItem("clinker_bricks", PoptartCoreBlocks.CLINKER_BRICKS);
    public static final DeferredItem<BlockItem> CLINKER_BRICK_SLAB =
            registerBlockItem("clinker_brick_slab", PoptartCoreBlocks.CLINKER_BRICK_SLAB);
    public static final DeferredItem<BlockItem> CLINKER_BRICK_STAIRS =
            registerBlockItem("clinker_brick_stairs", PoptartCoreBlocks.CLINKER_BRICK_STAIRS);
    public static final DeferredItem<BlockItem> CLINKER_BRICK_WALL =
            registerBlockItem("clinker_brick_wall", PoptartCoreBlocks.CLINKER_BRICK_WALL);
    public static final DeferredItem<BlockItem> CLINKER_TILE =
            registerBlockItem("clinker_tile", PoptartCoreBlocks.CLINKER_TILE);
    public static final DeferredItem<BlockItem> CLINKER_TILE_SLAB =
            registerBlockItem("clinker_tile_slab", PoptartCoreBlocks.CLINKER_TILE_SLAB);
    public static final DeferredItem<BlockItem> CLINKER_TILE_STAIRS =
            registerBlockItem("clinker_tile_stairs", PoptartCoreBlocks.CLINKER_TILE_STAIRS);
    public static final DeferredItem<BlockItem> CLINKER_TILE_WALL =
            registerBlockItem("clinker_tile_wall", PoptartCoreBlocks.CLINKER_TILE_WALL);
    public static final DeferredItem<BlockItem> MOSAIC_CLINKER_TILE =
            registerBlockItem("mosaic_clinker_tile", PoptartCoreBlocks.MOSAIC_CLINKER_TILE);
    public static final DeferredItem<BlockItem> CHISELED_CLINKER_TILE =
            registerBlockItem("chiseled_clinker_tile", PoptartCoreBlocks.CHISELED_CLINKER_TILE);
    public static final DeferredItem<BlockItem> CLINKER_PILLAR =
            registerBlockItem("clinker_pillar", PoptartCoreBlocks.CLINKER_PILLAR);
    public static final DeferredItem<Item> UNFIRED_INGOT_MOULD =
            ITEMS.register("unfired_ingot_mould", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> INGOT_MOULD =
            ITEMS.register("ingot_mould", () -> new Item(new Item.Properties().durability(32)));
    public static final DeferredItem<Item> UNFIRED_PLATE_MOULD =
            ITEMS.register("unfired_plate_mould", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PLATE_MOULD =
            ITEMS.register("plate_mould", () -> new Item(new Item.Properties().durability(32)));

    // Helper Functions
    private static DeferredItem<Item> registerItem(String name) {
        return registerItem(name, new Item.Properties());
    }

    private static DeferredItem<Item> registerItem(String name, Item.Properties properties) {
        return ITEMS.registerSimpleItem(name, properties);
    }

    private static DeferredItem<BlockItem> registerBlockItem(
            String name,
            net.neoforged.neoforge.registries.DeferredBlock<? extends net.minecraft.world.level.block.Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static boolean isMould(ItemStack stack) {
        return stack.is(INGOT_MOULD.get()) || stack.is(PLATE_MOULD.get());
    }

    // Registration
    public static void register(IEventBus eventBus) {
        ITEMS.addAlias(PoptartCore.location("steel_sheet"), PoptartCore.location("steel_plate"));
        ITEMS.addAlias(PoptartCore.location("bronze_sheet"), PoptartCore.location("bronze_plate"));
        ITEMS.register(eventBus);
    }
}
