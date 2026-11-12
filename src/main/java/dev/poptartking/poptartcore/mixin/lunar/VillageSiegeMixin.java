package dev.poptartking.poptartcore.mixin.lunar;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.poptartking.poptartcore.lunar.BloodMoonData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.village.VillageSiege;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VillageSiege.class)
public abstract class VillageSiegeMixin {
    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getTimeOfDay(F)F"))
    private float poptartcore$bloodMoonSiegeTime(ServerLevel level, float partialTick, Operation<Float> original) {
        if (!BloodMoonData.active(level)) return original.call(level, partialTick);
        // Vanilla only rolls when the sky angle is exactly 0.5. Slow clocks can hold it there for several ticks.
        return BloodMoonData.get(level).claimSiegeRoll(level.getDayTime()) ? 0.5F : 0;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
    private int poptartcore$bloodMoonSiegeOdds(
            RandomSource random, int bound, Operation<Integer> original, ServerLevel level) {
        return original.call(random, BloodMoonData.active(level) ? 2 : bound);
    }
}
