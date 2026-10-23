package dev.poptartking.poptartcore.mixin.integration.ragdoll;

import com.farcr.ragdoll.screen.CorpseMenu;
import com.farcr.ragdoll.screen.CorpseScreen;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.integration.ragdoll.CorpseInventoryLayout;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(CorpseScreen.class)
public abstract class CorpseInventoryScreenMixin extends AbstractContainerScreen<CorpseMenu> {
    @Shadow
    @Final
    @Mutable
    public ResourceLocation BACKGROUND;

    @Shadow
    private Button TAKE_BUTTON;

    protected CorpseInventoryScreenMixin(CorpseMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void poptartcore$texture(
            CorpseMenu menu,
            Inventory inventory,
            LivingEntity entity,
            boolean corpse,
            boolean offline,
            String name,
            CallbackInfo ci) {
        BACKGROUND = ResourceLocation.fromNamespaceAndPath(PoptartCore.MOD_ID, "textures/gui/corpse_inventory.png");
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void poptartcore$size(CallbackInfo ci) {
        imageWidth = CorpseInventoryLayout.WIDTH;
        imageHeight = CorpseInventoryLayout.HEIGHT;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void poptartcore$button(CallbackInfo ci) {
        topPos -= 7;
        TAKE_BUTTON.setPosition(leftPos + 160, topPos + 78);
    }

    @ModifyArgs(
            method = "renderBg",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V"))
    private void poptartcore$background(Args args) {
        args.set(1, leftPos);
        args.set(2, topPos);
        args.set(5, CorpseInventoryLayout.WIDTH);
        args.set(6, CorpseInventoryLayout.HEIGHT);
    }

    @ModifyArgs(
            method = "renderBg",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/farcr/ragdoll/screen/CorpseScreen;renderEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphics;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V"))
    private void poptartcore$preview(Args args) {
        args.set(1, leftPos + 41);
        args.set(2, topPos + 19);
        args.set(3, leftPos + 111);
        args.set(4, topPos + 89);
    }

    @ModifyArgs(
            method = "render",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V"))
    private void poptartcore$buttonSprite(Args args) {
        args.set(1, leftPos + 160);
        args.set(2, topPos + 78);
    }

    @ModifyArgs(
            method = "renderLabels",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphics;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)I"))
    private void poptartcore$title(Args args) {
        args.set(2, 6);
        args.set(3, -12);
    }

    @ModifyArgs(
            method = "renderLabels",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"))
    private void poptartcore$playerTitle(Args args) {
        args.set(2, 6);
        args.set(3, -12);
    }
}
