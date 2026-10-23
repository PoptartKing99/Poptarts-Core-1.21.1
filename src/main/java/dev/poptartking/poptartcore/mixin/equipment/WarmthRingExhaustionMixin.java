package dev.poptartking.poptartcore.mixin.equipment;

import dev.poptartking.poptartcore.equipment.EquipmentRing;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class WarmthRingExhaustionMixin {
    @ModifyVariable(method = "causeFoodExhaustion(F)V", at = @At("HEAD"), argsOnly = true)
    private float poptartcore$reduceExhaustion(float exhaustion) {
        return EquipmentRing.has((Player) (Object) this, EquipmentRing.Kind.WARMTH) ? exhaustion * .85F : exhaustion;
    }
}
