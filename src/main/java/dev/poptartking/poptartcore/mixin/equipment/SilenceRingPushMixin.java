package dev.poptartking.poptartcore.mixin.equipment;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.poptartking.poptartcore.equipment.SilenceRing;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class SilenceRingPushMixin {
    @ModifyReturnValue(method = "isPushable", at = @At("RETURN"))
    private boolean poptartcore$silenceStopsPushing(boolean original) {
        return original && !((Object) this instanceof Player player && SilenceRing.isIntangible(player));
    }
}
