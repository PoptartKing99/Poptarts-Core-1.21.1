package dev.poptartking.poptartcore.registry;

import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.MATERIALS;

import dev.poptartking.poptartcore.catalog.CatalogItemDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import dev.poptartking.poptartcore.patina.PatinaItem;
import dev.poptartking.poptartcore.wax.WaxItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class PoptartMaterialItems {
    public static final CatalogItemDefinition<Item> LIFESALT =
            PoptartCatalog.simpleItem("lifesalt").withoutGeneratedModel().creative(MATERIALS, 650);

    private PoptartMaterialItems() {}

    public static final CatalogItemDefinition<Item> STEEL_INGOT =
            PoptartCatalog.simpleItem("steel_ingot").creative(MATERIALS, 440);

    public static final CatalogItemDefinition<Item> LATEX_BOTTLE = PoptartCatalog.item(
                    "latex_bottle", () -> new Item(new Item.Properties().stacksTo(16)))
            .withoutGeneratedModel()
            .creative(MATERIALS, 530);

    public static final CatalogItemDefinition<Item> RUBBER =
            PoptartCatalog.simpleItem("rubber").withoutGeneratedModel().creative(MATERIALS, 540);

    public static final CatalogItemDefinition<Item> STEEL_NUGGET =
            PoptartCatalog.simpleItem("steel_nugget").creative(MATERIALS, 450);

    public static final CatalogItemDefinition<Item> STEEL_PLATE =
            PoptartCatalog.simpleItem("steel_plate").creative(MATERIALS, 460);

    public static final CatalogItemDefinition<Item> COAL_COKE =
            PoptartCatalog.simpleItem("coal_coke").fuelBurnTime(3200).creative(MATERIALS, 480);

    public static final CatalogItemDefinition<WaxItem> WAX =
            PoptartCatalog.item("wax", () -> new WaxItem(new Item.Properties())).creative(MATERIALS, 500);

    public static final CatalogItemDefinition<PatinaItem> PATINA = PoptartCatalog.item(
                    "patina", () -> new PatinaItem(new Item.Properties()))
            .withoutGeneratedModel()
            .creative(MATERIALS, 510);

    public static final CatalogItemDefinition<Item> REDSTONE_CIRCUIT =
            PoptartCatalog.simpleItem("redstone_circuit").creative(MATERIALS, 550);

    public static final CatalogItemDefinition<Item> AUDIO_CIRCUIT =
            PoptartCatalog.simpleItem("audio_circuit").creative(MATERIALS, 560);

    public static final CatalogItemDefinition<Item> VIDEO_CIRCUIT =
            PoptartCatalog.simpleItem("video_circuit").creative(MATERIALS, 570);

    public static final CatalogItemDefinition<Item> RADIO_CIRCUIT =
            PoptartCatalog.simpleItem("radio_circuit").creative(MATERIALS, 580);

    public static final CatalogItemDefinition<Item> AMETHYST_CRYSTAL =
            PoptartCatalog.simpleItem("amethyst_crystal").creative(MATERIALS, 590);

    public static final CatalogItemDefinition<Item> QUARTZ_CRYSTAL =
            PoptartCatalog.simpleItem("quartz_crystal").withoutGeneratedModel().creative(MATERIALS, 600);

    public static final CatalogItemDefinition<Item> BRONZE_INGOT =
            PoptartCatalog.simpleItem("bronze_ingot").creative(MATERIALS, 390);

    public static final CatalogItemDefinition<Item> BRONZE_NUGGET =
            PoptartCatalog.simpleItem("bronze_nugget").creative(MATERIALS, 400);

    public static final CatalogItemDefinition<Item> BRONZE_PLATE =
            PoptartCatalog.simpleItem("bronze_plate").creative(MATERIALS, 410);

    public static final CatalogItemDefinition<Item> TIN_INGOT =
            PoptartCatalog.simpleItem("tin_ingot").creative(MATERIALS, 50);

    public static final CatalogItemDefinition<Item> TIN_NUGGET =
            PoptartCatalog.simpleItem("tin_nugget").creative(MATERIALS, 60);

    public static final CatalogItemDefinition<Item> RAW_TIN =
            PoptartCatalog.simpleItem("raw_tin").creative(MATERIALS, 30);

    public static final CatalogItemDefinition<Item> LEAD_INGOT =
            PoptartCatalog.simpleItem("lead_ingot").creative(MATERIALS, 120);

    public static final CatalogItemDefinition<Item> LEAD_NUGGET =
            PoptartCatalog.simpleItem("lead_nugget").creative(MATERIALS, 130);

    public static final CatalogItemDefinition<Item> RAW_LEAD =
            PoptartCatalog.simpleItem("raw_lead").creative(MATERIALS, 100);

    public static final CatalogItemDefinition<Item> MAGNETITE =
            PoptartCatalog.simpleItem("magnetite").withoutGeneratedModel().creative(MATERIALS, 150);

    public static final CatalogItemDefinition<Item> CINNABAR =
            PoptartCatalog.simpleItem("cinnabar").withoutGeneratedModel().creative(MATERIALS, 170);

    public static final CatalogItemDefinition<Item> MERCURY_DROPLET =
            PoptartCatalog.simpleItem("mercury_droplet").withoutGeneratedModel().creative(MATERIALS, 190);

    public static final CatalogItemDefinition<Item> MERCURY_INGOT =
            PoptartCatalog.simpleItem("mercury_ingot").withoutGeneratedModel().creative(MATERIALS, 200);

    public static final CatalogItemDefinition<Item> SILVER_INGOT =
            PoptartCatalog.simpleItem("silver_ingot").creative(MATERIALS, 260);

    public static final CatalogItemDefinition<Item> SILVER_NUGGET =
            PoptartCatalog.simpleItem("silver_nugget").creative(MATERIALS, 270);

    public static final CatalogItemDefinition<Item> RAW_SILVER =
            PoptartCatalog.simpleItem("raw_silver").creative(MATERIALS, 240);

    public static final CatalogItemDefinition<Item> RAW_TITANIUM =
            PoptartCatalog.simpleItem("raw_titanium").creative(MATERIALS, 330);

    public static final CatalogItemDefinition<Item> TITANIUM_SHARD =
            PoptartCatalog.simpleItem("titanium_shard").creative(MATERIALS, 350);

    public static final CatalogItemDefinition<Item> TITANIUM_INGOT = PoptartCatalog.item(
                    "titanium_ingot", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)))
            .creative(MATERIALS, 360);

    public static final CatalogItemDefinition<Item> TITANIUM_NUGGET = PoptartCatalog.item(
                    "titanium_nugget", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)))
            .creative(MATERIALS, 370);

    public static final CatalogItemDefinition<Item> TITANIUM_PLATE = PoptartCatalog.item(
                    "titanium_plate", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)))
            .creative(MATERIALS, 380);

    public static final CatalogItemDefinition<Item> BONE_ASH =
            PoptartCatalog.simpleItem("bone_ash").withoutGeneratedModel().creative(MATERIALS, 610);
    public static final CatalogItemDefinition<Item> CHTHONIC_CALCULUS = PoptartCatalog.item(
                    "chthonic_calculus",
                    () -> new Item(new Item.Properties().rarity(PoptartRarities.MALEVOLENT.getValue())))
            .withoutGeneratedModel()
            .creative(MATERIALS, 620);

    public static void initialize() {}
}
