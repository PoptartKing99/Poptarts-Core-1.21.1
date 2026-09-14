package dev.poptartking.poptartcore.catalog.datagen;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.catalog.CatalogBlockDefinition;
import dev.poptartking.poptartcore.catalog.PoptartCatalog;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class CatalogBlockStateProvider extends BlockStateProvider {
    public CatalogBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, PoptartCore.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (CatalogBlockDefinition<?> definition : PoptartCatalog.blocks()) {
            switch (definition.model()) {
                case SIMPLE -> simple(definition);
                case RANDOM_CUBE -> randomCube(definition);
                case RANDOM_BOTTOM_TOP -> randomBottomTop(definition);
                case SLAB -> slab(definition);
                case STAIRS -> stairs(definition);
                case WALL -> wall(definition);
                case EXTERNAL -> {
                    // The block supplies handwritten blockstate and item model files.
                }
            }
        }
    }

    private void simple(CatalogBlockDefinition<?> definition) {
        if (definition.texture() == null) {
            simpleBlockWithItem(definition.get(), cubeAll(definition.get()));
            return;
        }
        ModelFile model = models().cubeAll(blockModelPath(definition), definition.texture());
        simpleBlockWithItem(definition.get(), model);
    }

    private void randomCube(CatalogBlockDefinition<?> definition) {
        ConfiguredModel[] variants = new ConfiguredModel[definition.variants()];
        for (int index = 0; index < variants.length; index++) {
            String suffix = index == 0 ? "" : "_" + index;
            ResourceLocation texture = suffix(definition.texture(), "_" + (index + 1));
            variants[index] = new ConfiguredModel(models().cubeAll(blockModelPath(definition) + suffix, texture));
        }
        getVariantBuilder(definition.get()).partialState().setModels(variants);
        simpleBlockItem(definition.get(), variants[0].model);
    }

    private void randomBottomTop(CatalogBlockDefinition<?> definition) {
        ConfiguredModel[] variants = new ConfiguredModel[definition.variants()];
        for (int index = 0; index < variants.length; index++) {
            String suffix = index == 0 ? "" : "_" + index;
            variants[index] = new ConfiguredModel(models().cubeBottomTop(
                            blockModelPath(definition) + suffix,
                            suffix(definition.sideTexture(), "_" + (index + 1)),
                            definition.bottomTexture(),
                            suffix(definition.topTexture(), "_" + (index + 1))));
        }
        getVariantBuilder(definition.get()).partialState().setModels(variants);
        simpleBlockItem(definition.get(), variants[0].model);
    }

    private void slab(CatalogBlockDefinition<?> definition) {
        SlabBlock block = (SlabBlock) definition.get();
        ModelFile bottom = models().slab(
                        blockModelPath(definition), definition.texture(), definition.texture(), definition.texture());
        ModelFile top = models().slabTop(
                        blockModelPath(definition) + "_top",
                        definition.texture(),
                        definition.texture(),
                        definition.texture());
        ModelFile doubleSlab = models().cubeAll(blockModelPath(definition) + "_double", definition.texture());
        slabBlock(block, bottom, top, doubleSlab);
        simpleBlockItem(block, bottom);
    }

    private void stairs(CatalogBlockDefinition<?> definition) {
        StairBlock block = (StairBlock) definition.get();
        stairsBlock(block, withoutSuffix(blockModelPath(definition), "_stairs"), definition.texture());
        itemModels().withExistingParent(definition.id(), modLoc("block/" + definition.modelPath()));
    }

    private void wall(CatalogBlockDefinition<?> definition) {
        WallBlock block = (WallBlock) definition.get();
        wallBlock(block, withoutSuffix(blockModelPath(definition), "_wall"), definition.texture());
        ModelFile inventory = models().wallInventory(blockModelPath(definition) + "_inventory", definition.texture());
        itemModels().withExistingParent(definition.id(), inventory.getLocation());
    }

    private static ResourceLocation suffix(ResourceLocation location, String suffix) {
        return ResourceLocation.fromNamespaceAndPath(location.getNamespace(), location.getPath() + suffix);
    }

    private static String blockModelPath(CatalogBlockDefinition<?> definition) {
        return "block/" + definition.modelPath();
    }

    private static String withoutSuffix(String value, String suffix) {
        if (!value.endsWith(suffix)) {
            throw new IllegalArgumentException("Catalog model path '" + value + "' must end with '" + suffix + "'");
        }
        return value.substring(0, value.length() - suffix.length());
    }
}
