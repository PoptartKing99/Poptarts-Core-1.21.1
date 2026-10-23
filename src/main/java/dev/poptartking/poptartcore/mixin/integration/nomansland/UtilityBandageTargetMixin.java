package dev.poptartking.poptartcore.mixin.integration.nomansland;

import dev.poptartking.poptartcore.equipment.UtilityWield;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.farcr.nomansland.common.item.BandageItem")
public abstract class UtilityBandageTargetMixin {
    @Inject(method = "getTargetEntity", at = @At("HEAD"), cancellable = true, remap = false)
    private void poptartcore$selfBandage(LivingEntity user, CallbackInfoReturnable<LivingEntity> cir) {
        if (user instanceof Player player && UtilityWield.isWielding(player)) cir.setReturnValue(user);
    }
}
