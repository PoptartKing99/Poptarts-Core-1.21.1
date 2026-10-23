package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.catalog.CatalogItemDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import dev.poptartking.poptartcore.patina.PatinaItem;
import dev.poptartking.poptartcore.wax.WaxItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public final class PoptartMaterialItems {
    public static final CatalogItemDefinition<Item> LIFESALT =
            PoptartCatalog.simpleItem("lifesalt").withoutGeneratedModel();

    private PoptartMaterialItems() {}

    public static final CatalogItemDefinition<Item> STEEL_INGOT = PoptartCatalog.simpleItem("steel_ingot");

    public static final CatalogItemDefinition<Item> LATEX_BOTTLE = PoptartCatalog.item(
                    "latex_bottle", () -> new Item(new Item.Properties().stacksTo(16)))
            .withoutGeneratedModel();

    public static final CatalogItemDefinition<Item> RUBBER =
            PoptartCatalog.simpleItem("rubber").withoutGeneratedModel();

    public static final CatalogItemDefinition<Item> STEEL_NUGGET = PoptartCatalog.simpleItem("steel_nugget");

    public static final CatalogItemDefinition<Item> STEEL_PLATE = PoptartCatalog.simpleItem("steel_plate");

    public static final CatalogItemDefinition<Item> COAL_COKE =
            PoptartCatalog.simpleItem("coal_coke").fuelBurnTime(3200);

    public static final CatalogItemDefinition<WaxItem> WAX =
            PoptartCatalog.item("wax", () -> new WaxItem(new Item.Properties()));

    public static final CatalogItemDefinition<PatinaItem> PATINA = PoptartCatalog.item(
                    "patina", () -> new PatinaItem(new Item.Properties()))
            .withoutGeneratedModel();

    public static final CatalogItemDefinition<Item> REDSTONE_CIRCUIT = PoptartCatalog.simpleItem("redstone_circuit");

    public static final CatalogItemDefinition<Item> AUDIO_CIRCUIT = PoptartCatalog.simpleItem("audio_circuit");

    public static final CatalogItemDefinition<Item> VIDEO_CIRCUIT = PoptartCatalog.simpleItem("video_circuit");

    public static final CatalogItemDefinition<Item> RADIO_CIRCUIT = PoptartCatalog.simpleItem("radio_circuit");

    public static final CatalogItemDefinition<Item> AMETHYST_CRYSTAL = PoptartCatalog.simpleItem("amethyst_crystal");

    public static final CatalogItemDefinition<Item> BRONZE_INGOT = PoptartCatalog.simpleItem("bronze_ingot");

    public static final CatalogItemDefinition<Item> BRONZE_NUGGET = PoptartCatalog.simpleItem("bronze_nugget");

    public static final CatalogItemDefinition<Item> BRONZE_PLATE = PoptartCatalog.simpleItem("bronze_plate");

    public static final CatalogItemDefinition<Item> TIN_INGOT = PoptartCatalog.simpleItem("tin_ingot");

    public static final CatalogItemDefinition<Item> TIN_NUGGET = PoptartCatalog.simpleItem("tin_nugget");

    public static final CatalogItemDefinition<Item> RAW_TIN = PoptartCatalog.simpleItem("raw_tin");

    public static final CatalogItemDefinition<Item> LEAD_INGOT = PoptartCatalog.simpleItem("lead_ingot");

    public static final CatalogItemDefinition<Item> LEAD_NUGGET = PoptartCatalog.simpleItem("lead_nugget");

    public static final CatalogItemDefinition<Item> RAW_LEAD = PoptartCatalog.simpleItem("raw_lead");

    public static final CatalogItemDefinition<Item> MAGNETITE =
            PoptartCatalog.simpleItem("magnetite").withoutGeneratedModel();

    public static final CatalogItemDefinition<Item> CINNABAR =
            PoptartCatalog.simpleItem("cinnabar").withoutGeneratedModel();

    public static final CatalogItemDefinition<Item> MERCURY_DROPLET =
            PoptartCatalog.simpleItem("mercury_droplet").withoutGeneratedModel();

    public static final CatalogItemDefinition<Item> MERCURY_INGOT =
            PoptartCatalog.simpleItem("mercury_ingot").withoutGeneratedModel();

    public static final CatalogItemDefinition<Item> SILVER_INGOT = PoptartCatalog.simpleItem("silver_ingot");

    public static final CatalogItemDefinition<Item> SILVER_NUGGET = PoptartCatalog.simpleItem("silver_nugget");

    public static final CatalogItemDefinition<Item> RAW_SILVER = PoptartCatalog.simpleItem("raw_silver");

    public static final CatalogItemDefinition<Item> RAW_TITANIUM = PoptartCatalog.simpleItem("raw_titanium");

    public static final CatalogItemDefinition<Item> TITANIUM_SHARD = PoptartCatalog.simpleItem("titanium_shard");

    public static final CatalogItemDefinition<Item> TITANIUM_INGOT =
            PoptartCatalog.item("titanium_ingot", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final CatalogItemDefinition<Item> TITANIUM_NUGGET =
            PoptartCatalog.item("titanium_nugget", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final CatalogItemDefinition<Item> TITANIUM_PLATE =
            PoptartCatalog.item("titanium_plate", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final CatalogItemDefinition<Item> BONE_ASH =
            PoptartCatalog.simpleItem("bone_ash").withoutGeneratedModel();
    public static final CatalogItemDefinition<Item> CHTHONIC_CALCULUS = PoptartCatalog.item(
                    "chthonic_calculus",
                    () -> new Item(new Item.Properties().rarity(PoptartRarities.MALEVOLENT.getValue())))
            .withoutGeneratedModel();

    public static void initialize() {}
}
