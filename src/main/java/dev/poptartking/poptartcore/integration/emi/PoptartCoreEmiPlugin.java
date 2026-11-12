package dev.poptartking.poptartcore.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.neoforge.NeoForgeEmiStack;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.crucible.alloying.AlloyingRecipe;
import dev.poptartking.poptartcore.crucible.casting.CastingRecipe;
import dev.poptartking.poptartcore.crucible.melting.MeltingRecipe;
import dev.poptartking.poptartcore.millstone.recipe.MillingRecipe;
import dev.poptartking.poptartcore.quern.recipe.GrindingRecipe;
import dev.poptartking.poptartcore.registry.PoptartCoreMenus;
import dev.poptartking.poptartcore.registry.PoptartCoreRecipes;
import dev.poptartking.poptartcore.registry.PoptartWorkstationBlocks;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

/** Discovered by EMI only. Common mod startup must not reference this optional integration. */
@EmiEntrypoint
public final class PoptartCoreEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        EmiRecipeCategory melting = category(registry, "crucible_melting", PoptartWorkstationBlocks.CRUCIBLE.get());
        EmiRecipeCategory blastMelting =
                category(registry, "blast_furnace_melting", PoptartWorkstationBlocks.BLAST_FURNACE.get());
        EmiRecipeCategory alloying = category(registry, "crucible_alloying", PoptartWorkstationBlocks.CRUCIBLE.get());
        EmiRecipeCategory blastAlloying =
                category(registry, "blast_furnace_alloying", PoptartWorkstationBlocks.BLAST_FURNACE.get());
        EmiRecipeCategory casting = category(registry, "casting", PoptartWorkstationBlocks.CRUCIBLE.get());
        registry.addWorkstation(melting, EmiStack.of(PoptartWorkstationBlocks.BLAST_FURNACE.get()));
        registry.addWorkstation(alloying, EmiStack.of(PoptartWorkstationBlocks.BLAST_FURNACE.get()));
        registry.addWorkstation(casting, EmiStack.of(PoptartWorkstationBlocks.BLAST_FURNACE.get()));
        EmiRecipeCategory grinding = category(registry, "grinding", PoptartWorkstationBlocks.QUERN.get());
        EmiRecipeCategory milling = category(registry, "milling", PoptartWorkstationBlocks.MILLSTONE.get());

        var recipes = registry.getRecipeManager();
        addMelting(registry, melting, recipes.getAllRecipesFor(PoptartCoreRecipes.CRUCIBLE_MELTING_TYPE.get()));
        addMelting(
                registry, blastMelting, recipes.getAllRecipesFor(PoptartCoreRecipes.BLAST_FURNACE_MELTING_TYPE.get()));
        for (var holder : recipes.getAllRecipesFor(PoptartCoreRecipes.CRUCIBLE_ALLOYING_TYPE.get())) {
            addAlloying(registry, alloying, holder);
        }
        for (var holder : recipes.getAllRecipesFor(PoptartCoreRecipes.BLAST_FURNACE_ALLOYING_TYPE.get())) {
            addAlloying(registry, blastAlloying, holder);
        }
        for (var holder : recipes.getAllRecipesFor(PoptartCoreRecipes.CRUCIBLE_CASTING_TYPE.get())) {
            addCasting(registry, casting, holder);
        }
        for (var holder : recipes.getAllRecipesFor(PoptartCoreRecipes.GRINDING_TYPE.get())) {
            addGrinding(registry, grinding, holder);
        }
        for (var holder : recipes.getAllRecipesFor(PoptartCoreRecipes.MILLING_TYPE.get())) {
            addMilling(registry, milling, holder);
        }
        registry.addWorkstation(
                VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(PoptartWorkstationBlocks.WORKBENCH.get()));
        registry.addRecipeHandler(PoptartCoreMenus.WORKBENCH.get(), new WorkbenchEmiRecipeHandler());
    }

    private static EmiRecipeCategory category(EmiRegistry registry, String name, ItemLike workstation) {
        EmiRecipeCategory category = new EmiRecipeCategory(PoptartCore.location(name), EmiStack.of(workstation));
        registry.addCategory(category);
        registry.addWorkstation(category, EmiStack.of(workstation));
        return category;
    }

    private static void addMelting(
            EmiRegistry registry, EmiRecipeCategory category, List<RecipeHolder<MeltingRecipe>> recipes) {
        Map<MeltingDisplayKey, List<RecipeHolder<MeltingRecipe>>> groups = new LinkedHashMap<>();
        for (var holder :
                recipes.stream().sorted(Comparator.comparing(RecipeHolder::id)).toList()) {
            var recipe = holder.value();
            var result = recipe.result();
            var key = new MeltingDisplayKey(
                    result.getFluid(), result.getComponentsPatch(), result.getAmount(), recipe.getCookingTime());
            groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(holder);
        }

        for (var group : groups.values()) {
            var first = group.getFirst();
            var alternatives = group.stream()
                    .map(holder -> EmiIngredient.of(holder.value().ingredient()))
                    .toList();
            registry.addRecipe(new ProcessingEmiRecipe(
                    category,
                    displayId(category, first.id()),
                    List.of(EmiIngredient.of(alternatives)),
                    List.of(NeoForgeEmiStack.of(first.value().result())),
                    List.of(),
                    true));
        }
    }

    private record MeltingDisplayKey(Fluid fluid, DataComponentPatch components, int amount, int duration) {}

    private static void addAlloying(
            EmiRegistry registry, EmiRecipeCategory category, RecipeHolder<AlloyingRecipe> holder) {
        AlloyingRecipe recipe = holder.value();
        List<EmiIngredient> inputs = recipe.ingredients().stream()
                .map(ingredient -> ingredient.fluid().isEmpty()
                        ? EmiIngredient.of(ingredient.item())
                        : NeoForgeEmiStack.of(ingredient.fluid()))
                .toList();
        List<EmiStack> outputs = new ArrayList<>();
        if (!recipe.result().isEmpty()) outputs.add(NeoForgeEmiStack.of(recipe.result()));
        if (!recipe.itemResult().isEmpty()) outputs.add(EmiStack.of(recipe.itemResult()));
        registry.addRecipe(
                new ProcessingEmiRecipe(category, displayId(category, holder.id()), inputs, outputs, List.of(), true));
    }

    private static void addCasting(
            EmiRegistry registry, EmiRecipeCategory category, RecipeHolder<CastingRecipe> holder) {
        CastingRecipe recipe = holder.value();
        // Each alternative gets its own remainder: reusable moulds lose durability; others are consumed.
        List<EmiIngredient> moulds = new ArrayList<>();
        for (ItemStack item : recipe.ingredient().getItems()) {
            EmiStack mould = EmiStack.of(item.copy());
            if (item.isDamageableItem() && item.getDamageValue() + 1 < item.getMaxDamage()) {
                ItemStack remainder = item.copy();
                remainder.setDamageValue(item.getDamageValue() + 1);
                mould.setRemainder(EmiStack.of(remainder));
            }
            moulds.add(mould);
        }
        registry.addRecipe(new ProcessingEmiRecipe(
                category,
                displayId(category, holder.id()),
                List.of(NeoForgeEmiStack.of(recipe.fluid()), EmiIngredient.of(moulds)),
                List.of(EmiStack.of(recipe.result())),
                List.of(),
                true));
    }

    private static void addGrinding(
            EmiRegistry registry, EmiRecipeCategory category, RecipeHolder<GrindingRecipe> holder) {
        GrindingRecipe recipe = holder.value();
        registry.addRecipe(new ProcessingEmiRecipe(
                category,
                displayId(category, holder.id()),
                List.of(EmiIngredient.of(recipe.ingredient())),
                List.of(EmiStack.of(recipe.result())),
                List.of(),
                true));
    }

    private static void addMilling(
            EmiRegistry registry, EmiRecipeCategory category, RecipeHolder<MillingRecipe> holder) {
        MillingRecipe recipe = holder.value();
        List<EmiStack> outputs = new ArrayList<>();
        outputs.add(EmiStack.of(recipe.result()));
        if (recipe.bonusCount() > 0 && recipe.bonusChance() > 0) {
            outputs.add(EmiStack.of(recipe.result().copyWithCount(recipe.bonusCount()))
                    .setChance(recipe.bonusChance()));
        }
        registry.addRecipe(new ProcessingEmiRecipe(
                category,
                displayId(category, holder.id()),
                List.of(EmiIngredient.of(recipe.ingredient(), recipe.inputCount())),
                outputs,
                List.of(),
                true));
    }

    private static ResourceLocation displayId(EmiRecipeCategory category, ResourceLocation recipeId) {
        // Keep viewer-only IDs separate from datapack recipe IDs.
        return PoptartCore.location(
                "/emi/" + category.getId().getPath() + "/" + recipeId.getNamespace() + "/" + recipeId.getPath());
    }
}
