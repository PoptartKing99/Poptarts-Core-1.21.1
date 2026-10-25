package dev.poptartking.poptartcore.mixin.equipment;

import dev.poptartking.poptartcore.backpack.BackpackAttachments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class BackpackWardingMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void poptartcore$effigy(LivingEntity target, CallbackInfo ci) {
        if (target instanceof Player player && BackpackAttachments.has(player, "nomansland:warding_effigy")) {
            var mob = (Mob) (Object) this;
            double range = mob.getAttributeValue(Attributes.FOLLOW_RANGE) * .5;
            if (mob.distanceToSqr(player) > range * range) ci.cancel();
        }
    }
}
