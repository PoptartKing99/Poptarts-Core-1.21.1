package dev.poptartking.poptartcore.catalog;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

public final class CatalogItemDefinition<T extends Item> implements Supplier<T> {
    private final String id;
    private final DeferredItem<T> item;
    private final Set<TagKey<Item>> tags = new LinkedHashSet<>();
    private String displayName;
    private CatalogItemModel model = CatalogItemModel.GENERATED;
    private int fuelBurnTime = -1;
    private boolean frozen;

    CatalogItemDefinition(String id, DeferredItem<T> item) {
        this.id = id;
        this.item = item;
        this.displayName = PoptartCatalog.createDisplayName(id);
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public CatalogItemModel model() {
        return model;
    }

    public Set<TagKey<Item>> tags() {
        return Set.copyOf(tags);
    }

    public int fuelBurnTime() {
        return fuelBurnTime;
    }

    public DeferredItem<T> item() {
        return item;
    }

    @Override
    public T get() {
        return item.get();
    }

    public CatalogItemDefinition<T> withName(String displayName) {
        requireMutable();
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("A Poptart Catalog item name cannot be blank");
        }
        this.displayName = displayName;
        return this;
    }

    public CatalogItemDefinition<T> model(CatalogItemModel model) {
        requireMutable();
        this.model = model;
        return this;
    }

    public CatalogItemDefinition<T> handheld() {
        return model(CatalogItemModel.HANDHELD);
    }

    public CatalogItemDefinition<T> withoutGeneratedModel() {
        return model(CatalogItemModel.CUSTOM);
    }

    @SafeVarargs
    public final CatalogItemDefinition<T> tags(TagKey<Item>... tags) {
        requireMutable();
        this.tags.addAll(java.util.List.of(tags));
        return this;
    }

    public CatalogItemDefinition<T> fuelBurnTime(int ticks) {
        requireMutable();
        if (ticks <= 0) {
            throw new IllegalArgumentException("A Poptart Catalog fuel burn time must be positive");
        }
        this.fuelBurnTime = ticks;
        return this;
    }

    void freeze() {
        frozen = true;
    }

    private void requireMutable() {
        if (frozen) {
            throw new IllegalStateException("Poptart Catalog item '" + id + "' is already registered");
        }
    }
}
