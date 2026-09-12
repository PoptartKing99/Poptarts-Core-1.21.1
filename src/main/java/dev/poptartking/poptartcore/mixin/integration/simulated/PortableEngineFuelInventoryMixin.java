package dev.poptartking.poptartcore.mixin.integration.simulated;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineInventory;
import dev.simulated_team.simulated.multiloader.inventory.ItemInfoWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PortableEngineInventory.class)
public class PortableEngineFuelInventoryMixin {
    @ModifyReturnValue(method = "canInsertItem", at = @At("RETURN"))
    private boolean poptartcore$onlyAllowedFuel(boolean original, @Local(argsOnly = true) ItemInfoWrapper info) {
        return original && info.type().getDefaultInstance().is(PoptartCoreTags.PORTABLE_ENGINE_ALLOWED_FUEL);
    }
}
