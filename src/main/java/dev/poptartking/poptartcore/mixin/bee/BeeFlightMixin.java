package dev.poptartking.poptartcore.mixin.bee;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bee.class)
public class BeeFlightMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void poptartcore$flyWithoutSinking(EntityType<? extends Bee> type, Level level, CallbackInfo info) {
        ((Bee) (Object) this).setNoGravity(true);
    }

    @WrapOperation(
            method = "*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isNight()Z"),
            require = 0)
    private boolean poptartcore$nightOnlyWithSky(Level level, Operation<Boolean> original) {
        return level.dimensionType().hasSkyLight() && original.call(level);
    }

    @WrapOperation(
            method = "*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isRaining()Z"),
            require = 0)
    private boolean poptartcore$rainOnlyWithSky(Level level, Operation<Boolean> original) {
        return level.dimensionType().hasSkyLight() && original.call(level);
    }
}
