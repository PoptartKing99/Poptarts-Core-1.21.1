package dev.poptartking.poptartcore.mixin.bee;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeehiveBlockEntity.class)
public class BeehiveSkylightMixin {
    @WrapOperation(
            method = "*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isNight()Z"),
            require = 0)
    private static boolean poptartcore$nightOnlyWithSky(Level level, Operation<Boolean> original) {
        return level.dimensionType().hasSkyLight() && original.call(level);
    }

    @WrapOperation(
            method = "*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isRaining()Z"),
            require = 0)
    private static boolean poptartcore$rainOnlyWithSky(Level level, Operation<Boolean> original) {
        return level.dimensionType().hasSkyLight() && original.call(level);
    }
}
