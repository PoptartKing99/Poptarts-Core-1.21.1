package dev.poptartking.poptartcore.mixin.equipment;

import dev.poptartking.poptartcore.equipment.EquipmentRing;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class RingDurabilityMixin {
    @Inject(method = "hurtArmor", at = @At("HEAD"))
    private void poptartcore$wearRing(DamageSource source, float damage, CallbackInfo ci) {
        EquipmentRing.hurt((Player) (Object) this, source, damage);
    }
}
