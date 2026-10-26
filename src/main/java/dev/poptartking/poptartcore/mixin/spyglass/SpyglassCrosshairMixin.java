package dev.poptartking.poptartcore.mixin.spyglass;

import dev.poptartking.poptartcore.spyglass.client.SpyglassZoom;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Gui.class})
public abstract class SpyglassCrosshairMixin {
    @Inject(
            method = {"renderCrosshair"},
            at = {@At("HEAD")},
            cancellable = true)
    private void poptartcore$hideWhileScoping(CallbackInfo ci) {
        if (SpyglassZoom.isZooming()) {
            ci.cancel();
        }
    }
}
