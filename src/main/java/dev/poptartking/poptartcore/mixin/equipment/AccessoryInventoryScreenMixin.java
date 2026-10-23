package dev.poptartking.poptartcore.mixin.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class AccessoryInventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> {
    protected AccessoryInventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @ModifyArg(
            method = "renderBg",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
            index = 0)
    private ResourceLocation poptartcore$background(ResourceLocation original) {
        return PoptartCore.location("textures/gui/equipment_inventory.png");
    }

    @Inject(method = "renderLabels", at = @At("HEAD"), cancellable = true)
    private void poptartcore$hideCraftingTitle(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void poptartcore$recipeButton(
            GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        for (var child : children()) {
            if (child instanceof ImageButton button) button.setPosition(leftPos + 152, topPos + 59);
        }
    }
}
