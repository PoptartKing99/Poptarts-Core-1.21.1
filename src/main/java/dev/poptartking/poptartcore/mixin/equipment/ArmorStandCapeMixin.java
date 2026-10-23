package dev.poptartking.poptartcore.mixin.equipment;

import dev.poptartking.poptartcore.equipment.ArmorStandCape;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStand.class)
public abstract class ArmorStandCapeMixin {
    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    private void poptartcore$capeInteract(
            Player player, Vec3 hit, InteractionHand hand, CallbackInfoReturnable<InteractionResult> callback) {
        var result = ArmorStandCape.interact((ArmorStand) (Object) this, player, hand);
        if (result != InteractionResult.PASS) callback.setReturnValue(result);
    }

    // All vanilla destruction paths converge here, unlike chunk unloading or world saving.
    @Inject(method = "kill", at = @At("HEAD"))
    private void poptartcore$dropCape(CallbackInfo callback) {
        ArmorStandCape.drop((ArmorStand) (Object) this);
    }
}
