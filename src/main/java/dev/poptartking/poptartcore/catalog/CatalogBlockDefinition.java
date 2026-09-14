package dev.poptartking.poptartcore.catalog;

import java.util.function.Supplier;
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
        this.storageRecipes = new StorageRecipes(ingredient, id, unpackingRecipeId);
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

    public record StorageRecipes(
            Supplier<? extends ItemLike> ingredient, String packingRecipeId, String unpackingRecipeId) {}
}
