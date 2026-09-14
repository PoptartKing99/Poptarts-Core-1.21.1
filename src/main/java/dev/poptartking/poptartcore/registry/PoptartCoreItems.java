package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.beekeeping.BeeSmokerItem;
import dev.poptartking.poptartcore.beekeeping.BeekeeperArmorItem;
import dev.poptartking.poptartcore.catalog.CatalogItemDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
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
    public static final CatalogItemDefinition<ArmorItem> MINING_HELMET = PoptartCatalog.item(
            "mining_helmet",
            () -> new ArmorItem(
                    PoptartCoreArmorMaterials.MINING_ARMOR_MATERIAL,
                    ArmorItem.Type.HELMET,
                    new Item.Properties().durability(100)));
    public static final CatalogItemDefinition<ArmorItem> RAW_HIDE_HELMET = PoptartCatalog.item(
            "raw_hide_helmet",
            () -> new ArmorItem(
                    PoptartCoreArmorMaterials.RAW_HIDE_ARMOR_MATERIAL,
                    ArmorItem.Type.HELMET,
                    new Item.Properties().durability(140)));
    public static final CatalogItemDefinition<ArmorItem> RAW_HIDE_CHESTPLATE = PoptartCatalog.item(
            "raw_hide_chestplate",
            () -> new ArmorItem(
                    PoptartCoreArmorMaterials.RAW_HIDE_ARMOR_MATERIAL,
                    ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(140)));
    public static final CatalogItemDefinition<ArmorItem> RAW_HIDE_LEGGINGS = PoptartCatalog.item(
            "raw_hide_leggings",
            () -> new ArmorItem(
                    PoptartCoreArmorMaterials.RAW_HIDE_ARMOR_MATERIAL,
                    ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(140)));
    public static final CatalogItemDefinition<Item> STEEL_INGOT = PoptartCatalog.simpleItem("steel_ingot");
    public static final CatalogItemDefinition<Item> STEEL_NUGGET = PoptartCatalog.simpleItem("steel_nugget");
    public static final CatalogItemDefinition<Item> STEEL_PLATE = PoptartCatalog.simpleItem("steel_plate");
    public static final CatalogItemDefinition<SwordItem> STEEL_SWORD = PoptartCatalog.item(
                    "steel_sword",
                    () -> new SwordItem(
                            PoptartCoreTiers.STEEL,
                            new Item.Properties()
                                    .attributes(SwordItem.createAttributes(PoptartCoreTiers.STEEL, 2, -2.4F))))
            .handheld();
    public static final CatalogItemDefinition<PickaxeItem> STEEL_PICKAXE = PoptartCatalog.item(
                    "steel_pickaxe",
                    () -> new PickaxeItem(
                            PoptartCoreTiers.STEEL,
                            new Item.Properties()
                                    .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.STEEL, 0.0F, -2.8F))))
            .handheld();
    public static final CatalogItemDefinition<AxeItem> STEEL_AXE = PoptartCatalog.item(
                    "steel_axe",
                    () -> new AxeItem(
                            PoptartCoreTiers.STEEL,
                            new Item.Properties()
                                    .attributes(AxeItem.createAttributes(PoptartCoreTiers.STEEL, 5.0F, -3.1F))))
            .handheld();
    public static final CatalogItemDefinition<ShovelItem> STEEL_SHOVEL = PoptartCatalog.item(
                    "steel_shovel",
                    () -> new ShovelItem(
                            PoptartCoreTiers.STEEL,
                            new Item.Properties()
                                    .attributes(ShovelItem.createAttributes(PoptartCoreTiers.STEEL, 0.5F, -3.0F))))
            .handheld();
    public static final CatalogItemDefinition<KnifeItem> STEEL_KNIFE = PoptartCatalog.item(
                    "steel_knife",
                    () -> new KnifeItem(
                            PoptartCoreTiers.STEEL,
                            new Item.Properties()
                                    .attributes(KnifeItem.createAttributes(PoptartCoreTiers.STEEL, 1.0F, -2.0F))))
            .handheld();
    public static final CatalogItemDefinition<Item> COAL_COKE = PoptartCatalog.simpleItem("coal_coke");
    public static final CatalogItemDefinition<WaxItem> WAX =
            PoptartCatalog.item("wax", () -> new WaxItem(new Item.Properties()));
    public static final CatalogItemDefinition<Item> REDSTONE_CIRCUIT = PoptartCatalog.simpleItem("redstone_circuit");
    public static final CatalogItemDefinition<BonePickItem> BONE_PICK = PoptartCatalog.item(
                    "bone_pick",
                    () -> new BonePickItem(
                            PoptartCoreTiers.BONE,
                            new Item.Properties()
                                    .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.BONE, 2.0F, -2.8F))))
            .handheld();
    public static final CatalogItemDefinition<AxeItem> FLINT_AXE = PoptartCatalog.item(
                    "flint_axe",
                    () -> new AxeItem(
                            PoptartCoreTiers.FLINT,
                            new Item.Properties()
                                    .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.FLINT, 3.0F, -3.1F))))
            .handheld();
    public static final CatalogItemDefinition<ShovelItem> FLINT_SHOVEL = PoptartCatalog.item(
                    "flint_shovel",
                    () -> new ShovelItem(
                            PoptartCoreTiers.FLINT,
                            new Item.Properties()
                                    .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.FLINT, 0.5F, -3.0F))))
            .handheld();
    public static final CatalogItemDefinition<FlintAndSteelItem> FIRESTARTER =
            PoptartCatalog.item("firestarter", () -> new FlintAndSteelItem(new Item.Properties().durability(3)));
    public static final CatalogItemDefinition<BeeSmokerItem> BEE_SMOKER = PoptartCatalog.item(
                    "bee_smoker",
                    () -> new BeeSmokerItem(new Item.Properties().stacksTo(1).durability(360)))
            .withoutGeneratedModel();
    public static final CatalogItemDefinition<BeekeeperArmorItem> BEEKEEPER_HELMET = PoptartCatalog.item(
                    "beekeeper_helmet",
                    () -> new BeekeeperArmorItem(ArmorItem.Type.HELMET, new Item.Properties().durability(200)))
            .withName("Beekeeper Hood");
    public static final CatalogItemDefinition<BeekeeperArmorItem> BEEKEEPER_CHESTPLATE = PoptartCatalog.item(
                    "beekeeper_chestplate",
                    () -> new BeekeeperArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(300)))
            .withName("Beekeeper Tunic");
    public static final CatalogItemDefinition<BeekeeperArmorItem> BEEKEEPER_LEGGINGS = PoptartCatalog.item(
                    "beekeeper_leggings",
                    () -> new BeekeeperArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties().durability(260)))
            .withName("Beekeeper Skirt");
    public static final CatalogItemDefinition<BeekeeperArmorItem> BEEKEEPER_BOOTS = PoptartCatalog.item(
            "beekeeper_boots",
            () -> new BeekeeperArmorItem(ArmorItem.Type.BOOTS, new Item.Properties().durability(200)));
    public static final CatalogItemDefinition<RepeatingCrossbowItem> REPEATING_CROSSBOW = PoptartCatalog.item(
                    "repeating_crossbow",
                    () -> new RepeatingCrossbowItem(new Item.Properties()
                            .stacksTo(1)
                            .durability(300)
                            .component(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY)))
            .withoutGeneratedModel();
    public static final CatalogItemDefinition<HammerItem> HAMMER = PoptartCatalog.item(
                    "hammer",
                    () -> new HammerItem(new Item.Properties()
                            .stacksTo(1)
                            .attributes(HammerItem.createAttributes())
                            .component(DataComponents.TOOL, HammerItem.createToolProperties())
                            .durability(720)))
            .handheld();
    public static final CatalogItemDefinition<Item> BRONZE_INGOT = PoptartCatalog.simpleItem("bronze_ingot");
    public static final CatalogItemDefinition<Item> BRONZE_NUGGET = PoptartCatalog.simpleItem("bronze_nugget");
    public static final CatalogItemDefinition<Item> BRONZE_PLATE = PoptartCatalog.simpleItem("bronze_plate");
    public static final CatalogItemDefinition<SwordItem> BRONZE_SWORD = PoptartCatalog.item(
                    "bronze_sword",
                    () -> new SwordItem(
                            PoptartCoreTiers.BRONZE,
                            new Item.Properties()
                                    .attributes(SwordItem.createAttributes(PoptartCoreTiers.BRONZE, 2, -2.4F))))
            .handheld();
    public static final CatalogItemDefinition<PickaxeItem> BRONZE_PICKAXE = PoptartCatalog.item(
                    "bronze_pickaxe",
                    () -> new PickaxeItem(
                            PoptartCoreTiers.BRONZE,
                            new Item.Properties()
                                    .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.BRONZE, 0.0F, -2.8F))))
            .handheld();
    public static final CatalogItemDefinition<AxeItem> BRONZE_AXE = PoptartCatalog.item(
                    "bronze_axe",
                    () -> new AxeItem(
                            PoptartCoreTiers.BRONZE,
                            new Item.Properties()
                                    .attributes(AxeItem.createAttributes(PoptartCoreTiers.BRONZE, 5.0F, -3.1F))))
            .handheld();
    public static final CatalogItemDefinition<ShovelItem> BRONZE_SHOVEL = PoptartCatalog.item(
                    "bronze_shovel",
                    () -> new ShovelItem(
                            PoptartCoreTiers.BRONZE,
                            new Item.Properties()
                                    .attributes(ShovelItem.createAttributes(PoptartCoreTiers.BRONZE, 0.5F, -3.0F))))
            .handheld();
    public static final CatalogItemDefinition<KnifeItem> BRONZE_KNIFE = PoptartCatalog.item(
                    "bronze_knife",
                    () -> new KnifeItem(
                            PoptartCoreTiers.BRONZE,
                            new Item.Properties()
                                    .attributes(KnifeItem.createAttributes(PoptartCoreTiers.BRONZE, 1.0F, -2.0F))))
            .handheld();
    public static final CatalogItemDefinition<Item> TIN_INGOT = PoptartCatalog.simpleItem("tin_ingot");
    public static final CatalogItemDefinition<Item> TIN_NUGGET = PoptartCatalog.simpleItem("tin_nugget");
    public static final CatalogItemDefinition<Item> RAW_TIN = PoptartCatalog.simpleItem("raw_tin");
    public static final CatalogItemDefinition<Item> LEAD_INGOT = PoptartCatalog.simpleItem("lead_ingot");
    public static final CatalogItemDefinition<Item> LEAD_NUGGET = PoptartCatalog.simpleItem("lead_nugget");
    public static final CatalogItemDefinition<Item> RAW_LEAD = PoptartCatalog.simpleItem("raw_lead");
    public static final CatalogItemDefinition<Item> SILVER_INGOT = PoptartCatalog.simpleItem("silver_ingot");
    public static final CatalogItemDefinition<Item> SILVER_NUGGET = PoptartCatalog.simpleItem("silver_nugget");
    public static final CatalogItemDefinition<Item> RAW_SILVER = PoptartCatalog.simpleItem("raw_silver");
    public static final CatalogItemDefinition<ArmorItem> STEEL_HELMET = PoptartCatalog.item(
            "steel_helmet",
            () -> new ArmorItem(
                    PoptartCoreArmorMaterials.STEEL_ARMOR_MATERIAL,
                    ArmorItem.Type.HELMET,
                    new Item.Properties().durability(275)));
    public static final CatalogItemDefinition<ArmorItem> STEEL_CHESTPLATE = PoptartCatalog.item(
            "steel_chestplate",
            () -> new ArmorItem(
                    PoptartCoreArmorMaterials.STEEL_ARMOR_MATERIAL,
                    ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(400)));
    public static final CatalogItemDefinition<ArmorItem> STEEL_LEGGINGS = PoptartCatalog.item(
            "steel_leggings",
            () -> new ArmorItem(
                    PoptartCoreArmorMaterials.STEEL_ARMOR_MATERIAL,
                    ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(375)));
    public static final CatalogItemDefinition<ArmorItem> STEEL_BOOTS = PoptartCatalog.item(
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
    public static final DeferredItem<BlockItem> CLINKER_BRICKS = PoptartCoreBlocks.CLINKER_BRICKS.item();
    public static final DeferredItem<BlockItem> CLINKER_BRICK_SLAB = PoptartCoreBlocks.CLINKER_BRICK_SLAB.item();
    public static final DeferredItem<BlockItem> CLINKER_BRICK_STAIRS = PoptartCoreBlocks.CLINKER_BRICK_STAIRS.item();
    public static final DeferredItem<BlockItem> CLINKER_BRICK_WALL = PoptartCoreBlocks.CLINKER_BRICK_WALL.item();
    public static final DeferredItem<BlockItem> CLINKER_TILE = PoptartCoreBlocks.CLINKER_TILE.item();
    public static final DeferredItem<BlockItem> CLINKER_TILE_SLAB = PoptartCoreBlocks.CLINKER_TILE_SLAB.item();
    public static final DeferredItem<BlockItem> CLINKER_TILE_STAIRS = PoptartCoreBlocks.CLINKER_TILE_STAIRS.item();
    public static final DeferredItem<BlockItem> CLINKER_TILE_WALL = PoptartCoreBlocks.CLINKER_TILE_WALL.item();
    public static final DeferredItem<BlockItem> MOSAIC_CLINKER_TILE = PoptartCoreBlocks.MOSAIC_CLINKER_TILE.item();
    public static final DeferredItem<BlockItem> CHISELED_CLINKER_TILE = PoptartCoreBlocks.CHISELED_CLINKER_TILE.item();
    public static final DeferredItem<BlockItem> CLINKER_PILLAR = PoptartCoreBlocks.CLINKER_PILLAR.item();
    public static final CatalogItemDefinition<Item> UNFIRED_INGOT_MOULD =
            PoptartCatalog.simpleItem("unfired_ingot_mould");
    public static final CatalogItemDefinition<Item> INGOT_MOULD =
            PoptartCatalog.item("ingot_mould", () -> new Item(new Item.Properties().durability(32)));
    public static final CatalogItemDefinition<Item> UNFIRED_PLATE_MOULD =
            PoptartCatalog.simpleItem("unfired_plate_mould");
    public static final CatalogItemDefinition<Item> PLATE_MOULD =
            PoptartCatalog.item("plate_mould", () -> new Item(new Item.Properties().durability(32)));

    // Helper Functions
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
