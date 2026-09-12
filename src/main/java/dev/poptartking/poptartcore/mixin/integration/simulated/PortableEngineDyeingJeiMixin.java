package dev.poptartking.poptartcore.mixin.integration.simulated;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "dev.simulated_team.simulated.neoforge.compat.jei.PortableEngineDyeingRecipeMaker")
public class PortableEngineDyeingJeiMixin {
    @ModifyExpressionValue(
            method = "createRecipes",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/tterrag/registrate/util/entry/BlockEntry;asStack()Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack poptartcore$showPoptartEngine(ItemStack original) {
        return new ItemStack(PoptartCoreBlocks.PORTABLE_ENGINE.get());
    }
}
