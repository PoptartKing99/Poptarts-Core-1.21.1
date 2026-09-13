package dev.poptartking.poptartcore.mixin.bee;

import dev.poptartking.poptartcore.beekeeping.SmokedBees;
import net.minecraft.world.entity.animal.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bee.class)
public abstract class SmokedBeeMixin {
    @Inject(
            method = "customServerAiStep",
            at = {@At("HEAD"), @At("TAIL")})
    private void poptartcore$stayCalmWhileSmoked(CallbackInfo info) {
        Bee bee = (Bee) (Object) this;
        if (SmokedBees.isSmoked(bee)) {
            SmokedBees.keepCalm(bee);
        }
    }
}
