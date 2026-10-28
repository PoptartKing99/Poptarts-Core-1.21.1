package dev.poptartking.poptartcore.catalog;

import static dev.poptartking.poptartcore.catalog.ToolTestSupport.item;

import com.google.gson.JsonObject;
import dev.poptartking.poptartcore.PoptartCore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CatalogCraftingGameTests {
    private CatalogCraftingGameTests() {}

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void itemRecipesPreserveInputsPatternsAndOutputs(GameTestHelper helper) throws Exception {
        for (var value : CatalogVerificationData.itemBehavior().getAsJsonArray("recipes")) {
            var contract = value.getAsJsonObject();
            String id = contract.get("id").getAsString();
            var holder = helper.getLevel().getRecipeManager().byKey(PoptartCore.location(id));
            helper.assertTrue(
                    holder.isPresent() && holder.get().value() instanceof CraftingRecipe, "Missing item recipe: " + id);
            var recipe = (CraftingRecipe) holder.orElseThrow().value();
            var ingredients = grid(contract, false);
            var input = CraftingInput.of(3, 3, ingredients);
            helper.assertTrue(
                    recipe.matches(input, helper.getLevel()), "Recipe rejects its intended ingredients: " + id);
            var reordered = grid(contract, true);
            helper.assertTrue(
                    recipe.matches(CraftingInput.of(3, 3, reordered), helper.getLevel()),
                    "Recipe rejects a mirrored or reordered input: " + id);
            var result = contract.getAsJsonObject("result");
            var expected = new ItemStack(
                    item(result.get("id").getAsString()), result.get("count").getAsInt());
            var actual = recipe.assemble(input, helper.getLevel().registryAccess());
            helper.assertTrue(
                    ItemStack.isSameItemSameComponents(actual, expected) && actual.getCount() == expected.getCount(),
                    "Wrong recipe output, count, or components: " + id);
            for (int index = 0; index < ingredients.size(); index++) {
                if (ingredients.get(index).isEmpty()) continue;
                var missing = grid(contract, false);
                missing.set(index, ItemStack.EMPTY);
                helper.assertTrue(
                        !recipe.matches(CraftingInput.of(3, 3, missing), helper.getLevel()),
                        "Recipe accepts a missing ingredient at slot " + index + ": " + id);
                var wrong = grid(contract, false);
                wrong.set(index, new ItemStack(wrongIngredient(ingredients.get(index))));
                helper.assertTrue(
                        !recipe.matches(CraftingInput.of(3, 3, wrong), helper.getLevel()),
                        "Recipe accepts a wrong ingredient at slot " + index + ": " + id);
            }
            var extra = grid(contract, false);
            extra.set(firstEmpty(extra), new ItemStack(Items.DIRT));
            helper.assertTrue(
                    !recipe.matches(CraftingInput.of(3, 3, extra), helper.getLevel()),
                    "Recipe accepts an extra ingredient: " + id);
            if (contract.has("pattern")) {
                var wrongShape = grid(contract, false);
                int first = firstOccupied(wrongShape);
                int different = firstDifferent(wrongShape, first);
                Collections.swap(wrongShape, first, different);
                helper.assertTrue(
                        !recipe.matches(CraftingInput.of(3, 3, wrongShape), helper.getLevel()),
                        "Shaped recipe accepts the wrong arrangement: " + id);
            }
        }
        helper.succeed();
    }

    private static ArrayList<ItemStack> grid(JsonObject contract, boolean reorder) {
        var result = new ArrayList<ItemStack>(Collections.nCopies(9, ItemStack.EMPTY));
        if (contract.has("pattern")) {
            var pattern = contract.getAsJsonArray("pattern");
            for (int y = 0; y < pattern.size(); y++) {
                String row = pattern.get(y).getAsString();
                for (int x = 0; x < row.length(); x++) {
                    char symbol = row.charAt(reorder ? row.length() - 1 - x : x);
                    if (symbol != ' ')
                        result.set(
                                y * 3 + x,
                                ingredient(contract.getAsJsonObject("key").getAsJsonObject(String.valueOf(symbol))));
                }
            }
        } else {
            var ingredients = contract.getAsJsonArray("ingredients");
            for (int i = 0; i < ingredients.size(); i++)
                result.set(reorder ? 8 - i : i, ingredient(ingredients.get(i).getAsJsonObject()));
        }
        return result;
    }

    private static ItemStack ingredient(JsonObject ingredient) {
        return new ItemStack(item(ingredient.get("item").getAsString()));
    }

    private static Item wrongIngredient(ItemStack stack) {
        if (stack.is(item("bronze_ingot"))) return item("steel_ingot");
        if (stack.is(item("steel_ingot"))) return item("bronze_ingot");
        if (stack.is(Items.BONE)) return Items.FLINT;
        if (stack.is(Items.FLINT)) return Items.BONE;
        return Items.DIRT;
    }

    private static int firstEmpty(List<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) if (items.get(i).isEmpty()) return i;
        throw new IllegalArgumentException("Recipe needs a free slot for the extra-ingredient test");
    }

    private static int firstOccupied(List<ItemStack> items) {
        for (int i = 0; i < items.size(); i++) if (!items.get(i).isEmpty()) return i;
        throw new IllegalArgumentException("Empty recipe contract");
    }

    private static int firstDifferent(List<ItemStack> items, int first) {
        for (int i = 0; i < items.size(); i++)
            if (!items.get(i).isEmpty() && !items.get(i).is(items.get(first).getItem())) return i;
        throw new IllegalArgumentException("Recipe needs two distinct ingredients for the arrangement test");
    }
}
