package dev.poptartking.poptartcore.catalog.datagen;

import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class CatalogBlockLootProvider extends BlockLootSubProvider {
    public CatalogBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.<Item>of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        for (CatalogBlockDefinition<?> definition : PoptartCatalog.blocks()) {
            if (!definition.generatedLoot()) {
                continue;
            }
            if (definition.model() == dev.poptartking.poptartcore.catalog.CatalogBlockModel.SLAB) {
                add(definition.get(), createSlabItemTable(definition.get()));
            } else if (definition.oreDrop() == null) {
                dropSelf(definition.get());
            } else {
                add(
                        definition.get(),
                        block -> createOreDrop(block, definition.oreDrop().get().asItem()));
            }
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return PoptartCatalog.blocks().stream()
                .filter(CatalogBlockDefinition::generatedLoot)
                .map(CatalogBlockDefinition::get)
                .map(Block.class::cast)
                .toList();
    }
}
