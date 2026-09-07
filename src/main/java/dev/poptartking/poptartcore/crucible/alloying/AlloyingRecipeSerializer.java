package dev.poptartking.poptartcore.crucible.alloying;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

public class AlloyingRecipeSerializer implements RecipeSerializer<AlloyingRecipe> {

    private static final int MAX_CRUCIBLE_INGREDIENTS = 4;
    private static final int MAX_BLAST_FURNACE_INGREDIENTS = 7;

    private final MapCodec<AlloyingRecipe> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, AlloyingRecipe> streamCodec;

    public AlloyingRecipeSerializer(boolean blastFurnace) {
        int maxIngredients = blastFurnace ? MAX_BLAST_FURNACE_INGREDIENTS : MAX_CRUCIBLE_INGREDIENTS;

        codec = RecordCodecBuilder.<AlloyingRecipe>mapCodec(instance -> instance.group(
                                CrucibleIngredient.CODEC
                                        .listOf(1, maxIngredients)
                                        .fieldOf("ingredients")
                                        .forGetter(AlloyingRecipe::ingredients),
                                Codec.INT.fieldOf("duration").forGetter(AlloyingRecipe::duration),
                                FluidStack.CODEC
                                        .optionalFieldOf("result", FluidStack.EMPTY)
                                        .forGetter(AlloyingRecipe::result),
                                ItemStack.OPTIONAL_CODEC
                                        .optionalFieldOf("item_result", ItemStack.EMPTY)
                                        .forGetter(AlloyingRecipe::itemResult))
                        .apply(
                                instance,
                                (ingredients, duration, result, itemResult) ->
                                        new AlloyingRecipe(ingredients, duration, result, itemResult, blastFurnace)))
                .validate(AlloyingRecipeSerializer::validateResult);

        streamCodec = StreamCodec.composite(
                ByteBufCodecs.fromCodec(CrucibleIngredient.CODEC.listOf(1, maxIngredients)),
                AlloyingRecipe::ingredients,
                ByteBufCodecs.INT,
                AlloyingRecipe::duration,
                FluidStack.OPTIONAL_STREAM_CODEC,
                AlloyingRecipe::result,
                ItemStack.OPTIONAL_STREAM_CODEC,
                AlloyingRecipe::itemResult,
                (ingredients, duration, result, itemResult) ->
                        new AlloyingRecipe(ingredients, duration, result, itemResult, blastFurnace));
    }

    private static DataResult<AlloyingRecipe> validateResult(AlloyingRecipe recipe) {
        if (recipe.result().isEmpty() && recipe.itemResult().isEmpty()) {
            return DataResult.error(() -> "Alloying recipe must have a fluid or item result");
        }

        return DataResult.success(recipe);
    }

    @Override
    public MapCodec<AlloyingRecipe> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AlloyingRecipe> streamCodec() {
        return streamCodec;
    }
}
