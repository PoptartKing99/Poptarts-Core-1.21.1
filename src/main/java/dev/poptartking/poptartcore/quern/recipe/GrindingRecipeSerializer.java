package dev.poptartking.poptartcore.quern.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class GrindingRecipeSerializer implements RecipeSerializer<GrindingRecipe> {
    private static final MapCodec<GrindingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(GrindingRecipe::ingredient),
                    ItemStack.CODEC.fieldOf("result").forGetter(GrindingRecipe::result),
                    com.mojang.serialization.Codec.intRange(1, Integer.MAX_VALUE)
                            .optionalFieldOf("cranks", GrindingRecipe.DEFAULT_CRANKS)
                            .forGetter(GrindingRecipe::cranks),
                    com.mojang.serialization.Codec.intRange(0, 0xFFFFFF)
                            .optionalFieldOf("powder_color", GrindingRecipe.DEFAULT_POWDER_COLOR)
                            .forGetter(GrindingRecipe::powderColor))
            .apply(instance, GrindingRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(Ingredient.CODEC_NONEMPTY),
            GrindingRecipe::ingredient,
            ItemStack.STREAM_CODEC,
            GrindingRecipe::result,
            ByteBufCodecs.VAR_INT,
            GrindingRecipe::cranks,
            ByteBufCodecs.VAR_INT,
            GrindingRecipe::powderColor,
            GrindingRecipe::new);

    @Override
    public MapCodec<GrindingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
