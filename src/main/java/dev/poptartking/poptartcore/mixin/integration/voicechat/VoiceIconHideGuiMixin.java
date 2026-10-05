package dev.poptartking.poptartcore.mixin.integration.voicechat;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "de.maxhenkel.voicechat.voice.client.RenderEvents", remap = false)
public abstract class VoiceIconHideGuiMixin {
    @Inject(method = "onRenderHUD", at = @At("HEAD"), cancellable = true)
    private void poptartcore$hideHudIcons(GuiGraphics graphics, float partialTick, CallbackInfo callback) {
        if (Minecraft.getInstance().options.hideGui) {
            callback.cancel();
        }
    }

    @Inject(method = "onRenderName", at = @At("HEAD"), cancellable = true)
    private void poptartcore$hideNameIcons(Entity entity, Component name, PoseStack pose,
            MultiBufferSource buffer, int light, float partialTick, CallbackInfo callback) {
        if (Minecraft.getInstance().options.hideGui) {
            callback.cancel();
        }
    }

    @ModifyReturnValue(method = "shouldShowIcons", at = @At("RETURN"))
    private boolean poptartcore$respectHideGui(boolean original) {
        return original && !Minecraft.getInstance().options.hideGui;
    }
}
