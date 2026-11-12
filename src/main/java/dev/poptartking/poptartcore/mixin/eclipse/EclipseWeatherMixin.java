package dev.poptartking.poptartcore.mixin.eclipse;

import dev.poptartking.poptartcore.eclipse.EclipseEvents;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class EclipseWeatherMixin {
    @Inject(method = "advanceWeatherCycle", at = @At("HEAD"), cancellable = true)
    private void poptartcore$holdClearWeather(CallbackInfo ci) {
        if (EclipseEvents.keepWeatherClear((ServerLevel) (Object) this)) ci.cancel();
    }
}
