package dev.poptartking.poptartcore.mixin.item;

import dev.poptartking.poptartcore.hammer.HammerMining;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class ClientHammerFaceMixin {

    @Inject(method = "startDestroyBlock", at = @At("HEAD"))
    private void poptartcore$rememberHammerFace(
            BlockPos pos, Direction face, CallbackInfoReturnable<Boolean> callback) {
        if (Minecraft.getInstance().player != null) {
            HammerMining.beginMining(Minecraft.getInstance().player, face);
        }
    }
}
