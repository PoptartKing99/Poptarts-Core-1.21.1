package dev.poptartking.poptartcore.registry;

import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.TOOLS;

import dev.poptartking.poptartcore.beekeeping.BeeSmokerItem;
import dev.poptartking.poptartcore.catalog.CatalogItemDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import dev.poptartking.poptartcore.crossbow.RepeatingCrossbowItem;
import dev.poptartking.poptartcore.hammer.HammerItem;
import dev.poptartking.poptartcore.hammer.SledgehammerItem;
import dev.poptartking.poptartcore.lostheart.HeartCrystalItem;
import dev.poptartking.poptartcore.scribing.ScribingToolsItem;
import dev.poptartking.poptartcore.summoning.AllayCallingBellItem;
import dev.poptartking.poptartcore.summoning.VexCallingBellItem;
import dev.poptartking.poptartcore.tool.BonePickItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.ChargedProjectiles;
import vectorwing.farmersdelight.common.item.KnifeItem;

public final class PoptartToolItems {
    private PoptartToolItems() {}

    public static final CatalogItemDefinition<Item> DEPTH_METER =
            PoptartCatalog.simpleItem("depth_meter").withoutGeneratedModel().creative(TOOLS, 140);

    public static final CatalogItemDefinition<AllayCallingBellItem> ALLAY_CALLING_BELL = PoptartCatalog.item(
                    "allay_calling_bell", () -> new AllayCallingBellItem(new Item.Properties().stacksTo(1), 0, 3))
            .withName("Allay-Calling Bell")
            .handheld()
            .creative(TOOLS, 640);

    public static final CatalogItemDefinition<VexCallingBellItem> VEX_CALLING_BELL = PoptartCatalog.item(
                    "vex_calling_bell", () -> new VexCallingBellItem(new Item.Properties().stacksTo(1), 0, 5))
            .withName("Vex-Calling Bell")
            .handheld()
            .creative(TOOLS, 650);

    public static final CatalogItemDefinition<ScribingToolsItem> SCRIBING_TOOLS = PoptartCatalog.item(
                    "scribing_tools", () -> new ScribingToolsItem(new Item.Properties().durability(25)))
            .withoutGeneratedModel()
            .creative(TOOLS, 160);

