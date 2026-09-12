package dev.poptartking.poptartcore.mixin.integration.simulated;

import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import dev.simulated_team.simulated.data.neoforge.PortableEngineDyeingRecipe;
import dev.simulated_team.simulated.index.SimBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PortableEngineDyeingRecipe.class)
public class PortableEngineDyeingMixin {
    @Inject(
            method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z",
            at = @At("RETURN"),
            cancellable = true)
    private void poptartcore$rejectOriginalRedEngine(
            CraftingInput input, Level level, CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValueZ()) {
            return;
        }
        for (int index = 0; index < input.size(); index++) {
            if (input.getItem(index).is(SimBlocks.RED_PORTABLE_ENGINE.asItem())) {
                callback.setReturnValue(false);
                return;
            }
        }
    }

    @Inject(method = "assemble", at = @At("RETURN"), cancellable = true)
    private void poptartcore$redDyeGivesPoptartEngine(
            CraftingInput input, HolderLookup.Provider registries, CallbackInfoReturnable<ItemStack> callback) {
        ItemStack result = callback.getReturnValue();
        if (!result.isEmpty() && result.is(SimBlocks.RED_PORTABLE_ENGINE.asItem())) {
            ItemStack replacement = new ItemStack(PoptartCoreBlocks.PORTABLE_ENGINE.get());
            if (!result.isComponentsPatchEmpty()) {
                replacement.applyComponents(result.getComponentsPatch());
            }
            callback.setReturnValue(replacement);
        }
    }
}
