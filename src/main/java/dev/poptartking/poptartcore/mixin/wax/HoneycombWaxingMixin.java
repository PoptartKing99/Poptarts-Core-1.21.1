package dev.poptartking.poptartcore.mixin.wax;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoneycombItem.class)
public class HoneycombWaxingMixin {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void poptartcore$disableHoneycombWaxing(
            UseOnContext context, CallbackInfoReturnable<InteractionResult> callback) {
        callback.setReturnValue(InteractionResult.PASS);
    }
}