    public static final CatalogItemDefinition<Item> LIFEGEM = PoptartCatalog.item(
                    "lifegem", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)))
            .withoutGeneratedModel()
            .creative(TOOLS, 560);

    public static final CatalogItemDefinition<HeartCrystalItem> HEART_CRYSTAL = PoptartCatalog.item(
                    "heart_crystal",
                    () -> new HeartCrystalItem(
                            new Item.Properties().durability(4).rarity(Rarity.RARE)))
            .withoutGeneratedModel()
            .creative(TOOLS, 570);

    public static final CatalogItemDefinition<SwordItem> STEEL_SWORD = PoptartCatalog.item(
                    "steel_sword",
                    () -> new SwordItem(
                            PoptartCoreTiers.STEEL,
                            new Item.Properties()
                                    .attributes(SwordItem.createAttributes(PoptartCoreTiers.STEEL, 2, -2.4F))))
            .handheld()
            .tags(ItemTags.SWORDS)
            .creative(TOOLS, 320);

    public static final CatalogItemDefinition<PickaxeItem> STEEL_PICKAXE = PoptartCatalog.item(
                    "steel_pickaxe",
                    () -> new PickaxeItem(
                            PoptartCoreTiers.STEEL,
                            new Item.Properties()
                                    .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.STEEL, 0.0F, -2.8F))))
            .handheld()
            .tags(ItemTags.PICKAXES)
            .creative(TOOLS, 330);

    public static final CatalogItemDefinition<AxeItem> STEEL_AXE = PoptartCatalog.item(
                    "steel_axe",
                    () -> new AxeItem(
                            PoptartCoreTiers.STEEL,
                            new Item.Properties()
                                    .attributes(AxeItem.createAttributes(PoptartCoreTiers.STEEL, 5.0F, -3.1F))))
            .handheld()
            .tags(ItemTags.AXES)
            .creative(TOOLS, 340);

    public static final CatalogItemDefinition<ShovelItem> STEEL_SHOVEL = PoptartCatalog.item(
                    "steel_shovel",
                    () -> new ShovelItem(
                            PoptartCoreTiers.STEEL,
                            new Item.Properties()
                                    .attributes(ShovelItem.createAttributes(PoptartCoreTiers.STEEL, 0.5F, -3.0F))))
            .handheld()
            .tags(ItemTags.SHOVELS)
            .creative(TOOLS, 350);

    public static final CatalogItemDefinition<KnifeItem> STEEL_KNIFE = PoptartCatalog.item(
                    "steel_knife",
                    () -> new KnifeItem(
                            PoptartCoreTiers.STEEL,
                            new Item.Properties()
                                    .attributes(KnifeItem.createAttributes(PoptartCoreTiers.STEEL, 1.0F, -2.0F))))
            .handheld()
            .tags(PoptartCoreTags.KNIVES)
            .creative(TOOLS, 360);

    public static final CatalogItemDefinition<SwordItem> RUINED_SWORD = PoptartCatalog.item(
                    "ruined_sword",
                    () -> new SwordItem(
                            PoptartCoreTiers.RUINED,
                            new Item.Properties()
                                    .attributes(SwordItem.createAttributes(PoptartCoreTiers.RUINED, 4.5F, -2.4F))))
            .handheld()
            .tags(ItemTags.SWORDS)
            .creative(TOOLS, 220);

    public static final CatalogItemDefinition<PickaxeItem> RUINED_PICKAXE = PoptartCatalog.item(
                    "ruined_pickaxe",
                    () -> new PickaxeItem(
                            PoptartCoreTiers.RUINED,
                            new Item.Properties()
                                    .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.RUINED, 2.5F, -2.8F))))
            .handheld()
            .tags(ItemTags.PICKAXES)
            .creative(TOOLS, 230);

    public static final CatalogItemDefinition<AxeItem> RUINED_AXE = PoptartCatalog.item(
                    "ruined_axe",
                    () -> new AxeItem(
                            PoptartCoreTiers.RUINED,
                            new Item.Properties()
                                    .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.RUINED, 7.5F, -3.1F))))
            .handheld()
            .tags(ItemTags.AXES)
            .creative(TOOLS, 240);

    public static final CatalogItemDefinition<ShovelItem> RUINED_SHOVEL = PoptartCatalog.item(
                    "ruined_shovel",
                    () -> new ShovelItem(
                            PoptartCoreTiers.RUINED,
                            new Item.Properties()
                                    .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.RUINED, 3.0F, -3.0F))))
            .handheld()
            .tags(ItemTags.SHOVELS)
            .creative(TOOLS, 250);

    public static final CatalogItemDefinition<KnifeItem> RUINED_KNIFE = PoptartCatalog.item(
                    "ruined_knife",
                    () -> new KnifeItem(
                            PoptartCoreTiers.RUINED,
                            new Item.Properties()
                                    .attributes(KnifeItem.createAttributes(PoptartCoreTiers.RUINED, 2.5F, -2.0F))))
            .handheld()
            .tags(PoptartCoreTags.KNIVES)
            .creative(TOOLS, 260);

    public static final CatalogItemDefinition<BonePickItem> BONE_PICK = PoptartCatalog.item(
                    "bone_pick",
                    () -> new BonePickItem(
                            PoptartCoreTiers.BONE,
                            new Item.Properties()
                                    .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.BONE, 2.0F, -2.8F))))
            .handheld()
            .tags(ItemTags.PICKAXES, ItemTags.HOES)
            .creative(TOOLS, 170);

    public static final CatalogItemDefinition<AxeItem> FLINT_AXE = PoptartCatalog.item(
                    "flint_axe",
                    () -> new AxeItem(
                            PoptartCoreTiers.FLINT,
                            new Item.Properties()
                                    .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.FLINT, 3.0F, -3.1F))))
            .handheld()
            .tags(ItemTags.AXES)
            .creative(TOOLS, 180);

    public static final CatalogItemDefinition<ShovelItem> FLINT_SHOVEL = PoptartCatalog.item(
                    "flint_shovel",
                    () -> new ShovelItem(
                            PoptartCoreTiers.FLINT,
                            new Item.Properties()
                                    .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.FLINT, 0.5F, -3.0F))))
            .handheld()
            .tags(ItemTags.SHOVELS)
            .creative(TOOLS, 190);

    public static final CatalogItemDefinition<FlintAndSteelItem> FIRESTARTER = PoptartCatalog.item(
                    "firestarter", () -> new FlintAndSteelItem(new Item.Properties().durability(3)))
            .creative(TOOLS, 150);

    public static final CatalogItemDefinition<BeeSmokerItem> BEE_SMOKER = PoptartCatalog.item(
                    "bee_smoker",
                    () -> new BeeSmokerItem(new Item.Properties().stacksTo(1).durability(360)))
            .withoutGeneratedModel()
            .creative(TOOLS, 380);

    public static final CatalogItemDefinition<RepeatingCrossbowItem> REPEATING_CROSSBOW = PoptartCatalog.item(
                    "repeating_crossbow",
                    () -> new RepeatingCrossbowItem(new Item.Properties()
                            .stacksTo(1)
                            .durability(300)
                            .component(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY)))
            .withoutGeneratedModel()
            .creative(TOOLS, 370);

    public static final CatalogItemDefinition<HammerItem> HAMMER = PoptartCatalog.item(
                    "hammer",
                    () -> new HammerItem(new Item.Properties()
                            .stacksTo(1)
                            .attributes(HammerItem.createAttributes())
                            .component(DataComponents.TOOL, HammerItem.createToolProperties())
                            .durability(720)))
            .handheld()
            .creative(TOOLS, 200);

    public static final CatalogItemDefinition<SledgehammerItem> SLEDGEHAMMER = PoptartCatalog.item(
                    "sledgehammer",
                    () -> new SledgehammerItem(new Item.Properties()
                            .stacksTo(1)
                            .attributes(SledgehammerItem.createSledgehammerAttributes())
                            .component(DataComponents.TOOL, HammerItem.createToolProperties())
                            .durability(1000)))
            .withoutGeneratedModel()
            .creative(TOOLS, 210);

    public static final CatalogItemDefinition<SwordItem> BRONZE_SWORD = PoptartCatalog.item(
                    "bronze_sword",
                    () -> new SwordItem(
                            PoptartCoreTiers.BRONZE,
                            new Item.Properties()
                                    .attributes(SwordItem.createAttributes(PoptartCoreTiers.BRONZE, 2, -2.4F))))
            .handheld()
            .tags(ItemTags.SWORDS)
            .creative(TOOLS, 270);

    public static final CatalogItemDefinition<PickaxeItem> BRONZE_PICKAXE = PoptartCatalog.item(
                    "bronze_pickaxe",
                    () -> new PickaxeItem(
                            PoptartCoreTiers.BRONZE,
                            new Item.Properties()
                                    .attributes(PickaxeItem.createAttributes(PoptartCoreTiers.BRONZE, 0.0F, -2.8F))))
            .handheld()
            .tags(ItemTags.PICKAXES)
            .creative(TOOLS, 280);

    public static final CatalogItemDefinition<AxeItem> BRONZE_AXE = PoptartCatalog.item(
                    "bronze_axe",
                    () -> new AxeItem(
                            PoptartCoreTiers.BRONZE,
                            new Item.Properties()
                                    .attributes(AxeItem.createAttributes(PoptartCoreTiers.BRONZE, 5.0F, -3.1F))))
            .handheld()
            .tags(ItemTags.AXES)
            .creative(TOOLS, 290);

    public static final CatalogItemDefinition<ShovelItem> BRONZE_SHOVEL = PoptartCatalog.item(
                    "bronze_shovel",
                    () -> new ShovelItem(
                            PoptartCoreTiers.BRONZE,
                            new Item.Properties()
                                    .attributes(ShovelItem.createAttributes(PoptartCoreTiers.BRONZE, 0.5F, -3.0F))))
            .handheld()
            .tags(ItemTags.SHOVELS)
            .creative(TOOLS, 300);

    public static final CatalogItemDefinition<KnifeItem> BRONZE_KNIFE = PoptartCatalog.item(
                    "bronze_knife",
                    () -> new KnifeItem(
                            PoptartCoreTiers.BRONZE,
                            new Item.Properties()
                                    .attributes(KnifeItem.createAttributes(PoptartCoreTiers.BRONZE, 1.0F, -2.0F))))
            .handheld()
            .tags(PoptartCoreTags.KNIVES)
            .creative(TOOLS, 310);

    public static void initialize() {}
}
