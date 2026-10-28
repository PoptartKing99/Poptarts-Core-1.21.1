package dev.poptartking.poptartcore.catalog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AccessoryType;
import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import dev.poptartking.poptartcore.registry.PoptartEquipmentBlocks;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.TreeMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CatalogBlockItemTagGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void generatedBlockItemTagsPreserveBackSlotCompatibility(GameTestHelper helper) {
        var expected = PoptartCatalog.blocks().stream()
                .filter(definition -> definition.itemTags().contains(PoptartCoreTags.BACKPACKS))
                .map(definition -> definition.item().get())
                .collect(java.util.stream.Collectors.toSet());
        var actual = BuiltInRegistries.ITEM.getTag(PoptartCoreTags.BACKPACKS).orElseThrow().stream()
                .map(holder -> holder.value())
                .collect(java.util.stream.Collectors.toSet());
        helper.assertTrue(
                !expected.isEmpty() && actual.equals(expected),
                "Generated item tag membership must match the declared block items exactly");
        for (var id : java.util.List.of(
                "poptartcore:backpack",
                "poptartcore:rucksack",
                "create:copper_backtank",
                "create:netherite_backtank",
                "reliable_backpacks:backpack")) {
            var item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id));
            if (item.isPresent())
                helper.assertTrue(
                        AccessoryType.BACK.accepts(item.get().getDefaultInstance()),
                        "Existing back-slot compatibility must remain intact: " + id);
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void blockAndItemTagsStayIndependentAndFreezeTogether(GameTestHelper helper) {
        var definition = new CatalogBlockDefinition<>(
                "tag_contract_test", PoptartEquipmentBlocks.BACKPACK.block(), PoptartEquipmentBlocks.BACKPACK.item());
        definition.tags(BlockTags.MINEABLE_WITH_AXE);
        definition.itemTags(PoptartCoreTags.BACKPACKS, PoptartCoreTags.BACKPACKS);
        var snapshot = definition.itemTags();
        definition.itemTags(ItemTags.SLABS);
        helper.assertTrue(
                definition.tags().equals(java.util.Set.of(BlockTags.MINEABLE_WITH_AXE))
                        && snapshot.equals(java.util.Set.of(PoptartCoreTags.BACKPACKS))
                        && definition.itemTags().equals(java.util.Set.of(PoptartCoreTags.BACKPACKS, ItemTags.SLABS)),
                "Block and item tags are independent, duplicate-free, and exposed as snapshots");
        boolean immutable = false;
        try {
            snapshot.add(ItemTags.WALLS);
        } catch (UnsupportedOperationException expected) {
            immutable = true;
        }
        helper.assertTrue(immutable, "Callers cannot mutate the catalog through a returned tag set");
        var before = definition.itemTags();
        boolean invalidRejected = false;
        try {
            definition.itemTags(ItemTags.STAIRS, null);
        } catch (NullPointerException expected) {
            invalidRejected = true;
        }
        helper.assertTrue(
                invalidRejected && definition.itemTags().equals(before),
                "Invalid tag batches fail without partially adding their valid entries");
        definition.freeze();
        boolean lateEditRejected = false;
        try {
            definition.itemTags(ItemTags.WALLS);
        } catch (IllegalStateException expected) {
            lateEditRejected = true;
        }
        helper.assertTrue(
                lateEditRejected && definition.itemTags().equals(before),
                "Item tag metadata freezes with the rest of the block definition");
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void exportLoadedBlockItemTagMemberships(GameTestHelper helper) throws Exception {
        var itemIds = new HashSet<ResourceLocation>();
        PoptartCatalog.blocks()
                .forEach(definition -> itemIds.add(
                        BuiltInRegistries.ITEM.getKey(definition.item().get())));
        var memberships = new TreeMap<String, java.util.List<String>>();
        BuiltInRegistries.ITEM.getTags().forEach(pair -> {
            var members = pair.getSecond().stream()
                    .map(holder -> BuiltInRegistries.ITEM.getKey(holder.value()))
                    .filter(itemIds::contains)
                    .map(ResourceLocation::toString)
                    .sorted()
                    .toList();
            if (!members.isEmpty()) memberships.put(pair.getFirst().location().toString(), members);
        });
        var report = new JsonObject();
        var items = new JsonArray();
        itemIds.stream().sorted().forEach(id -> items.add(id.toString()));
        report.add("items", items);
        var tags = new JsonObject();
        memberships.forEach((tag, members) -> {
            var array = new JsonArray();
            members.forEach(array::add);
            tags.add(tag, array);
        });
        report.add("tags", tags);
        Files.writeString(Path.of("catalog-block-item-tags.json"), report.toString());
        helper.succeed();
    }
}
