package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.crucible.alloying.AlloyingRecipe;
import dev.poptartking.poptartcore.crucible.alloying.AlloyingRecipeSerializer;
import dev.poptartking.poptartcore.crucible.casting.CastingRecipe;
import dev.poptartking.poptartcore.crucible.casting.CastingRecipeSerializer;
import dev.poptartking.poptartcore.crucible.melting.MeltingRecipe;
import dev.poptartking.poptartcore.crucible.melting.MeltingRecipeSerializer;
import dev.poptartking.poptartcore.quern.recipe.GrindingRecipe;
import dev.poptartking.poptartcore.quern.recipe.GrindingRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PoptartCoreRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, PoptartCore.MOD_ID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, PoptartCore.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MeltingRecipe>>
            CRUCIBLE_MELTING_SERIALIZER =
                    RECIPE_SERIALIZERS.register("crucible_melting", () -> new MeltingRecipeSerializer(false));

    public static final DeferredHolder<RecipeType<?>, RecipeType<MeltingRecipe>> CRUCIBLE_MELTING_TYPE =
            RECIPE_TYPES.register(
                    "crucible_melting",
                    () -> RecipeType.simple(
                            ResourceLocation.fromNamespaceAndPath(PoptartCore.MOD_ID, "crucible_melting")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AlloyingRecipe>>
            CRUCIBLE_ALLOYING_SERIALIZER =
                    RECIPE_SERIALIZERS.register("crucible_alloying", () -> new AlloyingRecipeSerializer(false));

    public static final DeferredHolder<RecipeType<?>, RecipeType<AlloyingRecipe>> CRUCIBLE_ALLOYING_TYPE =
            RECIPE_TYPES.register(
                    "crucible_alloying",
                    () -> RecipeType.simple(
                            ResourceLocation.fromNamespaceAndPath(PoptartCore.MOD_ID, "crucible_alloying")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MeltingRecipe>>
            BLAST_FURNACE_MELTING_SERIALIZER =
                    RECIPE_SERIALIZERS.register("blast_furnace_melting", () -> new MeltingRecipeSerializer(true));

    public static final DeferredHolder<RecipeType<?>, RecipeType<MeltingRecipe>> BLAST_FURNACE_MELTING_TYPE =
            RECIPE_TYPES.register(
                    "blast_furnace_melting", () -> RecipeType.simple(PoptartCore.location("blast_furnace_melting")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AlloyingRecipe>>
            BLAST_FURNACE_ALLOYING_SERIALIZER =
                    RECIPE_SERIALIZERS.register("blast_furnace_alloying", () -> new AlloyingRecipeSerializer(true));

    public static final DeferredHolder<RecipeType<?>, RecipeType<AlloyingRecipe>> BLAST_FURNACE_ALLOYING_TYPE =
            RECIPE_TYPES.register(
                    "blast_furnace_alloying", () -> RecipeType.simple(PoptartCore.location("blast_furnace_alloying")));

    public static final DeferredHolder<RecipeSerializer<?>, CastingRecipeSerializer> CRUCIBLE_CASTING_SERIALIZER =
            RECIPE_SERIALIZERS.register("crucible_casting", CastingRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CastingRecipe>> CRUCIBLE_CASTING_TYPE =
            RECIPE_TYPES.register(
                    "crucible_casting",
                    () -> RecipeType.simple(
                            ResourceLocation.fromNamespaceAndPath(PoptartCore.MOD_ID, "crucible_casting")));

    public static final DeferredHolder<RecipeSerializer<?>, GrindingRecipeSerializer> GRINDING_SERIALIZER =
            RECIPE_SERIALIZERS.register("grinding", GrindingRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<GrindingRecipe>> GRINDING_TYPE =
            RECIPE_TYPES.register("grinding", () -> RecipeType.simple(PoptartCore.location("grinding")));

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
        RECIPE_TYPES.register(eventBus);
    }
}
