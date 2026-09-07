package dev.poptartking.poptartcore.crucible.alloying;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

/** One alloying ingredient, represented by either an item ingredient or a fluid stack. */
public record CrucibleIngredient(Ingredient item, FluidStack fluid) {

    public static final Codec<CrucibleIngredient> CODEC = Codec.either(Ingredient.CODEC, FluidStack.CODEC)
            .xmap(
                    either -> new CrucibleIngredient(either.left(), either.right()),
                    ingredient -> ingredient.item().isEmpty()
                            ? Either.right(ingredient.fluid())
                            : Either.left(ingredient.item()));

    private CrucibleIngredient(Optional<Ingredient> item, Optional<FluidStack> fluid) {
        this(item.orElse(Ingredient.EMPTY), fluid.orElse(FluidStack.EMPTY));
    }
}
