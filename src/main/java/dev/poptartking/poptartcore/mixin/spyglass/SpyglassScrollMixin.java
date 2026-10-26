package dev.poptartking.poptartcore.mixin.spyglass;

import dev.poptartking.poptartcore.spyglass.client.SpyglassZoom;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class SpyglassScrollMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void poptartcore$scrollSpyglass(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (SpyglassZoom.scroll(vertical)) ci.cancel();
    }
}
