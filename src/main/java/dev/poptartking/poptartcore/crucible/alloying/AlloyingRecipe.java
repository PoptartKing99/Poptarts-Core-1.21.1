package dev.poptartking.poptartcore.crucible.alloying;

import dev.poptartking.poptartcore.crucible.melting.MeltingRecipeInput;
import dev.poptartking.poptartcore.registry.PoptartCoreRecipes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public record AlloyingRecipe(
        List<CrucibleIngredient> ingredients,
        int duration,
        FluidStack result,
        ItemStack itemResult,
        boolean blastFurnace)
        implements Recipe<AlloyingRecipeInput> {

    @Override
    public boolean matches(AlloyingRecipeInput input, Level level) {
        if (blastFurnace && !input.blastFurnace()) {
            return false;
        }

        int batches = batchCount(input, level);
        if (batches < 1) {
            return false;
        }

        if (result.isEmpty()) {
            return true;
        }

        FluidStack tankFluid = input.fluid();
        if (tankFluid.isEmpty() || FluidStack.isSameFluid(tankFluid, result)) {
            return true;
        }

        return ingredients.stream()
                .map(CrucibleIngredient::fluid)
                .filter(ingredient -> !ingredient.isEmpty())
                .anyMatch(ingredient -> FluidStack.isSameFluid(ingredient, tankFluid));
    }

    @Override
    public ItemStack assemble(AlloyingRecipeInput input, HolderLookup.Provider registries) {
        return itemResult.copy();
    }

    public FluidStack assembleFluid(AlloyingRecipeInput input, Level level) {
        int batches = batchCount(input, level);
        return result.isEmpty() || batches <= 0
                ? FluidStack.EMPTY
                : result.copyWithAmount(result.getAmount() * batches);
    }

    public boolean usesAsItem(ItemStack stack) {
        return ingredients.stream()
                .map(CrucibleIngredient::item)
                .filter(ingredient -> !ingredient.isEmpty())
                .anyMatch(ingredient -> ingredient.test(stack));
    }

    public int batchCount(AlloyingRecipeInput input, Level level) {
        Map<Fluid, Integer> availableFluids = new HashMap<>();
        Map<Item, Integer> availableItems = new HashMap<>();

        if (!input.fluid().isEmpty()) {
            availableFluids.merge(input.fluid().getFluid(), input.fluid().getAmount(), Integer::sum);
        }

        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) {
                continue;
            }

            if (usesAsItem(stack)) {
                availableItems.merge(stack.getItem(), stack.getCount(), Integer::sum);
                continue;
            }

            FluidStack melted = tryMelt(stack, level, input.blastFurnace());
            if (melted.isEmpty()) {
                availableItems.merge(stack.getItem(), stack.getCount(), Integer::sum);
            } else {
                availableFluids.merge(melted.getFluid(), melted.getAmount() * stack.getCount(), Integer::sum);
            }
        }

        Map<Fluid, Integer> requiredFluids = new HashMap<>();
        Map<Ingredient, Integer> requiredItems = new HashMap<>();

        for (CrucibleIngredient ingredient : ingredients) {
            if (!ingredient.fluid().isEmpty()) {
                requiredFluids.merge(
                        ingredient.fluid().getFluid(), ingredient.fluid().getAmount(), Integer::sum);
            } else if (!ingredient.item().isEmpty()) {
                requiredItems.merge(ingredient.item(), 1, Integer::sum);
            }
        }

        int batches = Integer.MAX_VALUE;

        for (Map.Entry<Fluid, Integer> requirement : requiredFluids.entrySet()) {
            int available = availableFluids.getOrDefault(requirement.getKey(), 0);
            batches = Math.min(batches, available / requirement.getValue());
        }

        for (Map.Entry<Ingredient, Integer> requirement : requiredItems.entrySet()) {
            int available = availableItems.entrySet().stream()
                    .filter(entry -> requirement.getKey().test(entry.getKey().getDefaultInstance()))
                    .mapToInt(Map.Entry::getValue)
                    .sum();
            batches = Math.min(batches, available / requirement.getValue());
        }

        return batches == Integer.MAX_VALUE ? 0 : batches;
    }

    private FluidStack tryMelt(ItemStack stack, Level level, boolean blastFurnaceInput) {
        ItemStack singleItem = stack.copyWithCount(1);
        MeltingRecipeInput input = new MeltingRecipeInput(List.of(singleItem), blastFurnaceInput);

        return level.getRecipeManager()
                .getRecipeFor(PoptartCoreRecipes.CRUCIBLE_MELTING_TYPE.get(), input, level)
                .map(RecipeHolder::value)
                .map(recipe -> recipe.assembleFluid(input))
                .orElse(FluidStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height > 0;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return itemResult.copy();
    }

    public int getCookingTime() {
        return duration;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return blastFurnace
                ? PoptartCoreRecipes.BLAST_FURNACE_ALLOYING_SERIALIZER.get()
                : PoptartCoreRecipes.CRUCIBLE_ALLOYING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return blastFurnace
                ? PoptartCoreRecipes.BLAST_FURNACE_ALLOYING_TYPE.get()
                : PoptartCoreRecipes.CRUCIBLE_ALLOYING_TYPE.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }
}
