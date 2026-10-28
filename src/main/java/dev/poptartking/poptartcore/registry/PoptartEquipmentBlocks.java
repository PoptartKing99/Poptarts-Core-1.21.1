package dev.poptartking.poptartcore.registry;

import static dev.poptartking.poptartcore.catalog.CreativeCatalogGroup.EQUIPMENT;

import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class PoptartEquipmentBlocks {
    public static final CatalogBlockDefinition<dev.poptartking.poptartcore.backpack.RucksackBlock> RUCKSACK =
            PoptartCatalog.block(
                            "rucksack",
                            () -> new dev.poptartking.poptartcore.backpack.RucksackBlock(
                                    BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_WOOL)
                                            .strength(0.8F)
                                            .noOcclusion()),
                            dev.poptartking.poptartcore.backpack.BackpackItem::new)
                    .externalModel()
                    .customLoot()
                    .itemTags(PoptartCoreTags.BACKPACKS)
                    .creative(EQUIPMENT, 140);

    public static final CatalogBlockDefinition<dev.poptartking.poptartcore.backpack.BackpackBlock> BACKPACK =
            PoptartCatalog.block(
                            "backpack",
                            () -> new dev.poptartking.poptartcore.backpack.BackpackBlock(
                                    BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_WOOL)
                                            .strength(0.8F)
                                            .noOcclusion()),
                            dev.poptartking.poptartcore.backpack.BackpackItem::new)
                    .externalModel()
                    .customLoot()
                    .itemTags(PoptartCoreTags.BACKPACKS)
                    .creative(EQUIPMENT, 130);

    private PoptartEquipmentBlocks() {}

    public static void initialize() {}
}
