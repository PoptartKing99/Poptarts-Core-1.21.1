package dev.poptartking.poptartcore.mixin.item;

import dev.poptartking.poptartcore.client.ClientMiningCleanup;
import dev.poptartking.poptartcore.hammer.HammerMining;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class ClientHammerFaceMixin implements ClientMiningCleanup {
    @Unique
    private int poptartcore$breakerId = -1;

    @Unique
    private BlockPos poptartcore$hammerCenter;

    @Inject(method = "startDestroyBlock", at = @At("HEAD"))
    private void poptartcore$rememberHammerFace(
            BlockPos pos, Direction face, CallbackInfoReturnable<Boolean> callback) {
        Minecraft minecraft = Minecraft.getInstance();
        poptartcore$clearActiveMining();
        if (minecraft.player != null && minecraft.level != null) {
            HammerMining.beginMining(minecraft.player, face);
            poptartcore$breakerId = minecraft.player.getId();
            poptartcore$hammerCenter = pos.immutable();
        }
    }

    @Inject(method = "stopDestroyBlock", at = @At("HEAD"))
    private void poptartcore$stopHammerMining(CallbackInfo callback) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            HammerMining.endMining(minecraft.player);
        }
    }

    @Override
    public void poptartcore$clearActiveMining() {
        Minecraft minecraft = Minecraft.getInstance();
        if (poptartcore$hammerCenter != null && poptartcore$breakerId >= 0) {
            minecraft.levelRenderer.destroyBlockProgress(poptartcore$breakerId, poptartcore$hammerCenter, -1);
        }
        if (minecraft.player != null) {
            HammerMining.endMining(minecraft.player);
        }
        poptartcore$breakerId = -1;
        poptartcore$hammerCenter = null;
    }
}
