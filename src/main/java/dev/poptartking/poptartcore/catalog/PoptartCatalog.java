package dev.poptartking.poptartcore.catalog;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.catalog.datagen.CatalogBlockLootProvider;
import dev.poptartking.poptartcore.catalog.datagen.CatalogBlockStateProvider;
import dev.poptartking.poptartcore.catalog.datagen.CatalogBlockTagProvider;
import dev.poptartking.poptartcore.catalog.datagen.CatalogItemModelProvider;
import dev.poptartking.poptartcore.catalog.datagen.CatalogItemTagProvider;
import dev.poptartking.poptartcore.catalog.datagen.CatalogLanguageProvider;
import dev.poptartking.poptartcore.catalog.datagen.CatalogRecipeProvider;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PoptartCatalog {
    public static final String GENERATED_RESOURCE_NAMESPACE = "poptart_catalog";

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PoptartCore.MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PoptartCore.MOD_ID);
    private static final Map<String, CatalogItemDefinition<?>> ITEM_DEFINITIONS = new LinkedHashMap<>();
    private static final Map<String, CatalogBlockDefinition<?>> BLOCK_DEFINITIONS = new LinkedHashMap<>();

    private PoptartCatalog() {}

    public static CatalogItemDefinition<Item> simpleItem(String id) {
        return item(id, () -> new Item(new Item.Properties()));
    }

    public static <T extends Item> CatalogItemDefinition<T> item(String id, Supplier<? extends T> factory) {
        if (ITEM_DEFINITIONS.containsKey(id) || BLOCK_DEFINITIONS.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate Poptart Catalog item: " + id);
        }

        DeferredItem<T> item = ITEMS.register(id, factory);
        CatalogItemDefinition<T> definition = new CatalogItemDefinition<>(id, item);
        ITEM_DEFINITIONS.put(id, definition);
        return definition;
    }

    public static <T extends Block> CatalogBlockDefinition<T> block(String id, Supplier<? extends T> factory) {
        return block(id, factory, BlockItem::new);
    }

    public static <T extends Block> CatalogBlockDefinition<T> block(
            String id,
            Supplier<? extends T> factory,
            BiFunction<? super T, Item.Properties, ? extends BlockItem> itemFactory) {
        if (BLOCK_DEFINITIONS.containsKey(id) || ITEM_DEFINITIONS.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate Poptart Catalog entry: " + id);
        }

        DeferredBlock<T> block = BLOCKS.register(id, factory);
        DeferredItem<BlockItem> item = ITEMS.register(id, () -> itemFactory.apply(block.get(), new Item.Properties()));
        CatalogBlockDefinition<T> definition = new CatalogBlockDefinition<>(id, block, item);
        BLOCK_DEFINITIONS.put(id, definition);
        return definition;
    }

    public static Collection<CatalogItemDefinition<?>> items() {
        return java.util.List.copyOf(ITEM_DEFINITIONS.values());
    }

    public static Collection<CatalogBlockDefinition<?>> blocks() {
        return List.copyOf(BLOCK_DEFINITIONS.values());
    }

    public static void addCreativeTabItems(CreativeModeTab.Output output, List<String> preferredOrder) {
        Map<String, Supplier<? extends ItemLike>> remaining = new LinkedHashMap<>();
        ITEM_DEFINITIONS.forEach((id, definition) -> remaining.put(id, definition));
        BLOCK_DEFINITIONS.forEach((id, definition) -> remaining.put(id, definition.item()));

        Set<String> orderedIds = new java.util.HashSet<>();
        for (String id : preferredOrder) {
            if (!orderedIds.add(id)) {
                throw new IllegalArgumentException("Duplicate Poptart Catalog creative-tab entry: " + id);
            }
            Supplier<? extends ItemLike> entry = remaining.remove(id);
            if (entry == null) {
                throw new IllegalArgumentException("Unknown Poptart Catalog creative-tab entry: " + id);
            }
            output.accept(entry.get());
        }
        remaining.values().forEach(entry -> output.accept(entry.get()));
    }

    public static void register(IEventBus eventBus) {
        ITEM_DEFINITIONS.values().forEach(CatalogItemDefinition::freeze);
        BLOCK_DEFINITIONS.values().forEach(CatalogBlockDefinition::freeze);
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        eventBus.addListener(PoptartCatalog::gatherData);
    }

    private static void gatherData(GatherDataEvent event) {
        if (event.includeClient()) {
            event.createProvider(output -> new CatalogItemModelProvider(output, event.getExistingFileHelper()));
            event.createProvider(output -> new CatalogBlockStateProvider(output, event.getExistingFileHelper()));
            event.createProvider(CatalogLanguageProvider::new);
        }
        if (event.includeServer()) {
            event.addProvider(new LootTableProvider(
                    event.getGenerator().getPackOutput(),
                    Set.of(),
                    List.of(new SubProviderEntry(CatalogBlockLootProvider::new, LootContextParamSets.BLOCK)),
                    event.getLookupProvider()));
            event.createProvider((output, lookup) -> new CatalogRecipeProvider(output, lookup));
            event.createProvider(
                    (output, lookup) -> new CatalogBlockTagProvider(output, lookup, event.getExistingFileHelper()));
            event.createProvider(
                    (output, lookup) -> new CatalogItemTagProvider(output, lookup, event.getExistingFileHelper()));
        }
    }

    static String createDisplayName(String id) {
        StringBuilder result = new StringBuilder();
        for (String word : id.split("_")) {
            if (word.isEmpty()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            if ((word.equals("of") || word.equals("and") || word.equals("with")) && !result.isEmpty()) {
                result.append(word);
            } else {
                result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return result.toString();
    }
}
