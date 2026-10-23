package dev.poptartking.poptartcore.mixin.flesh;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.poptartking.poptartcore.flesh.FleshWaterTint;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({BiomeColors.class})
public class WaterColorMixin {
    @ModifyReturnValue(
            method = {"getAverageWaterColor"},
            at = {@At("RETURN")})
    private static int poptartcore$fleshTint(int original, BlockAndTintGetter getter, BlockPos pos) {
        return FleshWaterTint.tint(pos, original);
    }
}
