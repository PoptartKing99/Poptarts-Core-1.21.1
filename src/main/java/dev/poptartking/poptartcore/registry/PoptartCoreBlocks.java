package dev.poptartking.poptartcore.registry;

import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedShaftBlock;
import com.teamabnormals.caverns_and_chasms.core.registry.CCBlocks;
import com.teamabnormals.caverns_and_chasms.core.registry.CCSoundEvents.CCSoundTypes;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.barrel.FluidBarrelBlock;
import dev.poptartking.poptartcore.barrel.FluidBarrelBlockItem;
import dev.poptartking.poptartcore.beekeeping.BeehiveRoofBlock;
import dev.poptartking.poptartcore.beekeeping.BeehiveSupportBlock;
import dev.poptartking.poptartcore.blastfurnace.BlastFurnaceBlock;
import dev.poptartking.poptartcore.bloomery.BloomeryBlock;
import dev.poptartking.poptartcore.bloomery.IronBloomBlock;
import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import dev.poptartking.poptartcore.clinker.ClinkerPillarBlock;
import dev.poptartking.poptartcore.crucible.CrucibleBlock;
import dev.poptartking.poptartcore.crucible.CrucibleBlockItem;
import dev.poptartking.poptartcore.flesh.DecayingFleshBlock;
import dev.poptartking.poptartcore.flesh.LifebudBlock;
import dev.poptartking.poptartcore.ingotpile.IngotPileBlock;
import dev.poptartking.poptartcore.millstone.MillstoneBlock;
import dev.poptartking.poptartcore.millstone.MillstoneBlockItem;
import dev.poptartking.poptartcore.millstone.MillstoneRotorBlock;
import dev.poptartking.poptartcore.millstone.MillstoneStructuralBlock;
import dev.poptartking.poptartcore.quern.QuernBlock;
import dev.poptartking.poptartcore.rift.CupricBrazierBlock;
import dev.poptartking.poptartcore.rift.CupricSconceLeverBlock;
import dev.poptartking.poptartcore.rift.CupricSconceTorchBlock;
import dev.poptartking.poptartcore.rift.CupricSconceWallTorchBlock;
import dev.poptartking.poptartcore.rift.CupricStoneBrazierBlock;
import dev.poptartking.poptartcore.rift.PackedRiftSedimentBlock;
import dev.poptartking.poptartcore.rift.RiftBrazierBlock;
import dev.poptartking.poptartcore.rift.RiftCampfireBlock;
import dev.poptartking.poptartcore.rift.RiftFireBlock;
import dev.poptartking.poptartcore.rift.RiftSconceLeverBlock;
import dev.poptartking.poptartcore.rift.RiftSconceTorchBlock;
import dev.poptartking.poptartcore.rift.RiftSconceWallTorchBlock;
import dev.poptartking.poptartcore.rift.RiftSedimentBlock;
import dev.poptartking.poptartcore.rift.RiftStoneBrazierBlock;
import dev.poptartking.poptartcore.rift.RiftTorchBlock;
import dev.poptartking.poptartcore.rift.RiftWallTorchBlock;
import dev.poptartking.poptartcore.rift.SoulSconceLeverBlock;
import dev.poptartking.poptartcore.scribing.ScribingTableBlock;
import dev.poptartking.poptartcore.spider.TemporaryCobwebBlock;
import dev.poptartking.poptartcore.treetap.TreeTapBlock;
import dev.poptartking.poptartcore.treetap.LatexCauldronBlock;
import dev.poptartking.poptartcore.workbench.WorkbenchBlock;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlock;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SconceLeverBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PoptartCoreBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PoptartCore.MOD_ID);

    public static final DeferredBlock<IngotPileBlock> INGOT_PILE = BLOCKS.register(
            "ingot_pile",
            () -> new IngotPileBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .noOcclusion()
                    .noLootTable()
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<TemporaryCobwebBlock> TEMPORARY_COBWEB = BLOCKS.register(
            "temporary_cobweb",
            () -> new TemporaryCobwebBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.COBWEB).noLootTable()));
    public static final DeferredBlock<RiftFireBlock> RIFT_FIRE = BLOCKS.register(
            "rift_fire",
            () -> new RiftFireBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.FIRE).noLootTable()));

    public static final DeferredBlock<RiftWallTorchBlock> RIFT_WALL_TORCH = BLOCKS.register(
            "rift_wall_torch",
            () -> new RiftWallTorchBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .lightLevel(state -> 14)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)));
    public static final CatalogBlockDefinition<RiftTorchBlock> RIFT_TORCH = PoptartCatalog.block(
                    "rift_torch",
                    () -> new RiftTorchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TORCH)),
                    (block, properties) ->
                            new StandingAndWallBlockItem(block, RIFT_WALL_TORCH.get(), properties, Direction.DOWN))
            .externalModel();
    public static final DeferredBlock<RiftSconceWallTorchBlock> SCONCE_RIFT_WALL_TORCH = BLOCKS.register(
            "sconce_rift_wall_torch",
            () -> new RiftSconceWallTorchBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .lightLevel(state -> 14)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)));
    public static final CatalogBlockDefinition<RiftSconceTorchBlock> SCONCE_RIFT_TORCH = PoptartCatalog.block(
                    "sconce_rift_torch",
                    () -> new RiftSconceTorchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TORCH)),
                    (block, properties) -> new StandingAndWallBlockItem(
                            block, SCONCE_RIFT_WALL_TORCH.get(), properties, Direction.DOWN))
            .externalModel()
            .customLoot();
    public static final DeferredBlock<CupricSconceWallTorchBlock> SCONCE_CUPRIC_WALL_TORCH = BLOCKS.register(
            "sconce_cupric_wall_torch",
            () -> new CupricSconceWallTorchBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .lightLevel(state -> 10)
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)));
    public static final CatalogBlockDefinition<CupricSconceTorchBlock> SCONCE_CUPRIC_TORCH = PoptartCatalog.block(
                    "sconce_cupric_torch",
                    () -> new CupricSconceTorchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_TORCH)),
                    (block, properties) -> new StandingAndWallBlockItem(
                            block, SCONCE_CUPRIC_WALL_TORCH.get(), properties, Direction.DOWN))
            .externalModel();
    public static final CatalogBlockDefinition<LanternBlock> RIFT_LANTERN = PoptartCatalog.block(
                    "rift_lantern", () -> new LanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN)))
            .externalModel()
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<RiftCampfireBlock> RIFT_CAMPFIRE = PoptartCatalog.block(
                    "rift_campfire", () -> new RiftCampfireBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAMPFIRE)))
            .externalModel()
            .mineableWithAxe();
    public static final CatalogBlockDefinition<RiftBrazierBlock> RIFT_BRAZIER = PoptartCatalog.block(
                    "rift_brazier", () -> new RiftBrazierBlock(riftBrazierProperties()))
            .externalModel()
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<RiftStoneBrazierBlock> RIFT_STONE_BRAZIER = PoptartCatalog.block(
                    "rift_stone_brazier", () -> new RiftStoneBrazierBlock(stoneBrazierProperties(15)))
            .externalModel()
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<CupricBrazierBlock> CUPRIC_BRAZIER = PoptartCatalog.block(
                    "cupric_brazier", () -> new CupricBrazierBlock(cupricBrazierProperties()))
            .externalModel()
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<CupricStoneBrazierBlock> CUPRIC_STONE_BRAZIER = PoptartCatalog.block(
                    "cupric_stone_brazier", () -> new CupricStoneBrazierBlock(stoneBrazierProperties(10)))
            .externalModel()
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<SconceLeverBlock> SCONCE_TORCH_LEVER = PoptartCatalog.block(
                    "sconce_torch_lever",
                    () -> new SconceLeverBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LEVER)
                            .noOcclusion()
                            .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 14 : 0),
                            () -> ParticleTypes.FLAME))
            .externalModel()
            .customLoot();
    public static final CatalogBlockDefinition<RiftSconceLeverBlock> SCONCE_RIFT_TORCH_LEVER = PoptartCatalog.block(
                    "sconce_rift_torch_lever",
                    () -> new RiftSconceLeverBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LEVER)
                            .noOcclusion()
                            .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 15 : 0)))
            .externalModel();
    public static final CatalogBlockDefinition<CupricSconceLeverBlock> SCONCE_CUPRIC_TORCH_LEVER = PoptartCatalog.block(
                    "sconce_cupric_torch_lever",
                    () -> new CupricSconceLeverBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LEVER)
                            .noOcclusion()
                            .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 10 : 0)))
            .externalModel();
    public static final CatalogBlockDefinition<SoulSconceLeverBlock> SCONCE_SOUL_TORCH_LEVER = PoptartCatalog.block(
                    "sconce_soul_torch_lever",
                    () -> new SoulSconceLeverBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LEVER)
                            .noOcclusion()
                            .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 10 : 0)))
            .externalModel();

    public static final CatalogBlockDefinition<CasingBlock> INDUSTRIAL_PLATING = PoptartCatalog.block(
                    "industrial_plating",
                    () -> new CasingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(4.0F, 6.0F)))
            .simpleModel(PoptartCore.location("block/industrial_plating"), "")
            .mineableWithPickaxe();
    public static final DeferredBlock<EncasedShaftBlock> INDUSTRIAL_ENCASED_SHAFT = BLOCKS.register(
            "industrial_encased_shaft",
            () -> new EncasedShaftBlock(industrialEncasedProperties(), INDUSTRIAL_PLATING::get));
    public static final DeferredBlock<EncasedCogwheelBlock> INDUSTRIAL_ENCASED_COGWHEEL = BLOCKS.register(
            "industrial_encased_cogwheel",
            () -> new EncasedCogwheelBlock(industrialEncasedProperties(), false, INDUSTRIAL_PLATING::get));
    public static final DeferredBlock<EncasedCogwheelBlock> INDUSTRIAL_ENCASED_LARGE_COGWHEEL = BLOCKS.register(
            "industrial_encased_large_cogwheel",
            () -> new EncasedCogwheelBlock(industrialEncasedProperties(), true, INDUSTRIAL_PLATING::get));
    public static final CatalogBlockDefinition<CasingBlock> TREATED_WOOD = PoptartCatalog.block(
                    "treated_wood",
                    () -> new CasingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS)
                            .sound(SoundType.WOOD)
                            .strength(2.0F, 3.0F)))
            .simpleModel(PoptartCore.location("block/treated_wood"), "")
            .mineableWithAxe();
    public static final DeferredBlock<EncasedShaftBlock> TREATED_WOOD_ENCASED_SHAFT = BLOCKS.register(
            "treated_wood_encased_shaft",
            () -> new EncasedShaftBlock(treatedWoodEncasedProperties(), TREATED_WOOD::get));
    public static final DeferredBlock<EncasedCogwheelBlock> TREATED_WOOD_ENCASED_COGWHEEL = BLOCKS.register(
            "treated_wood_encased_cogwheel",
            () -> new EncasedCogwheelBlock(treatedWoodEncasedProperties(), false, TREATED_WOOD::get));
    public static final DeferredBlock<EncasedCogwheelBlock> TREATED_WOOD_ENCASED_LARGE_COGWHEEL = BLOCKS.register(
            "treated_wood_encased_large_cogwheel",
            () -> new EncasedCogwheelBlock(treatedWoodEncasedProperties(), true, TREATED_WOOD::get));

    public static final CatalogBlockDefinition<MillstoneBlock> MILLSTONE = PoptartCatalog.block(
                    "millstone", () -> new MillstoneBlock(millstoneProperties()), MillstoneBlockItem::new)
            .externalModel()
            .mineableWithPickaxe();
    public static final DeferredBlock<MillstoneStructuralBlock> MILLSTONE_STRUCTURAL = BLOCKS.register(
            "millstone_structural",
            () -> new MillstoneStructuralBlock(millstoneProperties().noLootTable()));
    public static final DeferredBlock<MillstoneRotorBlock> MILLSTONE_ROTOR = BLOCKS.register(
            "millstone_rotor",
            () -> new MillstoneRotorBlock(millstoneProperties().noOcclusion().noLootTable()));

    private static BlockBehaviour.Properties millstoneProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(3.0F)
                .pushReaction(PushReaction.BLOCK);
    }

    public static final CatalogBlockDefinition<CrucibleBlock> CRUCIBLE = PoptartCatalog.block(
                    "crucible",
                    () -> new CrucibleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAMPFIRE)
                            .sound(SoundType.MUD_BRICKS)
                            .lightLevel(state -> {
                                if (state.getValue(CrucibleBlock.LIT)) {
                                    return 15;
                                }
                                return state.getValue(CrucibleBlock.FLUID_LEVEL) > 0 ? 8 : 0;
                            })),
                    CrucibleBlockItem::new)
            .externalModel()
            .mineableWithAxe()
            .mineableWithPickaxe();

    public static final CatalogBlockDefinition<BlastFurnaceBlock> BLAST_FURNACE = PoptartCatalog.block(
                    "blast_furnace",
                    () -> new BlastFurnaceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BLAST_FURNACE)
                            .lightLevel(state -> state.getValue(BlastFurnaceBlock.LIT) ? 13 : 0)))
            .externalModel()
            .mineableWithPickaxe();

    public static final CatalogBlockDefinition<FluidBarrelBlock> FLUID_BARREL = PoptartCatalog.block(
                    "fluid_barrel",
                    () -> new FluidBarrelBlock(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).noOcclusion()),
                    FluidBarrelBlockItem::new)
            .externalModel()
            .customLoot()
            .mineableWithAxe();

    public static final CatalogBlockDefinition<BloomeryBlock> BLOOMERY = PoptartCatalog.block(
                    "bloomery", () -> new BloomeryBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MUD_BRICKS)))
            .externalModel()
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<WorkbenchBlock> WORKBENCH = PoptartCatalog.block(
                    "workbench", () -> new WorkbenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)))
            .externalModel()
            .mineableWithAxe();
    public static final CatalogBlockDefinition<QuernBlock> QUERN = PoptartCatalog.block(
                    "quern",
                    () -> new QuernBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONECUTTER)
                            .noOcclusion()))
            .externalModel()
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<PortableEngineBlock> PORTABLE_ENGINE = PoptartCatalog.block(
                    "portable_engine",
                    () -> new PortableEngineBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.TERRACOTTA_WHITE)
                                    .requiresCorrectToolForDrops()
                                    .strength(3.0F, 6.0F)
                                    .sound(SoundType.COPPER)
                                    .lightLevel(state -> PortableEngineBlock.isLitState(state) ? 6 : 0)
                                    .noOcclusion(),
                            null))
            .externalModel()
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<IronBloomBlock> IRON_BLOOM = PoptartCatalog.block(
                    "iron_bloom",
                    () -> new IronBloomBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)
                            .noOcclusion()
                            .noLootTable()))
            .externalModel()
            .noLoot()
            .mineableWithPickaxe();

    public static final CatalogBlockDefinition<RiftSedimentBlock> RIFT_SEDIMENT = PoptartCatalog.block(
                    "rift_sediment",
                    () -> new RiftSedimentBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .noCollission()
                            .instabreak()
                            .sound(SoundType.SAND)
                            .lightLevel(state -> state.getValue(RiftSedimentBlock.LIT) ? 7 : 0)
                            .pushReaction(PushReaction.DESTROY)))
            .externalModel()
            .noLoot();
    public static final CatalogBlockDefinition<PackedRiftSedimentBlock> RIFT_SEDIMENT_BLOCK = PoptartCatalog.block(
                    "rift_sediment_block",
                    () -> new PackedRiftSedimentBlock(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.SAND).mapColor(MapColor.COLOR_PURPLE)))
            .withName("Block of Rift Sediment")
            .withStorageRecipes(PoptartCoreBlocks.RIFT_SEDIMENT::get, "rift_sediment_from_block")
            .simpleModel(PoptartCore.location("block/rift_sediment_block"), "")
            .tags(BlockTags.MINEABLE_WITH_SHOVEL);

    public static final CatalogBlockDefinition<DecayingFleshBlock> FLESH = PoptartCatalog.block(
                    "flesh",
                    () -> new DecayingFleshBlock(
                            () -> PoptartCoreBlocks.ROTTEN_FLESH.get(),
                            BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK)
                                    .mapColor(MapColor.NETHER)
                                    .randomTicks()
                                    .noLootTable()))
            .externalModel()
            .noLoot()
            .tags(BlockTags.MINEABLE_WITH_HOE);
    public static final CatalogBlockDefinition<DecayingFleshBlock> FRESH_FLESH = PoptartCatalog.block(
                    "fresh_flesh",
                    () -> new DecayingFleshBlock(
                            FLESH::get,
                            BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK)
                                    .mapColor(MapColor.COLOR_RED)
                                    .randomTicks()
                                    .noLootTable()))
            .externalModel()
            .noLoot()
            .tags(BlockTags.MINEABLE_WITH_HOE);
    public static final CatalogBlockDefinition<Block> ROTTEN_FLESH = PoptartCatalog.block(
                    "rotten_flesh",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK)
                            .mapColor(MapColor.COLOR_BROWN)
                            .noLootTable()))
            .externalModel()
            .noLoot()
            .tags(BlockTags.MINEABLE_WITH_HOE);
    public static final CatalogBlockDefinition<DecayingFleshBlock> BUDDING_FLESH = PoptartCatalog.block(
                    "budding_flesh",
                    () -> new DecayingFleshBlock(
                            FRESH_FLESH::get,
                            BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK)
                                    .mapColor(MapColor.COLOR_RED)
                                    .randomTicks()
                                    .noLootTable()))
            .externalModel()
            .noLoot()
            .tags(BlockTags.MINEABLE_WITH_HOE);
    public static final DeferredBlock<LifebudBlock> LIFEBUD = BLOCKS.register(
            "lifebud",
            () -> new LifebudBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .instabreak()
                    .sound(SoundType.HONEY_BLOCK)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    public static final CatalogBlockDefinition<Block> TIN_BLOCK = PoptartCatalog.block(
                    "tin_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(1.0F, 6.0F)))
            .withName("Block of Tin")
            .withStorageRecipes(() -> PoptartCoreItems.TIN_INGOT.get(), "tin_ingots_from_block")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<Block> TIN_ORE = PoptartCatalog.block(
                    "tin_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_ORE)
                            .strength(2.0F, 6.0F)))
            .withOreDrop(() -> PoptartCoreItems.RAW_TIN.get())
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<Block> DEEPSLATE_TIN_ORE = PoptartCatalog.block(
                    "deepslate_tin_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_COPPER_ORE)
                            .strength(3.0F, 6.0F)))
            .withOreDrop(() -> PoptartCoreItems.RAW_TIN.get())
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<Block> RAW_TIN_BLOCK = PoptartCatalog.block(
                    "raw_tin_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_COPPER_BLOCK)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Raw Tin")
            .withStorageRecipes(() -> PoptartCoreItems.RAW_TIN.get(), "raw_tin_from_block")
            .mineableWithPickaxe();

    public static final CatalogBlockDefinition<Block> LEAD_BLOCK = PoptartCatalog.block(
                    "lead_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(1.0F, 6.0F)))
            .withName("Block of Lead")
            .withStorageRecipes(() -> PoptartCoreItems.LEAD_INGOT.get(), "lead_ingots_from_block")
            .requiresIronTool();
    public static final CatalogBlockDefinition<Block> LEAD_ORE = PoptartCatalog.block(
                    "lead_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .strength(2.0F, 6.0F)))
            .withOreDrop(() -> PoptartCoreItems.RAW_LEAD.get())
            .requiresIronTool();
    public static final CatalogBlockDefinition<Block> DEEPSLATE_LEAD_ORE = PoptartCatalog.block(
                    "deepslate_lead_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                            .sound(SoundType.DEEPSLATE)
                            .requiresCorrectToolForDrops()
                            .strength(3.0F, 6.0F)))
            .withOreDrop(() -> PoptartCoreItems.RAW_LEAD.get())
            .requiresIronTool();
    public static final CatalogBlockDefinition<Block> RAW_LEAD_BLOCK = PoptartCatalog.block(
                    "raw_lead_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Raw Lead")
            .withStorageRecipes(() -> PoptartCoreItems.RAW_LEAD.get(), "raw_lead_from_block")
            .requiresIronTool();

    public static final CatalogBlockDefinition<Block> MAGNETITE_BLOCK = PoptartCatalog.block(
                    "magnetite_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Magnetite")
            .externalModel()
            .customLoot()
            .requiresStoneTool();

    public static final CatalogBlockDefinition<Block> CINNABAR_BLOCK = PoptartCatalog.block(
                    "cinnabar_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)
                            .strength(4.0F, 3.0F)))
            .withName("Block of Cinnabar")
            .externalModel()
            .customLoot()
            .requiresIronTool();

    public static final CatalogBlockDefinition<Block> MERCURY_BLOCK = PoptartCatalog.block(
                    "mercury_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Mercury")
            .externalModel()
            .customLoot()
            .requiresIronTool();

    public static final CatalogBlockDefinition<Block> SILVER_BLOCK = PoptartCatalog.block(
                    "silver_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Silver")
            .withStorageRecipes(() -> PoptartCoreItems.SILVER_INGOT.get(), "silver_ingots_from_block")
            .requiresIronTool();
    public static final CatalogBlockDefinition<Block> SILVER_ORE = PoptartCatalog.block(
                    "silver_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
                            .sound(SoundType.STONE)
                            .requiresCorrectToolForDrops()
                            .strength(5.0F, 6.0F)))
            .withOreDrop(() -> PoptartCoreItems.RAW_SILVER.get())
            .requiresIronTool();
    public static final CatalogBlockDefinition<Block> DEEPSLATE_SILVER_ORE = PoptartCatalog.block(
                    "deepslate_silver_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)
                            .sound(SoundType.DEEPSLATE)
                            .requiresCorrectToolForDrops()
                            .strength(6.0F, 6.0F)))
            .withOreDrop(() -> PoptartCoreItems.RAW_SILVER.get())
            .requiresIronTool();
    public static final CatalogBlockDefinition<Block> RAW_SILVER_BLOCK = PoptartCatalog.block(
                    "raw_silver_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Raw Silver")
            .withStorageRecipes(() -> PoptartCoreItems.RAW_SILVER.get(), "raw_silver_from_block")
            .requiresIronTool();

    public static final CatalogBlockDefinition<Block> TITANIUM_ORE = PoptartCatalog.block(
                    "titanium_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE)
                            .strength(3.0F, 6.0F)))
            .withOreDrop(() -> PoptartCoreItems.RAW_TITANIUM.get())
            .requiresIronTool();
    public static final CatalogBlockDefinition<Block> DEEPSLATE_TITANIUM_ORE = PoptartCatalog.block(
                    "deepslate_titanium_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE)
                            .strength(4.5F, 6.0F)))
            .withOreDrop(() -> PoptartCoreItems.RAW_TITANIUM.get())
            .requiresIronTool();
    public static final CatalogBlockDefinition<Block> CASSITERITE_TITANIUM_ORE = PoptartCatalog.block(
                    "cassiterite_titanium_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE)
                            .strength(3.0F, 6.0F)
                            .sound(CCSoundTypes.CASSITERITE)))
            .withName("Cassiterite Titanium Ore")
            .withOreDrop(() -> PoptartCoreItems.RAW_TITANIUM.get())
            .requiresIronTool();
    public static final CatalogBlockDefinition<Block> CYLINDRITE_TITANIUM_ORE = PoptartCatalog.block(
                    "cylindrite_titanium_ore",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE)
                            .strength(4.5F, 6.0F)
                            .sound(CCSoundTypes.CYLINDRITE)))
            .withName("Cylindrite Titanium Ore")
            .withOreDrop(() -> PoptartCoreItems.RAW_TITANIUM.get())
            .requiresIronTool();
    public static final CatalogBlockDefinition<Block> RAW_TITANIUM_BLOCK = PoptartCatalog.block(
                    "raw_titanium_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)
                            .strength(5.0F, 6.0F)))
            .withName("Block of Raw Titanium")
            .requiresIronTool();

    public static final CatalogBlockDefinition<Block> BRONZE_BRICKS = PoptartCatalog.block(
                    "bronze_bricks", () -> new Block(platedBrickProperties(CCBlocks.COPPER_BRICKS.get())))
            .withName("Bronze Plated Bricks")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<Block> CONCRETE = PoptartCatalog.block(
                    "concrete", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_GRAY_CONCRETE)))
            .externalModel()
            .customLoot()
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<SlabBlock> CONCRETE_SLAB = PoptartCatalog.block(
                    "concrete_slab",
                    () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_GRAY_CONCRETE)))
            .externalModel()
            .customLoot()
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<StairBlock> CONCRETE_STAIRS = PoptartCatalog.block(
                    "concrete_stairs",
                    () -> new StairBlock(
                            CONCRETE.get().defaultBlockState(),
                            BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_GRAY_CONCRETE)))
            .externalModel()
            .customLoot()
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<ColoredFallingBlock> CONCRETE_POWDER = PoptartCatalog.block(
                    "concrete_powder",
                    () -> new ColoredFallingBlock(
                            new ColorRGBA(15658734),
                            BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_GRAY_CONCRETE_POWDER)))
            .externalModel()
            .customLoot()
            .tags(BlockTags.MINEABLE_WITH_SHOVEL);
    public static final CatalogBlockDefinition<ScribingTableBlock> SCRIBING_TABLE = PoptartCatalog.block(
                    "scribing_table",
                    () -> new ScribingTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)))
            .externalModel()
            .mineableWithAxe();
    public static final CatalogBlockDefinition<Block> CHISELED_BRONZE_BRICKS = PoptartCatalog.block(
                    "chiseled_bronze_bricks",
                    () -> new Block(platedBrickProperties(CCBlocks.CHISELED_COPPER_BRICKS.get())))
            .withName("Chiseled Bronze Plated Bricks")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<SlabBlock> BRONZE_BRICK_SLAB = PoptartCatalog.block(
                    "bronze_brick_slab", () -> new SlabBlock(platedBrickProperties(CCBlocks.COPPER_BRICK_SLAB.get())))
            .withName("Bronze Plated Brick Slab")
            .slabModel(PoptartCore.location("block/bronze_bricks"), "")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<StairBlock> BRONZE_BRICK_STAIRS = PoptartCatalog.block(
                    "bronze_brick_stairs",
                    () -> new StairBlock(
                            BRONZE_BRICKS.get().defaultBlockState(),
                            platedBrickProperties(CCBlocks.COPPER_BRICK_STAIRS.get())))
            .withName("Bronze Plated Brick Stairs")
            .stairsModel(PoptartCore.location("block/bronze_bricks"), "")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<WallBlock> BRONZE_BRICK_WALL = PoptartCatalog.block(
                    "bronze_brick_wall", () -> new WallBlock(platedBrickProperties(CCBlocks.COPPER_BRICK_WALL.get())))
            .withName("Bronze Plated Brick Wall")
            .wallModel(PoptartCore.location("block/bronze_bricks"), "")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS);

    public static final CatalogBlockDefinition<Block> AGED_BRONZE_BRICKS = PoptartCatalog.block(
                    "aged_bronze_bricks", () -> new Block(platedBrickProperties(CCBlocks.COPPER_BRICKS.get())))
            .withName("Aged Bronze Plated Bricks")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<Block> CHISELED_AGED_BRONZE_BRICKS = PoptartCatalog.block(
                    "chiseled_aged_bronze_bricks",
                    () -> new Block(platedBrickProperties(CCBlocks.CHISELED_COPPER_BRICKS.get())))
            .withName("Chiseled Aged Bronze Plated Bricks")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<SlabBlock> AGED_BRONZE_BRICK_SLAB = PoptartCatalog.block(
                    "aged_bronze_brick_slab",
                    () -> new SlabBlock(platedBrickProperties(CCBlocks.COPPER_BRICK_SLAB.get())))
            .withName("Aged Bronze Plated Brick Slab")
            .slabModel(PoptartCore.location("block/aged_bronze_bricks"), "")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<StairBlock> AGED_BRONZE_BRICK_STAIRS = PoptartCatalog.block(
                    "aged_bronze_brick_stairs",
                    () -> new StairBlock(
                            AGED_BRONZE_BRICKS.get().defaultBlockState(),
                            platedBrickProperties(CCBlocks.COPPER_BRICK_STAIRS.get())))
            .withName("Aged Bronze Plated Brick Stairs")
            .stairsModel(PoptartCore.location("block/aged_bronze_bricks"), "")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<WallBlock> AGED_BRONZE_BRICK_WALL = PoptartCatalog.block(
                    "aged_bronze_brick_wall",
                    () -> new WallBlock(platedBrickProperties(CCBlocks.COPPER_BRICK_WALL.get())))
            .withName("Aged Bronze Plated Brick Wall")
            .wallModel(PoptartCore.location("block/aged_bronze_bricks"), "")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS);

    public static final CatalogBlockDefinition<Block> LEAD_BRICKS = PoptartCatalog.block(
                    "lead_bricks", () -> new Block(platedBrickProperties(CCBlocks.TIN_BRICKS.get())))
            .withName("Lead Plated Bricks")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<Block> CHISELED_LEAD_BRICKS = PoptartCatalog.block(
                    "chiseled_lead_bricks", () -> new Block(platedBrickProperties(CCBlocks.CHISELED_TIN_BRICKS.get())))
            .withName("Chiseled Lead Plated Bricks")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<SlabBlock> LEAD_BRICK_SLAB = PoptartCatalog.block(
                    "lead_brick_slab", () -> new SlabBlock(platedBrickProperties(CCBlocks.TIN_BRICK_SLAB.get())))
            .withName("Lead Plated Brick Slab")
            .slabModel(PoptartCore.location("block/lead_bricks"), "")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<StairBlock> LEAD_BRICK_STAIRS = PoptartCatalog.block(
                    "lead_brick_stairs",
                    () -> new StairBlock(
                            LEAD_BRICKS.get().defaultBlockState(),
                            platedBrickProperties(CCBlocks.TIN_BRICK_STAIRS.get())))
            .withName("Lead Plated Brick Stairs")
            .stairsModel(PoptartCore.location("block/lead_bricks"), "")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<WallBlock> LEAD_BRICK_WALL = PoptartCatalog.block(
                    "lead_brick_wall", () -> new WallBlock(platedBrickProperties(CCBlocks.TIN_BRICK_WALL.get())))
            .withName("Lead Plated Brick Wall")
            .wallModel(PoptartCore.location("block/lead_bricks"), "")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS);

    public static final CatalogBlockDefinition<Block> MERCURY_BRICKS = PoptartCatalog.block(
                    "mercury_bricks", () -> new Block(platedBrickProperties(CCBlocks.SILVER_BRICKS.get())))
            .withName("Mercury Plated Bricks")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<Block> CHISELED_MERCURY_BRICKS = PoptartCatalog.block(
                    "chiseled_mercury_bricks",
                    () -> new Block(platedBrickProperties(CCBlocks.CHISELED_SILVER_BRICKS.get())))
            .withName("Chiseled Mercury Plated Bricks")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<SlabBlock> MERCURY_BRICK_SLAB = PoptartCatalog.block(
                    "mercury_brick_slab", () -> new SlabBlock(platedBrickProperties(CCBlocks.SILVER_BRICK_SLAB.get())))
            .withName("Mercury Plated Brick Slab")
            .slabModel(PoptartCore.location("block/mercury_bricks"), "")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<StairBlock> MERCURY_BRICK_STAIRS = PoptartCatalog.block(
                    "mercury_brick_stairs",
                    () -> new StairBlock(
                            MERCURY_BRICKS.get().defaultBlockState(),
                            platedBrickProperties(CCBlocks.SILVER_BRICK_STAIRS.get())))
            .withName("Mercury Plated Brick Stairs")
            .stairsModel(PoptartCore.location("block/mercury_bricks"), "")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<WallBlock> MERCURY_BRICK_WALL = PoptartCatalog.block(
                    "mercury_brick_wall", () -> new WallBlock(platedBrickProperties(CCBlocks.SILVER_BRICK_WALL.get())))
            .withName("Mercury Plated Brick Wall")
            .wallModel(PoptartCore.location("block/mercury_bricks"), "")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS);

    public static final CatalogBlockDefinition<Block> STEEL_BRICKS = PoptartCatalog.block(
                    "steel_bricks", () -> new Block(platedBrickProperties(CCBlocks.IRON_BRICKS.get())))
            .withName("Steel Plated Bricks")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<Block> CHISELED_STEEL_BRICKS = PoptartCatalog.block(
                    "chiseled_steel_bricks",
                    () -> new Block(platedBrickProperties(CCBlocks.CHISELED_IRON_BRICKS.get())))
            .withName("Chiseled Steel Plated Bricks")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<SlabBlock> STEEL_BRICK_SLAB = PoptartCatalog.block(
                    "steel_brick_slab", () -> new SlabBlock(platedBrickProperties(CCBlocks.IRON_BRICK_SLAB.get())))
            .withName("Steel Plated Brick Slab")
            .slabModel(PoptartCore.location("block/steel_bricks"), "")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<StairBlock> STEEL_BRICK_STAIRS = PoptartCatalog.block(
                    "steel_brick_stairs",
                    () -> new StairBlock(
                            STEEL_BRICKS.get().defaultBlockState(),
                            platedBrickProperties(CCBlocks.IRON_BRICK_STAIRS.get())))
            .withName("Steel Plated Brick Stairs")
            .stairsModel(PoptartCore.location("block/steel_bricks"), "")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<WallBlock> STEEL_BRICK_WALL = PoptartCatalog.block(
                    "steel_brick_wall", () -> new WallBlock(platedBrickProperties(CCBlocks.IRON_BRICK_WALL.get())))
            .withName("Steel Plated Brick Wall")
            .wallModel(PoptartCore.location("block/steel_bricks"), "")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS);
    public static final CatalogBlockDefinition<Block> TITANIUM_BRICKS = PoptartCatalog.block(
                    "titanium_bricks", () -> new Block(titaniumBrickProperties()))
            .withName("Titanium Plated Bricks")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<Block> CHISELED_TITANIUM_BRICKS = PoptartCatalog.block(
                    "chiseled_titanium_bricks", () -> new Block(titaniumBrickProperties()))
            .withName("Chiseled Titanium Plated Bricks")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<SlabBlock> TITANIUM_BRICK_SLAB = PoptartCatalog.block(
                    "titanium_brick_slab", () -> new SlabBlock(titaniumBrickProperties()))
            .withName("Titanium Plated Brick Slab")
            .slabModel(PoptartCore.location("block/titanium_bricks"), "")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<StairBlock> TITANIUM_BRICK_STAIRS = PoptartCatalog.block(
                    "titanium_brick_stairs",
                    () -> new StairBlock(TITANIUM_BRICKS.get().defaultBlockState(), titaniumBrickProperties()))
            .withName("Titanium Plated Brick Stairs")
            .stairsModel(PoptartCore.location("block/titanium_bricks"), "")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<WallBlock> TITANIUM_BRICK_WALL = PoptartCatalog.block(
                    "titanium_brick_wall", () -> new WallBlock(titaniumBrickProperties()))
            .withName("Titanium Plated Brick Wall")
            .wallModel(PoptartCore.location("block/titanium_bricks"), "")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS);

    public static final CatalogBlockDefinition<Block> WAX_BLOCK = PoptartCatalog.block(
                    "wax_block",
                    () -> new Block(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.MUD).strength(0.7F, 0.3F)))
            .withStorageRecipes(() -> PoptartCoreItems.WAX.get(), "wax_from_block");
    public static final CatalogBlockDefinition<TreeTapBlock> TREE_TAP = PoptartCatalog.block(
                    "tree_tap",
                    () -> new TreeTapBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE)
                            .noOcclusion()
                            .strength(0.8F)))
            .externalModel()
            .customLoot()
            .mineableWithAxe();
    public static final DeferredBlock<LatexCauldronBlock> LATEX_CAULDRON = BLOCKS.register(
            "latex_cauldron",
            () -> new LatexCauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WATER_CAULDRON)));
    public static final CatalogBlockDefinition<BeehiveSupportBlock> BEEHIVE_SUPPORT = PoptartCatalog.block(
                    "beehive_support",
                    () -> new BeehiveSupportBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEEHIVE)
                            .noOcclusion()
                            .strength(0.8F)))
            .externalModel()
            .customLoot()
            .mineableWithAxe();
    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_WHITE_ROOF = beehiveRoof("white");
    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_LIGHT_GRAY_ROOF = beehiveRoof("light_gray");
    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_GRAY_ROOF = beehiveRoof("gray");
    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_BLACK_ROOF = beehiveRoof("black");
    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_BROWN_ROOF = beehiveRoof("brown");
    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_RED_ROOF = beehiveRoof("red");
    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_ORANGE_ROOF = beehiveRoof("orange");
    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_YELLOW_ROOF = beehiveRoof("yellow");
    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_LIME_ROOF = beehiveRoof("lime");
    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_GREEN_ROOF = beehiveRoof("green");
    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_CYAN_ROOF = beehiveRoof("cyan");
    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_LIGHT_BLUE_ROOF = beehiveRoof("light_blue");
    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_BLUE_ROOF = beehiveRoof("blue");
    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_PURPLE_ROOF = beehiveRoof("purple");
    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_MAGENTA_ROOF = beehiveRoof("magenta");
    public static final CatalogBlockDefinition<BeehiveRoofBlock> BEEHIVE_PINK_ROOF = beehiveRoof("pink");

    public static BeehiveRoofBlock beehiveRoofForDye(DyeColor color) {
        return switch (color) {
            case WHITE -> BEEHIVE_WHITE_ROOF.get();
            case LIGHT_GRAY -> BEEHIVE_LIGHT_GRAY_ROOF.get();
            case GRAY -> BEEHIVE_GRAY_ROOF.get();
            case BLACK -> BEEHIVE_BLACK_ROOF.get();
            case BROWN -> BEEHIVE_BROWN_ROOF.get();
            case RED -> BEEHIVE_RED_ROOF.get();
            case ORANGE -> BEEHIVE_ORANGE_ROOF.get();
            case YELLOW -> BEEHIVE_YELLOW_ROOF.get();
            case LIME -> BEEHIVE_LIME_ROOF.get();
            case GREEN -> BEEHIVE_GREEN_ROOF.get();
            case CYAN -> BEEHIVE_CYAN_ROOF.get();
            case LIGHT_BLUE -> BEEHIVE_LIGHT_BLUE_ROOF.get();
            case BLUE -> BEEHIVE_BLUE_ROOF.get();
            case PURPLE -> BEEHIVE_PURPLE_ROOF.get();
            case MAGENTA -> BEEHIVE_MAGENTA_ROOF.get();
            case PINK -> BEEHIVE_PINK_ROOF.get();
        };
    }
    public static final CatalogBlockDefinition<Block> COAL_COKE_BLOCK = PoptartCatalog.block(
                    "coal_coke_block", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_BLOCK)))
            .withStorageRecipes(() -> PoptartCoreItems.COAL_COKE.get(), "coal_coke_from_block")
            .fuelBurnTime(28800)
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<Block> BRONZE_BLOCK = PoptartCatalog.block(
                    "bronze_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(3.0F, 6.0F)))
            .withName("Block of Bronze")
            .withStorageRecipes(() -> PoptartCoreItems.BRONZE_INGOT.get(), "bronze_ingot_from_block")
            .requiresStoneTool();
    public static final CatalogBlockDefinition<Block> STEEL_BLOCK = PoptartCatalog.block(
                    "steel_block",
                    () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .sound(SoundType.METAL)
                            .strength(7.0F, 8.0F)))
            .withName("Block of Steel")
            .withStorageRecipes(() -> PoptartCoreItems.STEEL_INGOT.get(), "steel_ingot_from_block")
            .requiresIronTool();

    public static final CatalogBlockDefinition<Block> CLINKER_BRICKS = PoptartCatalog.block(
                    "clinker_bricks", () -> new Block(clinkerProperties()))
            .randomCubeModel(PoptartCore.location("block/clinker/clinker_bricks"), "clinker", 12)
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<SlabBlock> CLINKER_BRICK_SLAB = PoptartCatalog.block(
                    "clinker_brick_slab", () -> new SlabBlock(clinkerProperties()))
            .slabModel(PoptartCore.location("block/clinker/clinker_bricks_1"), "clinker")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<StairBlock> CLINKER_BRICK_STAIRS = PoptartCatalog.block(
                    "clinker_brick_stairs",
                    () -> new StairBlock(CLINKER_BRICKS.get().defaultBlockState(), clinkerProperties()))
            .stairsModel(PoptartCore.location("block/clinker/clinker_bricks_1"), "clinker")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<WallBlock> CLINKER_BRICK_WALL = PoptartCatalog.block(
                    "clinker_brick_wall", () -> new WallBlock(clinkerProperties()))
            .wallModel(PoptartCore.location("block/clinker/clinker_bricks_1"), "clinker")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS);
    public static final CatalogBlockDefinition<Block> CLINKER_TILE = PoptartCatalog.block(
                    "clinker_tile", () -> new Block(clinkerProperties()))
            .simpleModel(PoptartCore.location("block/clinker/clinker_tile"), "clinker")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<SlabBlock> CLINKER_TILE_SLAB = PoptartCatalog.block(
                    "clinker_tile_slab", () -> new SlabBlock(clinkerProperties()))
            .slabModel(PoptartCore.location("block/clinker/clinker_tile"), "clinker")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<StairBlock> CLINKER_TILE_STAIRS = PoptartCatalog.block(
                    "clinker_tile_stairs",
                    () -> new StairBlock(CLINKER_TILE.get().defaultBlockState(), clinkerProperties()))
            .stairsModel(PoptartCore.location("block/clinker/clinker_tile"), "clinker")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<WallBlock> CLINKER_TILE_WALL = PoptartCatalog.block(
                    "clinker_tile_wall", () -> new WallBlock(clinkerProperties()))
            .wallModel(PoptartCore.location("block/clinker/clinker_tile"), "clinker")
            .mineableWithPickaxe()
            .tags(BlockTags.WALLS);
    public static final CatalogBlockDefinition<Block> MOSAIC_CLINKER_TILE = PoptartCatalog.block(
                    "mosaic_clinker_tile", () -> new Block(clinkerProperties()))
            .randomBottomTopModel(
                    PoptartCore.location("block/clinker/mosaic_clinker_tile"),
                    PoptartCore.location("block/clinker/mosaic_clinker_tile_top"),
                    PoptartCore.location("block/clinker/mosaic_clinker_tile_bottom"),
                    "clinker",
                    4)
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<Block> CHISELED_CLINKER_TILE = PoptartCatalog.block(
                    "chiseled_clinker_tile", () -> new Block(clinkerProperties()))
            .simpleModel(PoptartCore.location("block/clinker/chiseled_clinker_tile"), "clinker")
            .mineableWithPickaxe();
    public static final CatalogBlockDefinition<ClinkerPillarBlock> CLINKER_PILLAR = PoptartCatalog.block(
                    "clinker_pillar", () -> new ClinkerPillarBlock(clinkerProperties()))
            .externalModel()
            .mineableWithPickaxe();

    private static BlockBehaviour.Properties clinkerProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS);
    }

    private static CatalogBlockDefinition<BeehiveRoofBlock> beehiveRoof(String color) {
        return PoptartCatalog.block(
                        "beehive_" + color + "_roof",
                        () -> new BeehiveRoofBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEEHIVE)
                                .noOcclusion()
                                .strength(0.8F)))
                .externalModel()
                .customLoot()
                .mineableWithAxe();
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

    private static BlockBehaviour.Properties riftBrazierProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN)
                .strength(4.0F, 8.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 15 : 0);
    }

    private static BlockBehaviour.Properties cupricBrazierProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_LANTERN)
                .strength(4.0F, 8.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 10 : 0);
    }

    private static BlockBehaviour.Properties stoneBrazierProperties(int light) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(4.0F, 8.0F)
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .ignitedByLava()
                .instrument(NoteBlockInstrument.BASS)
                .sound(SoundType.POLISHED_DEEPSLATE)
                .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? light : 0);
    }

    private static BlockBehaviour.Properties industrialEncasedProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                .sound(SoundType.METAL)
                .strength(4.0F, 6.0F)
                .noOcclusion();
    }

    private static BlockBehaviour.Properties treatedWoodEncasedProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS)
                .sound(SoundType.WOOD)
                .strength(2.0F, 3.0F)
                .noOcclusion();
    }

    public static void initialize() {}

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
