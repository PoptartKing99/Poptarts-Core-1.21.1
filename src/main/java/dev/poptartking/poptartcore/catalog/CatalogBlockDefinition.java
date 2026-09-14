package dev.poptartking.poptartcore.catalog;

import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

public final class CatalogBlockDefinition<T extends Block> implements Supplier<T> {
    private final String id;
    private final DeferredBlock<T> block;
    private final DeferredItem<BlockItem> item;
    private String displayName;
    private StorageRecipes storageRecipes;
    private Supplier<? extends ItemLike> oreDrop;
    private CatalogBlockModel model = CatalogBlockModel.SIMPLE;
    private String modelFolder = "";
    private ResourceLocation texture;
    private ResourceLocation sideTexture;
    private ResourceLocation topTexture;
    private ResourceLocation bottomTexture;
    private int variants = 1;
    private boolean generatedLoot = true;
    private boolean frozen;

    CatalogBlockDefinition(String id, DeferredBlock<T> block, DeferredItem<BlockItem> item) {
        this.id = id;
        this.block = block;
        this.item = item;
        this.displayName = PoptartCatalog.createDisplayName(id);
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public DeferredBlock<T> block() {
        return block;
    }

    public DeferredItem<BlockItem> item() {
        return item;
    }

    public StorageRecipes storageRecipes() {
        return storageRecipes;
    }

    public Supplier<? extends ItemLike> oreDrop() {
        return oreDrop;
    }

    public CatalogBlockModel model() {
        return model;
    }

    public String modelPath() {
        return modelFolder.isEmpty() ? id : modelFolder + "/" + id;
    }

    public ResourceLocation texture() {
        return texture;
    }

    public ResourceLocation sideTexture() {
        return sideTexture;
    }

    public ResourceLocation topTexture() {
        return topTexture;
    }

    public ResourceLocation bottomTexture() {
        return bottomTexture;
    }

    public int variants() {
        return variants;
    }

    public boolean generatedLoot() {
        return generatedLoot;
    }

    @Override
    public T get() {
        return block.get();
    }

    public CatalogBlockDefinition<T> withName(String displayName) {
        requireMutable();
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("A Poptart Catalog block name cannot be blank");
        }
        this.displayName = displayName;
        return this;
    }

    public CatalogBlockDefinition<T> withStorageRecipes(
            Supplier<? extends ItemLike> ingredient, String unpackingRecipeId) {
        requireMutable();
        requireNoSpecialType();
        this.storageRecipes = new StorageRecipes(ingredient, id, unpackingRecipeId);
        return this;
    }

    public CatalogBlockDefinition<T> withOreDrop(Supplier<? extends ItemLike> drop) {
        requireMutable();
        requireNoSpecialType();
        this.oreDrop = drop;
        return this;
    }

    public CatalogBlockDefinition<T> simpleModel(ResourceLocation texture, String modelFolder) {
        return configureModel(CatalogBlockModel.SIMPLE, texture, modelFolder, 1);
    }

    public CatalogBlockDefinition<T> randomCubeModel(ResourceLocation texture, String modelFolder, int variants) {
        if (variants < 2) {
            throw new IllegalArgumentException("A random Catalog block model needs at least two variants");
        }
        return configureModel(CatalogBlockModel.RANDOM_CUBE, texture, modelFolder, variants);
    }

    public CatalogBlockDefinition<T> randomBottomTopModel(
            ResourceLocation side, ResourceLocation top, ResourceLocation bottom, String modelFolder, int variants) {
        if (variants < 2) {
            throw new IllegalArgumentException("A random Catalog block model needs at least two variants");
        }
        configureModel(CatalogBlockModel.RANDOM_BOTTOM_TOP, null, modelFolder, variants);
        this.sideTexture = side;
        this.topTexture = top;
        this.bottomTexture = bottom;
        return this;
    }

    public CatalogBlockDefinition<T> slabModel(ResourceLocation texture, String modelFolder) {
        return configureModel(CatalogBlockModel.SLAB, texture, modelFolder, 1);
    }

    public CatalogBlockDefinition<T> stairsModel(ResourceLocation texture, String modelFolder) {
        return configureModel(CatalogBlockModel.STAIRS, texture, modelFolder, 1);
    }

    public CatalogBlockDefinition<T> wallModel(ResourceLocation texture, String modelFolder) {
        return configureModel(CatalogBlockModel.WALL, texture, modelFolder, 1);
    }

    public CatalogBlockDefinition<T> externalModel() {
        return configureModel(CatalogBlockModel.EXTERNAL, null, "", 1);
    }

    public CatalogBlockDefinition<T> withoutGeneratedLoot() {
        requireMutable();
        this.generatedLoot = false;
        return this;
    }

    void freeze() {
        frozen = true;
    }

    private void requireMutable() {
        if (frozen) {
            throw new IllegalStateException("Poptart Catalog block '" + id + "' is already registered");
        }
    }

    private void requireNoSpecialType() {
        if (storageRecipes != null || oreDrop != null) {
            throw new IllegalStateException("Poptart Catalog block '" + id + "' already has a block type");
        }
    }

    private CatalogBlockDefinition<T> configureModel(
            CatalogBlockModel model, ResourceLocation texture, String modelFolder, int variants) {
        requireMutable();
        if (modelFolder.startsWith("/") || modelFolder.endsWith("/")) {
            throw new IllegalArgumentException("A Catalog model folder cannot start or end with '/'");
        }
        this.model = model;
        this.texture = texture;
        this.modelFolder = modelFolder;
        this.variants = variants;
        return this;
    }

    public record StorageRecipes(
            Supplier<? extends ItemLike> ingredient, String packingRecipeId, String unpackingRecipeId) {}
}
