package dev.poptartking.poptartcore.mixin.navigation;

import dev.poptartking.poptartcore.navigation.client.hud.CompassHud;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class NavigationMouseMixin {
    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    private void poptartcore$hideWaypoint(long window, int button, int action, int modifiers, CallbackInfo ci) {
        if (button == 0 && action == 1 && CompassHud.tryHide()) ci.cancel();
    }
}
