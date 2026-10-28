package dev.poptartking.poptartcore.registry;

import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.BUILDING;

import com.teamabnormals.caverns_and_chasms.core.registry.CCBlocks;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import dev.poptartking.poptartcore.clinker.ClinkerPillarBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class PoptartBuildingBlocks {
    public static final CatalogBlockDefinition<Block> BRONZE_BRICKS = PoptartCatalog.block(
                    "bronze_bricks", () -> new Block(platedBrickProperties(CCBlocks.COPPER_BRICKS.get())))
            .withName("Bronze Plated Bricks")
            .mineableWithPickaxe()
            .creative(BUILDING, 320);

    public static final CatalogBlockDefinition<Block> CONCRETE = PoptartCatalog.block(
                    "concrete", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_GRAY_CONCRETE)))
            .externalModel()
            .customLoot()
            .mineableWithPickaxe()
            .creative(BUILDING, 170);

    public static final CatalogBlockDefinition<SlabBlock> CONCRETE_SLAB = PoptartCatalog.block(
                    "concrete_slab",
                    () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_GRAY_CONCRETE)))
            .externalModel()
            .customLoot()
            .mineableWithPickaxe()
            .creative(BUILDING, 180);

    public static final CatalogBlockDefinition<StairBlock> CONCRETE_STAIRS = PoptartCatalog.block(
                    "concrete_stairs",
                    () -> new StairBlock(
                            CONCRETE.get().defaultBlockState(),
                            BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_GRAY_CONCRETE)))
            .externalModel()
            .customLoot()
            .mineableWithPickaxe()
            .creative(BUILDING, 190);

    public static final CatalogBlockDefinition<ColoredFallingBlock> CONCRETE_POWDER = PoptartCatalog.block(
                    "concrete_powder",
                    () -> new ColoredFallingBlock(
                            new ColorRGBA(15658734),
                            BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_GRAY_CONCRETE_POWDER)))
            .externalModel()
            .customLoot()
            .tags(BlockTags.MINEABLE_WITH_SHOVEL)
            .creative(BUILDING, 200);

    public static final CatalogBlockDefinition<Block> CHISELED_BRONZE_BRICKS = PoptartCatalog.block(
                    "chiseled_bronze_bricks",
                    () -> new Block(platedBrickProperties(CCBlocks.CHISELED_COPPER_BRICKS.get())))
            .withName("Chiseled Bronze Plated Bricks")
            .mineableWithPickaxe()
            .creative(BUILDING, 330);

    public static final CatalogBlockDefinition<SlabBlock> BRONZE_BRICK_SLAB = PoptartCatalog.block(
                    "bronze_brick_slab", () -> new SlabBlock(platedBrickProperties(CCBlocks.COPPER_BRICK_SLAB.get())))
            .withName("Bronze Plated Brick Slab")
            .slabModel(PoptartCore.location("block/bronze_bricks"), "")
            .mineableWithPickaxe()
            .creative(BUILDING, 340);

    public static final CatalogBlockDefinition<StairBlock> BRONZE_BRICK_STAIRS = PoptartCatalog.block(
                    "bronze_brick_stairs",
                    () -> new StairBlock(
                            BRONZE_BRICKS.get().defaultBlockState(),
                            platedBrickProperties(CCBlocks.COPPER_BRICK_STAIRS.get())))
            .withName("Bronze Plated Brick Stairs")
            .stairsModel(PoptartCore.location("block/bronze_bricks"), "")
            .mineableWithPickaxe()
            .creative(BUILDING, 350);

    public static final CatalogBlockDefinition<WallBlock> BRONZE_BRICK_WALL = PoptartCatalog.block(
                    "bronze_brick_wall", () -> new WallBlock(platedBrickProperties(CCBlocks.COPPER_BRICK_WALL.get())))
            .withName("Bronze Plated Brick Wall")
            .wallModel(PoptartCore.location("block/bronze_bricks"), "")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS)
            .creative(BUILDING, 360);

    public static final CatalogBlockDefinition<Block> AGED_BRONZE_BRICKS = PoptartCatalog.block(
                    "aged_bronze_bricks", () -> new Block(platedBrickProperties(CCBlocks.COPPER_BRICKS.get())))
            .withName("Aged Bronze Plated Bricks")
            .mineableWithPickaxe()
            .creative(BUILDING, 370);

    public static final CatalogBlockDefinition<Block> CHISELED_AGED_BRONZE_BRICKS = PoptartCatalog.block(
                    "chiseled_aged_bronze_bricks",
                    () -> new Block(platedBrickProperties(CCBlocks.CHISELED_COPPER_BRICKS.get())))
            .withName("Chiseled Aged Bronze Plated Bricks")
            .mineableWithPickaxe()
            .creative(BUILDING, 380);

    public static final CatalogBlockDefinition<SlabBlock> AGED_BRONZE_BRICK_SLAB = PoptartCatalog.block(
                    "aged_bronze_brick_slab",
                    () -> new SlabBlock(platedBrickProperties(CCBlocks.COPPER_BRICK_SLAB.get())))
            .withName("Aged Bronze Plated Brick Slab")
            .slabModel(PoptartCore.location("block/aged_bronze_bricks"), "")
            .mineableWithPickaxe()
            .creative(BUILDING, 390);

    public static final CatalogBlockDefinition<StairBlock> AGED_BRONZE_BRICK_STAIRS = PoptartCatalog.block(
                    "aged_bronze_brick_stairs",
                    () -> new StairBlock(
                            AGED_BRONZE_BRICKS.get().defaultBlockState(),
                            platedBrickProperties(CCBlocks.COPPER_BRICK_STAIRS.get())))
            .withName("Aged Bronze Plated Brick Stairs")
            .stairsModel(PoptartCore.location("block/aged_bronze_bricks"), "")
            .mineableWithPickaxe()
            .creative(BUILDING, 400);

    public static final CatalogBlockDefinition<WallBlock> AGED_BRONZE_BRICK_WALL = PoptartCatalog.block(
                    "aged_bronze_brick_wall",
                    () -> new WallBlock(platedBrickProperties(CCBlocks.COPPER_BRICK_WALL.get())))
            .withName("Aged Bronze Plated Brick Wall")
            .wallModel(PoptartCore.location("block/aged_bronze_bricks"), "")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS)
            .creative(BUILDING, 410);

    public static final CatalogBlockDefinition<Block> LEAD_BRICKS = PoptartCatalog.block(
                    "lead_bricks", () -> new Block(platedBrickProperties(CCBlocks.TIN_BRICKS.get())))
            .withName("Lead Plated Bricks")
            .mineableWithPickaxe()
            .creative(BUILDING, 420);

    public static final CatalogBlockDefinition<Block> CHISELED_LEAD_BRICKS = PoptartCatalog.block(
                    "chiseled_lead_bricks", () -> new Block(platedBrickProperties(CCBlocks.CHISELED_TIN_BRICKS.get())))
            .withName("Chiseled Lead Plated Bricks")
            .mineableWithPickaxe()
            .creative(BUILDING, 430);

    public static final CatalogBlockDefinition<SlabBlock> LEAD_BRICK_SLAB = PoptartCatalog.block(
                    "lead_brick_slab", () -> new SlabBlock(platedBrickProperties(CCBlocks.TIN_BRICK_SLAB.get())))
            .withName("Lead Plated Brick Slab")
            .slabModel(PoptartCore.location("block/lead_bricks"), "")
            .mineableWithPickaxe()
            .creative(BUILDING, 440);

    public static final CatalogBlockDefinition<StairBlock> LEAD_BRICK_STAIRS = PoptartCatalog.block(
                    "lead_brick_stairs",
                    () -> new StairBlock(
                            LEAD_BRICKS.get().defaultBlockState(),
                            platedBrickProperties(CCBlocks.TIN_BRICK_STAIRS.get())))
            .withName("Lead Plated Brick Stairs")
            .stairsModel(PoptartCore.location("block/lead_bricks"), "")
            .mineableWithPickaxe()
            .creative(BUILDING, 450);

    public static final CatalogBlockDefinition<WallBlock> LEAD_BRICK_WALL = PoptartCatalog.block(
                    "lead_brick_wall", () -> new WallBlock(platedBrickProperties(CCBlocks.TIN_BRICK_WALL.get())))
            .withName("Lead Plated Brick Wall")
            .wallModel(PoptartCore.location("block/lead_bricks"), "")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS)
            .creative(BUILDING, 460);

    public static final CatalogBlockDefinition<Block> MERCURY_BRICKS = PoptartCatalog.block(
                    "mercury_bricks", () -> new Block(platedBrickProperties(CCBlocks.SILVER_BRICKS.get())))
            .withName("Mercury Plated Bricks")
            .mineableWithPickaxe()
            .creative(BUILDING, 470);

    public static final CatalogBlockDefinition<Block> CHISELED_MERCURY_BRICKS = PoptartCatalog.block(
                    "chiseled_mercury_bricks",
                    () -> new Block(platedBrickProperties(CCBlocks.CHISELED_SILVER_BRICKS.get())))
            .withName("Chiseled Mercury Plated Bricks")
            .mineableWithPickaxe()
            .creative(BUILDING, 480);

    public static final CatalogBlockDefinition<SlabBlock> MERCURY_BRICK_SLAB = PoptartCatalog.block(
                    "mercury_brick_slab", () -> new SlabBlock(platedBrickProperties(CCBlocks.SILVER_BRICK_SLAB.get())))
            .withName("Mercury Plated Brick Slab")
            .slabModel(PoptartCore.location("block/mercury_bricks"), "")
            .mineableWithPickaxe()
            .creative(BUILDING, 490);

    public static final CatalogBlockDefinition<StairBlock> MERCURY_BRICK_STAIRS = PoptartCatalog.block(
                    "mercury_brick_stairs",
                    () -> new StairBlock(
                            MERCURY_BRICKS.get().defaultBlockState(),
                            platedBrickProperties(CCBlocks.SILVER_BRICK_STAIRS.get())))
            .withName("Mercury Plated Brick Stairs")
            .stairsModel(PoptartCore.location("block/mercury_bricks"), "")
            .mineableWithPickaxe()
            .creative(BUILDING, 500);

    public static final CatalogBlockDefinition<WallBlock> MERCURY_BRICK_WALL = PoptartCatalog.block(
                    "mercury_brick_wall", () -> new WallBlock(platedBrickProperties(CCBlocks.SILVER_BRICK_WALL.get())))
            .withName("Mercury Plated Brick Wall")
            .wallModel(PoptartCore.location("block/mercury_bricks"), "")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS)
            .creative(BUILDING, 510);

    public static final CatalogBlockDefinition<Block> STEEL_BRICKS = PoptartCatalog.block(
                    "steel_bricks", () -> new Block(platedBrickProperties(CCBlocks.IRON_BRICKS.get())))
            .withName("Steel Plated Bricks")
            .mineableWithPickaxe()
            .creative(BUILDING, 520);

    public static final CatalogBlockDefinition<Block> CHISELED_STEEL_BRICKS = PoptartCatalog.block(
                    "chiseled_steel_bricks",
                    () -> new Block(platedBrickProperties(CCBlocks.CHISELED_IRON_BRICKS.get())))
            .withName("Chiseled Steel Plated Bricks")
            .mineableWithPickaxe()
            .creative(BUILDING, 530);

    public static final CatalogBlockDefinition<SlabBlock> STEEL_BRICK_SLAB = PoptartCatalog.block(
                    "steel_brick_slab", () -> new SlabBlock(platedBrickProperties(CCBlocks.IRON_BRICK_SLAB.get())))
            .withName("Steel Plated Brick Slab")
            .slabModel(PoptartCore.location("block/steel_bricks"), "")
            .mineableWithPickaxe()
            .creative(BUILDING, 540);

    public static final CatalogBlockDefinition<StairBlock> STEEL_BRICK_STAIRS = PoptartCatalog.block(
                    "steel_brick_stairs",
                    () -> new StairBlock(
                            STEEL_BRICKS.get().defaultBlockState(),
                            platedBrickProperties(CCBlocks.IRON_BRICK_STAIRS.get())))
            .withName("Steel Plated Brick Stairs")
            .stairsModel(PoptartCore.location("block/steel_bricks"), "")
            .mineableWithPickaxe()
            .creative(BUILDING, 550);

    public static final CatalogBlockDefinition<WallBlock> STEEL_BRICK_WALL = PoptartCatalog.block(
                    "steel_brick_wall", () -> new WallBlock(platedBrickProperties(CCBlocks.IRON_BRICK_WALL.get())))
            .withName("Steel Plated Brick Wall")
            .wallModel(PoptartCore.location("block/steel_bricks"), "")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS)
            .creative(BUILDING, 560);

    public static final CatalogBlockDefinition<Block> TITANIUM_BRICKS = PoptartCatalog.block(
                    "titanium_bricks", () -> new Block(titaniumBrickProperties()))
            .withName("Titanium Plated Bricks")
            .mineableWithPickaxe()
            .creative(BUILDING, 570);

    public static final CatalogBlockDefinition<Block> CHISELED_TITANIUM_BRICKS = PoptartCatalog.block(
                    "chiseled_titanium_bricks", () -> new Block(titaniumBrickProperties()))
            .withName("Chiseled Titanium Plated Bricks")
            .mineableWithPickaxe()
            .creative(BUILDING, 580);

    public static final CatalogBlockDefinition<SlabBlock> TITANIUM_BRICK_SLAB = PoptartCatalog.block(
                    "titanium_brick_slab", () -> new SlabBlock(titaniumBrickProperties()))
            .withName("Titanium Plated Brick Slab")
            .slabModel(PoptartCore.location("block/titanium_bricks"), "")
            .mineableWithPickaxe()
            .creative(BUILDING, 590);

    public static final CatalogBlockDefinition<StairBlock> TITANIUM_BRICK_STAIRS = PoptartCatalog.block(
                    "titanium_brick_stairs",
                    () -> new StairBlock(TITANIUM_BRICKS.get().defaultBlockState(), titaniumBrickProperties()))
            .withName("Titanium Plated Brick Stairs")
            .stairsModel(PoptartCore.location("block/titanium_bricks"), "")
            .mineableWithPickaxe()
            .creative(BUILDING, 600);

    public static final CatalogBlockDefinition<WallBlock> TITANIUM_BRICK_WALL = PoptartCatalog.block(
                    "titanium_brick_wall", () -> new WallBlock(titaniumBrickProperties()))
            .withName("Titanium Plated Brick Wall")
            .wallModel(PoptartCore.location("block/titanium_bricks"), "")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS)
            .creative(BUILDING, 610);

    public static final CatalogBlockDefinition<Block> CLINKER_BRICKS = PoptartCatalog.block(
                    "clinker_bricks", () -> new Block(clinkerProperties()))
            .randomCubeModel(PoptartCore.location("block/clinker/clinker_bricks"), "clinker", 12)
            .mineableWithPickaxe()
            .creative(BUILDING, 210);

    public static final CatalogBlockDefinition<SlabBlock> CLINKER_BRICK_SLAB = PoptartCatalog.block(
                    "clinker_brick_slab", () -> new SlabBlock(clinkerProperties()))
            .slabModel(PoptartCore.location("block/clinker/clinker_bricks_1"), "clinker")
            .mineableWithPickaxe()
            .creative(BUILDING, 220);

    public static final CatalogBlockDefinition<StairBlock> CLINKER_BRICK_STAIRS = PoptartCatalog.block(
                    "clinker_brick_stairs",
                    () -> new StairBlock(CLINKER_BRICKS.get().defaultBlockState(), clinkerProperties()))
            .stairsModel(PoptartCore.location("block/clinker/clinker_bricks_1"), "clinker")
            .mineableWithPickaxe()
            .creative(BUILDING, 230);

    public static final CatalogBlockDefinition<WallBlock> CLINKER_BRICK_WALL = PoptartCatalog.block(
                    "clinker_brick_wall", () -> new WallBlock(clinkerProperties()))
            .wallModel(PoptartCore.location("block/clinker/clinker_bricks_1"), "clinker")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS)
            .creative(BUILDING, 240);

    public static final CatalogBlockDefinition<Block> CLINKER_TILE = PoptartCatalog.block(
                    "clinker_tile", () -> new Block(clinkerProperties()))
            .simpleModel(PoptartCore.location("block/clinker/clinker_tile"), "clinker")
            .mineableWithPickaxe()
            .creative(BUILDING, 250);

    public static final CatalogBlockDefinition<SlabBlock> CLINKER_TILE_SLAB = PoptartCatalog.block(
                    "clinker_tile_slab", () -> new SlabBlock(clinkerProperties()))
            .slabModel(PoptartCore.location("block/clinker/clinker_tile"), "clinker")
            .mineableWithPickaxe()
            .creative(BUILDING, 260);

    public static final CatalogBlockDefinition<StairBlock> CLINKER_TILE_STAIRS = PoptartCatalog.block(
                    "clinker_tile_stairs",
                    () -> new StairBlock(CLINKER_TILE.get().defaultBlockState(), clinkerProperties()))
            .stairsModel(PoptartCore.location("block/clinker/clinker_tile"), "clinker")
            .mineableWithPickaxe()
            .creative(BUILDING, 270);

    public static final CatalogBlockDefinition<WallBlock> CLINKER_TILE_WALL = PoptartCatalog.block(
                    "clinker_tile_wall", () -> new WallBlock(clinkerProperties()))
            .wallModel(PoptartCore.location("block/clinker/clinker_tile"), "clinker")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS)
            .creative(BUILDING, 280);

    public static final CatalogBlockDefinition<Block> MOSAIC_CLINKER_TILE = PoptartCatalog.block(
                    "mosaic_clinker_tile", () -> new Block(clinkerProperties()))
            .randomBottomTopModel(
                    PoptartCore.location("block/clinker/mosaic_clinker_tile"),
                    PoptartCore.location("block/clinker/mosaic_clinker_tile_top"),
                    PoptartCore.location("block/clinker/mosaic_clinker_tile_bottom"),
                    "clinker",
                    4)
            .mineableWithPickaxe()
            .creative(BUILDING, 290);

    public static final CatalogBlockDefinition<Block> CHISELED_CLINKER_TILE = PoptartCatalog.block(
                    "chiseled_clinker_tile", () -> new Block(clinkerProperties()))
            .simpleModel(PoptartCore.location("block/clinker/chiseled_clinker_tile"), "clinker")
            .mineableWithPickaxe()
            .creative(BUILDING, 300);

    public static final CatalogBlockDefinition<ClinkerPillarBlock> CLINKER_PILLAR = PoptartCatalog.block(
                    "clinker_pillar", () -> new ClinkerPillarBlock(clinkerProperties()))
            .externalModel()
            .mineableWithPickaxe()
            .creative(BUILDING, 310);

    private static BlockBehaviour.Properties clinkerProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS);
    }

    private static BlockBehaviour.Properties platedBrickProperties(Block source) {
        return BlockBehaviour.Properties.ofFullCopy(source);
    }

    private static BlockBehaviour.Properties titaniumBrickProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .strength(5.0F, 6.0F);
    }

    public static final CatalogBlockDefinition<Block> RUBBER_BLOCK = PoptartCatalog.block(
                    "rubber_block", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_WOOL)))
            .withName("Block of Rubber")
            .customLoot()
            .creative(BUILDING, 140);

    private PoptartBuildingBlocks() {}

    public static void initialize() {}
}
