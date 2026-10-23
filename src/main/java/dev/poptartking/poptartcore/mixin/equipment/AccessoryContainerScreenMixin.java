package dev.poptartking.poptartcore.mixin.equipment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.poptartking.poptartcore.equipment.client.AccessoryPanel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AccessoryContainerScreenMixin {
    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    protected AbstractContainerMenu menu;

    @Inject(method = "renderBackground", at = @At("TAIL"))
    private void poptartcore$panel(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        AccessoryPanel.render(graphics, menu, leftPos, topPos);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void poptartcore$tooltip(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        AccessoryPanel.tooltip(graphics, menu, leftPos, topPos, mouseX, mouseY);
    }

    @WrapOperation(
            method = "mouseClicked",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;hasClickedOutside(DDIII)Z"))
    private boolean poptartcore$insidePanel(
            AbstractContainerScreen<?> screen,
            double mouseX,
            double mouseY,
            int left,
            int top,
            int button,
            Operation<Boolean> original) {
        return !AccessoryPanel.contains(menu, mouseX - leftPos, mouseY - topPos)
                && original.call(screen, mouseX, mouseY, left, top, button);
    }
}
