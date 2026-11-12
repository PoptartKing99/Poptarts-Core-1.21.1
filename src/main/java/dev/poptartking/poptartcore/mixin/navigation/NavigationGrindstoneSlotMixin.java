package dev.poptartking.poptartcore.mixin.navigation;

import dev.poptartking.poptartcore.navigation.Waypoints;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net.minecraft.world.inventory.GrindstoneMenu$2", "net.minecraft.world.inventory.GrindstoneMenu$3"})
public abstract class NavigationGrindstoneSlotMixin {
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void poptartcore$acceptMarkedCompass(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (Waypoints.isMarkedCompass(stack)) cir.setReturnValue(true);
    }
}
