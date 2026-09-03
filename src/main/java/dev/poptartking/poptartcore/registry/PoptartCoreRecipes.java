package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.crucible.melting.MeltingRecipe;
import dev.poptartking.poptartcore.crucible.melting.MeltingRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PoptartCoreRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(
                    Registries.RECIPE_SERIALIZER,
                    PoptartCore.MOD_ID
            );

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(
                    Registries.RECIPE_TYPE,
                    PoptartCore.MOD_ID
            );

    public static final DeferredHolder<
            RecipeSerializer<?>,
            RecipeSerializer<MeltingRecipe>
            > CRUCIBLE_MELTING_SERIALIZER =
            RECIPE_SERIALIZERS.register(
                    "crucible_melting",
                    () -> new MeltingRecipeSerializer(false)
            );

    public static final DeferredHolder<
            RecipeType<?>,
            RecipeType<MeltingRecipe>
            > CRUCIBLE_MELTING_TYPE =
            RECIPE_TYPES.register(
                    "crucible_melting",
                    () -> RecipeType.simple(
                            ResourceLocation.fromNamespaceAndPath(
                                    PoptartCore.MOD_ID,
                                    "crucible_melting"
                            )
                    )
            );

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
        RECIPE_TYPES.register(eventBus);
    }
}