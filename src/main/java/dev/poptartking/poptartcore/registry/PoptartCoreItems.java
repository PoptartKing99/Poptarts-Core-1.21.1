package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.crossbow.RepeatingCrossbowItem;
import dev.poptartking.poptartcore.crucible.CrucibleBlockItem;
import dev.poptartking.poptartcore.hammer.HammerItem;
import dev.poptartking.poptartcore.tool.BonePickItem;
import dev.poptartking.poptartcore.wax.WaxItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.common.item.KnifeItem;

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
    public static final DeferredItem<SwordItem> STEEL_SWORD = ITEMS.register(
            "steel_sword",
            () -> new SwordItem(
                    PoptartCoreTiers.STEEL,
                    new Item.Properties().attributes(SwordItem.createAttributes(PoptartCoreTiers.STEEL, 2, -2.4F))));
    public static final DeferredItem<PickaxeItem> STEEL_PICKAXE = ITEMS.register(
            "steel_pickaxe",
            () -> new PickaxeItem(
                    PoptartCoreTiers.STEEL,
                    new Item.Properties()
                            .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.STEEL, 0.0F, -2.8F))));
    public static final DeferredItem<AxeItem> STEEL_AXE = ITEMS.register(
            "steel_axe",
            () -> new AxeItem(
                    PoptartCoreTiers.STEEL,
                    new Item.Properties().attributes(AxeItem.createAttributes(PoptartCoreTiers.STEEL, 5.0F, -3.1F))));
    public static final DeferredItem<ShovelItem> STEEL_SHOVEL = ITEMS.register(
            "steel_shovel",
            () -> new ShovelItem(
                    PoptartCoreTiers.STEEL,
                    new Item.Properties()
                            .attributes(ShovelItem.createAttributes(PoptartCoreTiers.STEEL, 0.5F, -3.0F))));
    public static final DeferredItem<KnifeItem> STEEL_KNIFE = ITEMS.register(
            "steel_knife",
            () -> new KnifeItem(
                    PoptartCoreTiers.STEEL,
                    new Item.Properties()
                            .durability(1250)
                            .attributes(KnifeItem.createAttributes(PoptartCoreTiers.STEEL, 1.0F, -2.0F))));
    public static final DeferredItem<Item> COAL_COKE = registerItem("coal_coke");
    public static final DeferredItem<WaxItem> WAX = ITEMS.register("wax", () -> new WaxItem(new Item.Properties()));
    public static final DeferredItem<Item> REDSTONE_CIRCUIT = registerItem("redstone_circuit");
    public static final DeferredItem<BonePickItem> BONE_PICK = ITEMS.register(
            "bone_pick",
            () -> new BonePickItem(
                    PoptartCoreTiers.BONE,
                    new Item.Properties()
                            .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.BONE, 2.0F, -2.8F))));
    public static final DeferredItem<AxeItem> FLINT_AXE = ITEMS.register(
            "flint_axe",
            () -> new AxeItem(
                    PoptartCoreTiers.FLINT,
                    new Item.Properties()
                            .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.FLINT, 3.0F, -3.1F))));
    public static final DeferredItem<ShovelItem> FLINT_SHOVEL = ITEMS.register(
            "flint_shovel",
            () -> new ShovelItem(
                    PoptartCoreTiers.FLINT,
                    new Item.Properties()
                            .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.FLINT, 0.5F, -3.0F))));
    public static final DeferredItem<FlintAndSteelItem> FIRESTARTER =
            ITEMS.register("firestarter", () -> new FlintAndSteelItem(new Item.Properties().durability(3)));
    public static final DeferredItem<RepeatingCrossbowItem> REPEATING_CROSSBOW = ITEMS.register(
            "repeating_crossbow",
            () -> new RepeatingCrossbowItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(300)
                    .component(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY)));
    public static final DeferredItem<HammerItem> HAMMER = ITEMS.register(
            "hammer",
            () -> new HammerItem(new Item.Properties()
                    .stacksTo(1)
                    .attributes(HammerItem.createAttributes())
                    .component(DataComponents.TOOL, HammerItem.createToolProperties())
                    .durability(720)));
    public static final DeferredItem<Item> BRONZE_INGOT = registerItem("bronze_ingot");
    public static final DeferredItem<Item> BRONZE_NUGGET = registerItem("bronze_nugget");
    public static final DeferredItem<Item> BRONZE_PLATE = registerItem("bronze_plate");
    public static final DeferredItem<SwordItem> BRONZE_SWORD = ITEMS.register(
            "bronze_sword",
            () -> new SwordItem(
                    PoptartCoreTiers.BRONZE,
                    new Item.Properties().attributes(SwordItem.createAttributes(PoptartCoreTiers.BRONZE, 2, -2.4F))));
    public static final DeferredItem<PickaxeItem> BRONZE_PICKAXE = ITEMS.register(
            "bronze_pickaxe",
            () -> new PickaxeItem(
                    PoptartCoreTiers.BRONZE,
                    new Item.Properties()
                            .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.BRONZE, 0.0F, -2.8F))));
    public static final DeferredItem<AxeItem> BRONZE_AXE = ITEMS.register(
            "bronze_axe",
            () -> new AxeItem(
                    PoptartCoreTiers.BRONZE,
                    new Item.Properties().attributes(AxeItem.createAttributes(PoptartCoreTiers.BRONZE, 5.0F, -3.1F))));
    public static final DeferredItem<ShovelItem> BRONZE_SHOVEL = ITEMS.register(
            "bronze_shovel",
            () -> new ShovelItem(
                    PoptartCoreTiers.BRONZE,
                    new Item.Properties()
                            .attributes(ShovelItem.createAttributes(PoptartCoreTiers.BRONZE, 0.5F, -3.0F))));
    public static final DeferredItem<KnifeItem> BRONZE_KNIFE = ITEMS.register(
            "bronze_knife",
            () -> new KnifeItem(
                    PoptartCoreTiers.BRONZE,
                    new Item.Properties()
                            .durability(275)
                            .attributes(KnifeItem.createAttributes(PoptartCoreTiers.BRONZE, 1.0F, -2.0F))));
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
    public static final DeferredItem<BlockItem> WORKBENCH = registerBlockItem("workbench", PoptartCoreBlocks.WORKBENCH);
    public static final DeferredItem<BlockItem> QUERN = registerBlockItem("quern", PoptartCoreBlocks.QUERN);
    public static final DeferredItem<BlockItem> PORTABLE_ENGINE =
            registerBlockItem("portable_engine", PoptartCoreBlocks.PORTABLE_ENGINE);
    public static final DeferredItem<dev.poptartking.poptartcore.millstone.MillstoneBlockItem> MILLSTONE =
            ITEMS.register(
                    "millstone",
                    () -> new dev.poptartking.poptartcore.millstone.MillstoneBlockItem(
                            PoptartCoreBlocks.MILLSTONE.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> IRON_BLOOM =
            registerBlockItem("iron_bloom", PoptartCoreBlocks.IRON_BLOOM);
    public static final DeferredItem<BlockItem> TIN_BLOCK = registerBlockItem("tin_block", PoptartCoreBlocks.TIN_BLOCK);
    public static final DeferredItem<BlockItem> TIN_ORE = registerBlockItem("tin_ore", PoptartCoreBlocks.TIN_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_TIN_ORE =
            registerBlockItem("deepslate_tin_ore", PoptartCoreBlocks.DEEPSLATE_TIN_ORE);
    public static final DeferredItem<BlockItem> RAW_TIN_BLOCK =
            registerBlockItem("raw_tin_block", PoptartCoreBlocks.RAW_TIN_BLOCK);
    public static final DeferredItem<BlockItem> LEAD_BLOCK =
            registerBlockItem("lead_block", PoptartCoreBlocks.LEAD_BLOCK);
    public static final DeferredItem<BlockItem> LEAD_ORE = registerBlockItem("lead_ore", PoptartCoreBlocks.LEAD_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_LEAD_ORE =
            registerBlockItem("deepslate_lead_ore", PoptartCoreBlocks.DEEPSLATE_LEAD_ORE);
    public static final DeferredItem<BlockItem> RAW_LEAD_BLOCK =
            registerBlockItem("raw_lead_block", PoptartCoreBlocks.RAW_LEAD_BLOCK);
    public static final DeferredItem<BlockItem> SILVER_BLOCK =
            registerBlockItem("silver_block", PoptartCoreBlocks.SILVER_BLOCK);
    public static final DeferredItem<BlockItem> SILVER_ORE =
            registerBlockItem("silver_ore", PoptartCoreBlocks.SILVER_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_SILVER_ORE =
            registerBlockItem("deepslate_silver_ore", PoptartCoreBlocks.DEEPSLATE_SILVER_ORE);
    public static final DeferredItem<BlockItem> RAW_SILVER_BLOCK =
            registerBlockItem("raw_silver_block", PoptartCoreBlocks.RAW_SILVER_BLOCK);
    public static final DeferredItem<BlockItem> WAX_BLOCK = registerBlockItem("wax_block", PoptartCoreBlocks.WAX_BLOCK);
    public static final DeferredItem<BlockItem> COAL_COKE_BLOCK =
            registerBlockItem("coal_coke_block", PoptartCoreBlocks.COAL_COKE_BLOCK);
    public static final DeferredItem<BlockItem> BRONZE_BLOCK =
            registerBlockItem("bronze_block", PoptartCoreBlocks.BRONZE_BLOCK);
    public static final DeferredItem<BlockItem> STEEL_BLOCK =
            registerBlockItem("steel_block", PoptartCoreBlocks.STEEL_BLOCK);
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
