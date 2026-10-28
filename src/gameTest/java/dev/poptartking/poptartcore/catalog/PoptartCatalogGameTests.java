package dev.poptartking.poptartcore.catalog;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartArmorItems;
import dev.poptartking.poptartcore.registry.PoptartBuildingBlocks;
import dev.poptartking.poptartcore.registry.PoptartCreateBlocks;
import dev.poptartking.poptartcore.registry.PoptartMaterialBlocks;
import dev.poptartking.poptartcore.registry.PoptartMaterialItems;
import dev.poptartking.poptartcore.registry.PoptartProcessingItems;
import dev.poptartking.poptartcore.registry.PoptartToolItems;
import dev.poptartking.poptartcore.registry.PoptartWorkstationBlocks;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PoptartCatalogGameTests {
    private PoptartCatalogGameTests() {}

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void catalogTagDeclarationsMatchLoadedResources(GameTestHelper helper) {
        var missing = new java.util.ArrayList<String>();
        for (var definition : PoptartCatalog.blocks()) {
            for (var tag : definition.tags())
                if (!definition.get().defaultBlockState().is(tag))
                    missing.add(definition.id() + " block tag " + tag.location());
            for (var tag : definition.itemTags())
                if (!definition.item().get().getDefaultInstance().is(tag))
                    missing.add(definition.id() + " block item tag " + tag.location());
        }
        for (var definition : PoptartCatalog.items()) {
            for (var tag : definition.tags())
                if (!definition.get().getDefaultInstance().is(tag))
                    missing.add(definition.id() + " item tag " + tag.location());
        }
        helper.assertTrue(missing.isEmpty(), "Missing declared catalog tags: " + missing);
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void exportRegisteredCatalogForPackagedResourceChecks(GameTestHelper helper) throws Exception {
        CatalogVerificationData.writeManifest();
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void specialCatalogItemsAndBlocksRetainTheirBehavior(GameTestHelper helper) {
        helper.assertTrue(
                PoptartBuildingBlocks.CLINKER_BRICK_SLAB.model() == CatalogBlockModel.SLAB
                        && PoptartBuildingBlocks.CLINKER_BRICK_STAIRS.model() == CatalogBlockModel.STAIRS
                        && PoptartBuildingBlocks.CLINKER_BRICK_WALL.model() == CatalogBlockModel.WALL
                        && PoptartBuildingBlocks.CLINKER_TILE_SLAB.model() == CatalogBlockModel.SLAB
                        && PoptartBuildingBlocks.CLINKER_TILE_STAIRS.model() == CatalogBlockModel.STAIRS
                        && PoptartBuildingBlocks.CLINKER_TILE_WALL.model() == CatalogBlockModel.WALL
                        && PoptartBuildingBlocks.CLINKER_PILLAR.model() == CatalogBlockModel.EXTERNAL,
                "Catalog clinker blocks do not preserve their model types");
        helper.assertTrue(
                PoptartWorkstationBlocks.IRON_BLOOM.model() == CatalogBlockModel.EXTERNAL
                        && PoptartWorkstationBlocks.IRON_BLOOM.lootMode() == CatalogBlockDefinition.LootMode.NONE
                        && PoptartWorkstationBlocks.IRON_BLOOM
                                        .get()
                                        .defaultBlockState()
                                        .getValue(dev.poptartking.poptartcore.bloomery.IronBloomBlock.BLOOMS)
                                == 1,
                "Catalog Iron Bloom does not preserve its custom model, loot, and initial stack state");
        helper.assertTrue(
                PoptartWorkstationBlocks.WORKBENCH.model() == CatalogBlockModel.EXTERNAL
                        && PoptartWorkstationBlocks.WORKBENCH.lootMode() == CatalogBlockDefinition.LootMode.GENERATED
                        && PoptartWorkstationBlocks.WORKBENCH.get()
                                instanceof dev.poptartking.poptartcore.workbench.WorkbenchBlock,
                "Catalog Workbench does not preserve its custom block class, model, and self-drop loot");
        helper.assertTrue(
                PoptartWorkstationBlocks.QUERN.model() == CatalogBlockModel.EXTERNAL
                        && PoptartWorkstationBlocks.QUERN.lootMode() == CatalogBlockDefinition.LootMode.GENERATED
                        && PoptartWorkstationBlocks.QUERN.get() instanceof dev.poptartking.poptartcore.quern.QuernBlock,
                "Catalog Quern does not preserve its custom block class, model, and self-drop loot");
        helper.assertTrue(
                PoptartWorkstationBlocks.BLOOMERY.model() == CatalogBlockModel.EXTERNAL
                        && PoptartWorkstationBlocks.BLOOMERY.lootMode() == CatalogBlockDefinition.LootMode.GENERATED
                        && PoptartWorkstationBlocks.BLOOMERY.get()
                                instanceof dev.poptartking.poptartcore.bloomery.BloomeryBlock,
                "Catalog Bloomery does not preserve its custom block class, model, and self-drop loot");
        helper.assertTrue(
                PoptartCreateBlocks.PORTABLE_ENGINE.model() == CatalogBlockModel.EXTERNAL
                        && PoptartCreateBlocks.PORTABLE_ENGINE.lootMode() == CatalogBlockDefinition.LootMode.GENERATED
                        && PoptartCreateBlocks.PORTABLE_ENGINE.get()
                                instanceof
                                dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlock,
                "Catalog Portable Engine does not preserve its Aeronautics block class, model, and self-drop loot");
        helper.assertTrue(
                PoptartWorkstationBlocks.CRUCIBLE.model() == CatalogBlockModel.EXTERNAL
                        && PoptartWorkstationBlocks.CRUCIBLE.lootMode() == CatalogBlockDefinition.LootMode.GENERATED
                        && PoptartWorkstationBlocks.CRUCIBLE.get()
                                instanceof dev.poptartking.poptartcore.crucible.CrucibleBlock
                        && PoptartWorkstationBlocks.CRUCIBLE.item().get()
                                instanceof dev.poptartking.poptartcore.crucible.CrucibleBlockItem,
                "Catalog Crucible does not preserve its custom block, BlockItem, model, and self-drop loot");
        helper.assertTrue(
                PoptartWorkstationBlocks.MILLSTONE.model() == CatalogBlockModel.EXTERNAL
                        && PoptartWorkstationBlocks.MILLSTONE.lootMode() == CatalogBlockDefinition.LootMode.GENERATED
                        && PoptartWorkstationBlocks.MILLSTONE.get()
                                instanceof dev.poptartking.poptartcore.millstone.MillstoneBlock
                        && PoptartWorkstationBlocks.MILLSTONE.item().get()
                                instanceof dev.poptartking.poptartcore.millstone.MillstoneBlockItem,
                "Catalog Millstone does not preserve its custom block, BlockItem, model, and self-drop loot");
        helper.assertTrue(
                PoptartMaterialBlocks.WAX_BLOCK.item().get().getBlock() == PoptartMaterialBlocks.WAX_BLOCK.get(),
                "Catalog block item does not point to its registered block");
        helper.assertTrue(
                PoptartMaterialItems.TIN_NUGGET.item().get() == PoptartMaterialItems.TIN_NUGGET.get(),
                "Catalog definition does not return its registered item");
        helper.assertTrue(
                PoptartProcessingItems.INGOT_MOULD.get().getDefaultInstance().getMaxDamage() == 32
                        && PoptartProcessingItems.PLATE_MOULD
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 32,
                "Catalog moulds do not preserve their durability");
        helper.assertTrue(
                PoptartMaterialItems.WAX.get() instanceof dev.poptartking.poptartcore.wax.WaxItem,
                "Catalog wax does not preserve its custom item class");
        helper.assertTrue(
                PoptartToolItems.FIRESTARTER.get().getDefaultInstance().getMaxDamage() == 3,
                "Catalog firestarter does not preserve its durability");
        helper.assertTrue(
                PoptartToolItems.HAMMER.get().getDefaultInstance().getMaxDamage() == 720
                        && PoptartToolItems.HAMMER.get().getDefaultMaxStackSize() == 1,
                "Catalog hammer does not preserve its durability and stack size");
        helper.assertTrue(
                PoptartToolItems.BEE_SMOKER.get().getDefaultInstance().getMaxDamage() == 360
                        && PoptartToolItems.BEE_SMOKER.get().getDefaultMaxStackSize() == 1,
                "Catalog Bee Smoker does not preserve its durability and stack size");
        helper.assertTrue(
                PoptartToolItems.REPEATING_CROSSBOW.get().getDefaultInstance().getMaxDamage() == 300
                        && PoptartToolItems.REPEATING_CROSSBOW.get().getDefaultMaxStackSize() == 1
                        && net.minecraft.world.item.component.ChargedProjectiles.EMPTY.equals(
                                PoptartToolItems.REPEATING_CROSSBOW
                                        .get()
                                        .getDefaultInstance()
                                        .get(net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES)),
                "Catalog Repeating Crossbow does not preserve its durability, stack size, and charge data");
        helper.assertTrue(
                PoptartArmorItems.MINING_HELMET.get().getType() == net.minecraft.world.item.ArmorItem.Type.HELMET
                        && PoptartArmorItems.MINING_HELMET.get().getMaterial()
                                == dev.poptartking.poptartcore.registry.PoptartCoreArmorMaterials.MINING_ARMOR_MATERIAL
                        && PoptartArmorItems.MINING_HELMET
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 100,
                "Catalog Mining Helmet does not preserve its slot, material, and durability");
        helper.assertTrue(
                PoptartArmorItems.RAW_HIDE_HELMET.get().getType() == net.minecraft.world.item.ArmorItem.Type.HELMET
                        && PoptartArmorItems.RAW_HIDE_CHESTPLATE.get().getType()
                                == net.minecraft.world.item.ArmorItem.Type.CHESTPLATE
                        && PoptartArmorItems.RAW_HIDE_LEGGINGS.get().getType()
                                == net.minecraft.world.item.ArmorItem.Type.LEGGINGS
                        && PoptartArmorItems.RAW_HIDE_HELMET.get().getMaterial()
                                == dev.poptartking.poptartcore.registry.PoptartCoreArmorMaterials
                                        .RAW_HIDE_ARMOR_MATERIAL
                        && PoptartArmorItems.RAW_HIDE_CHESTPLATE.get().getMaterial()
                                == dev.poptartking.poptartcore.registry.PoptartCoreArmorMaterials
                                        .RAW_HIDE_ARMOR_MATERIAL
                        && PoptartArmorItems.RAW_HIDE_LEGGINGS.get().getMaterial()
                                == dev.poptartking.poptartcore.registry.PoptartCoreArmorMaterials
                                        .RAW_HIDE_ARMOR_MATERIAL
                        && PoptartArmorItems.RAW_HIDE_HELMET
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 140
                        && PoptartArmorItems.RAW_HIDE_CHESTPLATE
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 140
                        && PoptartArmorItems.RAW_HIDE_LEGGINGS
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 140,
                "Catalog Raw Hide armor does not preserve its slots, material, and durability");
        helper.assertTrue(
                PoptartArmorItems.BEEKEEPER_HELMET.get().getType() == net.minecraft.world.item.ArmorItem.Type.HELMET
                        && PoptartArmorItems.BEEKEEPER_CHESTPLATE.get().getType()
                                == net.minecraft.world.item.ArmorItem.Type.CHESTPLATE
                        && PoptartArmorItems.BEEKEEPER_LEGGINGS.get().getType()
                                == net.minecraft.world.item.ArmorItem.Type.LEGGINGS
                        && PoptartArmorItems.BEEKEEPER_BOOTS.get().getType()
                                == net.minecraft.world.item.ArmorItem.Type.BOOTS
                        && PoptartArmorItems.BEEKEEPER_HELMET
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 200
                        && PoptartArmorItems.BEEKEEPER_CHESTPLATE
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 300
                        && PoptartArmorItems.BEEKEEPER_LEGGINGS
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 260
                        && PoptartArmorItems.BEEKEEPER_BOOTS
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 200,
                "Catalog Beekeeper armor does not preserve its slots and durability");
        helper.assertTrue(
                PoptartArmorItems.STEEL_HELMET.get().getType() == net.minecraft.world.item.ArmorItem.Type.HELMET
                        && PoptartArmorItems.STEEL_CHESTPLATE.get().getType()
                                == net.minecraft.world.item.ArmorItem.Type.CHESTPLATE
                        && PoptartArmorItems.STEEL_LEGGINGS.get().getType()
                                == net.minecraft.world.item.ArmorItem.Type.LEGGINGS
                        && PoptartArmorItems.STEEL_BOOTS.get().getType()
                                == net.minecraft.world.item.ArmorItem.Type.BOOTS
                        && PoptartArmorItems.STEEL_HELMET.get().getMaterial()
                                == dev.poptartking.poptartcore.registry.PoptartCoreArmorMaterials.STEEL_ARMOR_MATERIAL
                        && PoptartArmorItems.STEEL_CHESTPLATE
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 400
                        && PoptartArmorItems.STEEL_LEGGINGS
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 375
                        && PoptartArmorItems.STEEL_BOOTS
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 325
                        && PoptartArmorItems.STEEL_HELMET
                                        .get()
                                        .getDefaultInstance()
                                        .getMaxDamage()
                                == 275,
                "Catalog Steel armor does not preserve its slots, material, and durability");
        helper.succeed();
    }
}
