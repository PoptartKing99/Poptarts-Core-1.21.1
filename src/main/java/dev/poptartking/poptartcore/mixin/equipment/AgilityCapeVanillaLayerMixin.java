package dev.poptartking.poptartcore.mixin.equipment;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.poptartking.poptartcore.equipment.AgilityCape;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class AgilityCapeVanillaLayerMixin {
    @Inject(
            method =
                    "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
            at = @At("HEAD"),
            cancellable = true)
    private void poptartcore$replaceAccountCape(
            PoseStack stack,
            MultiBufferSource buffers,
            int light,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float age,
            float headYaw,
            float headPitch,
            CallbackInfo ci) {
        if (AgilityCape.equipped(player)) ci.cancel();
    }
}
