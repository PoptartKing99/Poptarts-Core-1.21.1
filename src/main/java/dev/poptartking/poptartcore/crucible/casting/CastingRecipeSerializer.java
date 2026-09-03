package dev.poptartking.poptartcore.crucible.casting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

public class CastingRecipeSerializer
        implements RecipeSerializer<CastingRecipe> {

    public static final MapCodec<CastingRecipe> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            FluidStack.CODEC
                                    .fieldOf("fluid")
                                    .forGetter(CastingRecipe::fluid),
                            Ingredient.CODEC_NONEMPTY
                                    .fieldOf("ingredient")
                                    .forGetter(CastingRecipe::ingredient),
                            ItemStack.CODEC
                                    .fieldOf("result")
                                    .forGetter(CastingRecipe::result)
                    ).apply(
                            instance,
                            CastingRecipe::new
                    )
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            CastingRecipe
            > STREAM_CODEC =
            StreamCodec.composite(
                    FluidStack.STREAM_CODEC,
                    CastingRecipe::fluid,

                    ByteBufCodecs.fromCodec(
                            Ingredient.CODEC_NONEMPTY
                    ),
                    CastingRecipe::ingredient,

                    ItemStack.STREAM_CODEC,
                    CastingRecipe::result,

                    CastingRecipe::new
            );

    @Override
    public MapCodec<CastingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<
            RegistryFriendlyByteBuf,
            CastingRecipe
            > streamCodec() {
        return STREAM_CODEC;
    }
}