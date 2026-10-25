package dev.poptartking.poptartcore.mixin.equipment;

import dev.poptartking.poptartcore.equipment.client.UtilityClient;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class UtilityReleaseMixin {
    @Inject(method = "releaseUsingItem", at = @At("HEAD"), cancellable = true)
    private void poptartcore$holdUtility(Player player, CallbackInfo ci) {
        if (UtilityClient.isHolding()
                || dev.poptartking.poptartcore.backpack.client.BackpackAttachmentClient.isHolding()) ci.cancel();
    }
}
