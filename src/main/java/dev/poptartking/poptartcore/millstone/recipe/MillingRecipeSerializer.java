package dev.poptartking.poptartcore.millstone.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class MillingRecipeSerializer implements RecipeSerializer<MillingRecipe> {
    public static final MapCodec<MillingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(MillingRecipe::ingredient),
                    Codec.INT.optionalFieldOf("input_count", 1).forGetter(MillingRecipe::inputCount),
                    ItemStack.CODEC.fieldOf("result").forGetter(MillingRecipe::result),
                    Codec.INT.optionalFieldOf("duration", 20).forGetter(MillingRecipe::duration),
                    Codec.FLOAT.optionalFieldOf("bonus_chance", 0.0F).forGetter(MillingRecipe::bonusChance),
                    Codec.INT.optionalFieldOf("bonus_count", 0).forGetter(MillingRecipe::bonusCount))
            .apply(inst, MillingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MillingRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(Ingredient.CODEC_NONEMPTY),
            MillingRecipe::ingredient,
            ByteBufCodecs.INT,
            MillingRecipe::inputCount,
            ItemStack.STREAM_CODEC,
            MillingRecipe::result,
            ByteBufCodecs.INT,
            MillingRecipe::duration,
            ByteBufCodecs.FLOAT,
            MillingRecipe::bonusChance,
            ByteBufCodecs.INT,
            MillingRecipe::bonusCount,
            MillingRecipe::new);

    public MapCodec<MillingRecipe> codec() {
        return CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, MillingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
