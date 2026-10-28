package dev.poptartking.poptartcore.catalog;

import dev.poptartking.poptartcore.PoptartCore;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CatalogContentGameTests {
    private CatalogContentGameTests() {}

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void registeredContentPreservesOriginalContracts(GameTestHelper helper) throws Exception {
        var original = CatalogVerificationData.originalContent();
        var items = original.getAsJsonObject("items");
        var blocks = original.getAsJsonObject("blocks");
        for (var expected : items.entrySet())
            helper.assertTrue(
                    PoptartCatalog.items().stream()
                            .anyMatch(definition -> definition.id().equals(expected.getKey())),
                    "Missing original catalog item: " + expected.getKey());
        for (var expected : blocks.entrySet())
            helper.assertTrue(
                    PoptartCatalog.blocks().stream()
                            .anyMatch(definition -> definition.id().equals(expected.getKey())),
                    "Missing original catalog block: " + expected.getKey());
        for (var definition : PoptartCatalog.items()) {
            helper.assertTrue(
                    PoptartCore.location(definition.id()).equals(BuiltInRegistries.ITEM.getKey(definition.get()))
                            && !definition.displayName().isBlank(),
                    "Invalid catalog item: " + definition.id());
            if (!items.has(definition.id())) continue;
            var expected = items.getAsJsonObject(definition.id());
            helper.assertTrue(
                    expected.get("name").getAsString().equals(definition.displayName())
                            && expected.get("model")
                                    .getAsString()
                                    .equals(definition.model().name()),
                    "Original item name or model changed: " + definition.id());
        }
        for (var definition : PoptartCatalog.blocks()) {
            helper.assertTrue(
                    PoptartCore.location(definition.id()).equals(BuiltInRegistries.BLOCK.getKey(definition.get()))
                            && PoptartCore.location(definition.id())
                                    .equals(BuiltInRegistries.ITEM.getKey(
                                            definition.item().get()))
                            && !definition.displayName().isBlank(),
                    "Invalid catalog block or item: " + definition.id());
            if (!blocks.has(definition.id())) continue;
            var expected = blocks.getAsJsonObject(definition.id());
            helper.assertTrue(
                    expected.get("name").getAsString().equals(definition.displayName())
                            && expected.get("model")
                                    .getAsString()
                                    .equals(definition.model().name()),
                    "Original block name or model changed: " + definition.id());
            if (expected.has("variants"))
                helper.assertTrue(
                        definition.variants() == expected.get("variants").getAsInt(),
                        "Original block model variant count changed: " + definition.id());
            if (expected.has("oreDrop"))
                helper.assertTrue(
                        definition.oreDrop() != null
                                && expected.get("oreDrop")
                                        .getAsString()
                                        .equals(BuiltInRegistries.ITEM
                                                .getKey(definition
                                                        .oreDrop()
                                                        .get()
                                                        .asItem())
                                                .toString()),
                        "Original ore drop changed: " + definition.id());
            if (expected.has("storage")) {
                var storage = expected.getAsJsonObject("storage");
                helper.assertTrue(
                        definition.storageRecipes() != null
                                && storage.get("ingredient")
                                        .getAsString()
                                        .equals(BuiltInRegistries.ITEM
                                                .getKey(definition
                                                        .storageRecipes()
                                                        .ingredient()
                                                        .get()
                                                        .asItem())
                                                .toString())
                                && storage.get("packing")
                                        .getAsString()
                                        .equals(definition.storageRecipes().packingRecipeId())
                                && storage.get("unpacking")
                                        .getAsString()
                                        .equals(definition.storageRecipes().unpackingRecipeId()),
                        "Original storage recipe changed: " + definition.id());
            }
        }
        for (var entry : original.getAsJsonObject("blockTags").entrySet()) {
            var tag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(entry.getKey()));
            for (var id : entry.getValue().getAsJsonArray())
                helper.assertTrue(
                        BuiltInRegistries.BLOCK
                                .get(ResourceLocation.parse(id.getAsString()))
                                .defaultBlockState()
                                .is(tag),
                        id.getAsString() + " is missing loaded block tag " + entry.getKey());
        }
        for (var entry : original.getAsJsonObject("itemTags").entrySet()) {
            var tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(entry.getKey()));
            for (var id : entry.getValue().getAsJsonArray())
                helper.assertTrue(
                        BuiltInRegistries.ITEM
                                .get(ResourceLocation.parse(id.getAsString()))
                                .getDefaultInstance()
                                .is(tag),
                        id.getAsString() + " is missing loaded item tag " + entry.getKey());
        }
        for (var entry : original.getAsJsonObject("fuels").entrySet())
            helper.assertTrue(
                    BuiltInRegistries.ITEM
                                    .get(ResourceLocation.parse(entry.getKey()))
                                    .getDefaultInstance()
                                    .getBurnTime(RecipeType.SMELTING)
                            == entry.getValue().getAsInt(),
                    "Original furnace burn time changed: " + entry.getKey());
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void storageRecipesCraftBothWays(GameTestHelper helper) {
        for (var definition : PoptartCatalog.blocks()) {
            var storage = definition.storageRecipes();
            if (storage == null) continue;
            var ingredient = storage.ingredient().get().asItem();
            var packing = CraftingInput.of(
                    3,
                    3,
                    IntStream.range(0, 9)
                            .mapToObj(i -> new ItemStack(ingredient))
                            .toList());
            var unpacking = CraftingInput.of(1, 1, List.of(new ItemStack(definition.get())));
            assertCrafts(helper, storage.packingRecipeId(), packing, new ItemStack(definition.get()));
            assertCrafts(helper, storage.unpackingRecipeId(), unpacking, new ItemStack(ingredient, 9));
        }
        helper.succeed();
    }

    private static void assertCrafts(GameTestHelper helper, String id, CraftingInput input, ItemStack expected) {
        var holder = helper.getLevel().getRecipeManager().byKey(PoptartCore.location(id));
        helper.assertTrue(
                holder.isPresent() && holder.get().value() instanceof CraftingRecipe, "Missing crafting recipe: " + id);
        var recipe = (CraftingRecipe) holder.orElseThrow().value();
        helper.assertTrue(recipe.matches(input, helper.getLevel()), "Recipe rejects its intended ingredients: " + id);
        var actual = recipe.assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(
                ItemStack.isSameItemSameComponents(expected, actual) && expected.getCount() == actual.getCount(),
                "Recipe has the wrong output or count: " + id);
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void blockLootProducesExpectedDrops(GameTestHelper helper) {
        var tool = new ItemStack(Items.DIAMOND_PICKAXE);
        var silkTool = tool.copy();
        silkTool.enchant(
                helper.getLevel()
                        .registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.SILK_TOUCH),
                1);
        var pos = helper.absolutePos(new BlockPos(1, 1, 1));
        for (var definition : PoptartCatalog.blocks()) {
            var state = definition.get().defaultBlockState();
            if (definition.get().getLootTable().equals(BuiltInLootTables.EMPTY)) {
                helper.assertTrue(
                        Block.getDrops(state, helper.getLevel(), pos, null, null, tool)
                                .isEmpty(),
                        "No-loot block drops an item: " + definition.id());
                continue;
            }
            if (definition.lootMode() != CatalogBlockDefinition.LootMode.GENERATED) continue;
            var drops = Block.getDrops(state, helper.getLevel(), pos, null, null, tool);
            var expected = definition.oreDrop() == null
                    ? definition.item().get()
                    : definition.oreDrop().get().asItem();
            helper.assertTrue(
                    drops.size() == 1
                            && drops.getFirst().is(expected)
                            && drops.getFirst().getCount() == 1,
                    "Wrong normal block drop: " + definition.id());
            if (definition.oreDrop() != null) {
                var silk = Block.getDrops(state, helper.getLevel(), pos, null, null, silkTool);
                helper.assertTrue(
                        silk.size() == 1
                                && silk.getFirst().is(definition.item().get())
                                && silk.getFirst().getCount() == 1,
                        "Silk Touch does not preserve the ore block: " + definition.id());
            }
            if (definition.model() == CatalogBlockModel.SLAB) {
                var doubleSlab = Block.getDrops(
                        state.setValue(SlabBlock.TYPE, SlabType.DOUBLE), helper.getLevel(), pos, null, null, tool);
                helper.assertTrue(
                        doubleSlab.size() == 1
                                && doubleSlab.getFirst().is(expected)
                                && doubleSlab.getFirst().getCount() == 2,
                        "Double slab does not return two slabs: " + definition.id());
            }
        }
        helper.succeed();
    }
}
