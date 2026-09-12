package dev.poptartking.poptartcore.mixin.bee;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public class BeeFlipMixin {
    @ModifyReturnValue(method = "getFlipDegrees", at = @At("RETURN"))
    private float poptartcore$beesLandOnTheirBacks(float original, LivingEntity entity) {
        return entity instanceof Bee ? 180.0F : original;
    }
}
