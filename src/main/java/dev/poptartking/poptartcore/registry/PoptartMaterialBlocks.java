package dev.poptartking.poptartcore.registry;

import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.MATERIALS;

import com.teamabnormals.caverns_and_chasms.core.registry.CCSoundEvents.CCSoundTypes;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import dev.poptartking.poptartcore.ingotpile.IngotPileBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PoptartMaterialBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PoptartCore.MOD_ID);

    public static final DeferredBlock<IngotPileBlock> INGOT_PILE = BLOCKS.register(
            "ingot_pile",
            () -> new IngotPileBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .noOcclusion()
                    .noLootTable()
                    .pushReaction(PushReaction.DESTROY)));

    public static final CatalogBlockDefinition<Block> TIN_BLOCK = PoptartCatalog.block(
                    "tin_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(1.0F, 6.0F)))
            .withName("Block of Tin")
            .withStorageRecipes(() -> PoptartMaterialItems.TIN_INGOT.get(), "tin_ingots_from_block")
            .mineableWithPickaxe()
            .creative(MATERIALS, 70);

    public static final CatalogBlockDefinition<Block> TIN_ORE = PoptartCatalog.block(
                    "tin_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_ORE)
                            .strength(2.0F, 6.0F)))
            .withOreDrop(() -> PoptartMaterialItems.RAW_TIN.get())
            .mineableWithPickaxe()
            .creative(MATERIALS, 10);

    public static final CatalogBlockDefinition<Block> DEEPSLATE_TIN_ORE = PoptartCatalog.block(
                    "deepslate_tin_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_COPPER_ORE)
                            .strength(3.0F, 6.0F)))
            .withOreDrop(() -> PoptartMaterialItems.RAW_TIN.get())
            .mineableWithPickaxe()
            .creative(MATERIALS, 20);

    public static final CatalogBlockDefinition<Block> RAW_TIN_BLOCK = PoptartCatalog.block(
                    "raw_tin_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_COPPER_BLOCK)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Raw Tin")
            .withStorageRecipes(() -> PoptartMaterialItems.RAW_TIN.get(), "raw_tin_from_block")
            .mineableWithPickaxe()
            .creative(MATERIALS, 40);

    public static final CatalogBlockDefinition<Block> LEAD_BLOCK = PoptartCatalog.block(
                    "lead_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(1.0F, 6.0F)))
            .withName("Block of Lead")
            .withStorageRecipes(() -> PoptartMaterialItems.LEAD_INGOT.get(), "lead_ingots_from_block")
            .requiresIronTool()
            .creative(MATERIALS, 140);

    public static final CatalogBlockDefinition<Block> LEAD_ORE = PoptartCatalog.block(
                    "lead_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .strength(2.0F, 6.0F)))
            .withOreDrop(() -> PoptartMaterialItems.RAW_LEAD.get())
            .requiresIronTool()
            .creative(MATERIALS, 80);

    public static final CatalogBlockDefinition<Block> DEEPSLATE_LEAD_ORE = PoptartCatalog.block(
                    "deepslate_lead_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                            .sound(SoundType.DEEPSLATE)
                            .requiresCorrectToolForDrops()
                            .strength(3.0F, 6.0F)))
            .withOreDrop(() -> PoptartMaterialItems.RAW_LEAD.get())
            .requiresIronTool()
            .creative(MATERIALS, 90);

    public static final CatalogBlockDefinition<Block> RAW_LEAD_BLOCK = PoptartCatalog.block(
                    "raw_lead_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Raw Lead")
            .withStorageRecipes(() -> PoptartMaterialItems.RAW_LEAD.get(), "raw_lead_from_block")
            .requiresIronTool()
            .creative(MATERIALS, 110);

    public static final CatalogBlockDefinition<Block> MAGNETITE_BLOCK = PoptartCatalog.block(
                    "magnetite_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Magnetite")
            .externalModel()
            .customLoot()
            .requiresStoneTool()
            .creative(MATERIALS, 160);

    public static final CatalogBlockDefinition<Block> CINNABAR_BLOCK = PoptartCatalog.block(
                    "cinnabar_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)
                            .strength(4.0F, 3.0F)))
            .withName("Block of Cinnabar")
            .externalModel()
            .customLoot()
            .requiresIronTool()
            .creative(MATERIALS, 180);

    public static final CatalogBlockDefinition<Block> MERCURY_BLOCK = PoptartCatalog.block(
                    "mercury_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Mercury")
            .externalModel()
            .customLoot()
            .requiresIronTool()
            .creative(MATERIALS, 210);

    public static final CatalogBlockDefinition<Block> SILVER_BLOCK = PoptartCatalog.block(
                    "silver_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Silver")
            .withStorageRecipes(() -> PoptartMaterialItems.SILVER_INGOT.get(), "silver_ingots_from_block")
            .requiresIronTool()
            .creative(MATERIALS, 280);

    public static final CatalogBlockDefinition<Block> SILVER_ORE = PoptartCatalog.block(
                    "silver_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .strength(5.0F, 6.0F)))
            .withOreDrop(() -> PoptartMaterialItems.RAW_SILVER.get())
            .requiresIronTool()
            .creative(MATERIALS, 220);

    public static final CatalogBlockDefinition<Block> DEEPSLATE_SILVER_ORE = PoptartCatalog.block(
                    "deepslate_silver_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)
                            .sound(SoundType.DEEPSLATE)
                            .requiresCorrectToolForDrops()
                            .strength(6.0F, 6.0F)))
            .withOreDrop(() -> PoptartMaterialItems.RAW_SILVER.get())
            .requiresIronTool()
            .creative(MATERIALS, 230);

    public static final CatalogBlockDefinition<Block> RAW_SILVER_BLOCK = PoptartCatalog.block(
                    "raw_silver_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Raw Silver")
            .withStorageRecipes(() -> PoptartMaterialItems.RAW_SILVER.get(), "raw_silver_from_block")
            .requiresIronTool()
            .creative(MATERIALS, 250);

    public static final CatalogBlockDefinition<Block> TITANIUM_ORE = PoptartCatalog.block(
                    "titanium_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE)
                            .strength(3.0F, 6.0F)))
            .withOreDrop(() -> PoptartMaterialItems.RAW_TITANIUM.get())
            .requiresIronTool()
            .creative(MATERIALS, 290);

    public static final CatalogBlockDefinition<Block> DEEPSLATE_TITANIUM_ORE = PoptartCatalog.block(
                    "deepslate_titanium_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE)
                            .strength(4.5F, 6.0F)))
            .withOreDrop(() -> PoptartMaterialItems.RAW_TITANIUM.get())
            .requiresIronTool()
            .creative(MATERIALS, 300);

    public static final CatalogBlockDefinition<Block> CASSITERITE_TITANIUM_ORE = PoptartCatalog.block(
                    "cassiterite_titanium_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE)
                            .strength(3.0F, 6.0F)
                            .sound(CCSoundTypes.CASSITERITE)))
            .withName("Cassiterite Titanium Ore")
            .withOreDrop(() -> PoptartMaterialItems.RAW_TITANIUM.get())
            .requiresIronTool()
            .creative(MATERIALS, 310);

    public static final CatalogBlockDefinition<Block> CYLINDRITE_TITANIUM_ORE = PoptartCatalog.block(
                    "cylindrite_titanium_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE)
                            .strength(4.5F, 6.0F)
                            .sound(CCSoundTypes.CYLINDRITE)))
            .withName("Cylindrite Titanium Ore")
            .withOreDrop(() -> PoptartMaterialItems.RAW_TITANIUM.get())
            .requiresIronTool()
            .creative(MATERIALS, 320);

    public static final CatalogBlockDefinition<Block> RAW_TITANIUM_BLOCK = PoptartCatalog.block(
                    "raw_titanium_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Raw Titanium")
            .requiresIronTool()
            .creative(MATERIALS, 340);

    public static final CatalogBlockDefinition<Block> WAX_BLOCK = PoptartCatalog.block(
                    "wax_block",
                    () -> new Block(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.MUD).strength(0.7F, 0.3F)))
            .withStorageRecipes(() -> PoptartMaterialItems.WAX.get(), "wax_from_block")
            .creative(MATERIALS, 520);

    public static final CatalogBlockDefinition<Block> COAL_COKE_BLOCK = PoptartCatalog.block(
                    "coal_coke_block", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_BLOCK)))
            .withStorageRecipes(() -> PoptartMaterialItems.COAL_COKE.get(), "coal_coke_from_block")
            .fuelBurnTime(28800)
            .mineableWithPickaxe()
            .creative(MATERIALS, 490);

    public static final CatalogBlockDefinition<Block> BRONZE_BLOCK = PoptartCatalog.block(
                    "bronze_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(3.0F, 6.0F)))
            .withName("Block of Bronze")
            .withStorageRecipes(() -> PoptartMaterialItems.BRONZE_INGOT.get(), "bronze_ingot_from_block")
            .requiresStoneTool()
            .creative(MATERIALS, 420);

    public static final CatalogBlockDefinition<Block> STEEL_BLOCK = PoptartCatalog.block(
                    "steel_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(7.0F, 8.0F)))
            .withName("Block of Steel")
            .withStorageRecipes(() -> PoptartMaterialItems.STEEL_INGOT.get(), "steel_ingot_from_block")
            .requiresIronTool()
            .creative(MATERIALS, 470);

    private PoptartMaterialBlocks() {}

    public static void initialize() {}

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
