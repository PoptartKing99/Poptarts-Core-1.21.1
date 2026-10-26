package dev.poptartking.poptartcore.mixin.navigation;

import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Our HUD supplies coordinates and time; allow compass selection and clock stopwatch use. */
@Pseudo
@Mixin(targets = "com.teamabnormals.caverns_and_chasms.core.other.CCClientEvents")
public abstract class NavigationCavernsCompassMixin {
    @Inject(method = "onItemUse", at = @At("HEAD"), cancellable = true)
    private static void poptartcore$allowCompassNavigation(PlayerInteractEvent.RightClickItem event, CallbackInfo ci) {
        if (event.getItemStack().is(Items.COMPASS) || event.getItemStack().is(Items.CLOCK)) ci.cancel();
    }
}
