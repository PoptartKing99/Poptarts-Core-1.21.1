package dev.poptartking.poptartcore.crucible.melting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

public class MeltingRecipeSerializer
        implements RecipeSerializer<MeltingRecipe> {

    private final MapCodec<MeltingRecipe> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, MeltingRecipe> streamCodec;

    public MeltingRecipeSerializer(boolean blastFurnace) {
        this.codec = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Ingredient.CODEC_NONEMPTY
                                .fieldOf("ingredient")
                                .forGetter(MeltingRecipe::ingredient),
                        Codec.INT
                                .fieldOf("duration")
                                .forGetter(MeltingRecipe::duration),
                        FluidStack.CODEC
                                .fieldOf("result")
                                .forGetter(MeltingRecipe::result)
                ).apply(
                        instance,
                        (ingredient, duration, result) ->
                                new MeltingRecipe(
                                        ingredient,
                                        duration,
                                        result,
                                        blastFurnace
                                )
                )
        );

        this.streamCodec = StreamCodec.composite(
                ByteBufCodecs.fromCodec(Ingredient.CODEC_NONEMPTY),
                MeltingRecipe::ingredient,

                ByteBufCodecs.INT,
                MeltingRecipe::duration,

                FluidStack.STREAM_CODEC,
                MeltingRecipe::result,

                (ingredient, duration, result) ->
                        new MeltingRecipe(
                                ingredient,
                                duration,
                                result,
                                blastFurnace
                        )
        );
    }

    @Override
    public MapCodec<MeltingRecipe> codec() {
        return codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MeltingRecipe> streamCodec() {
        return streamCodec;
    }
}