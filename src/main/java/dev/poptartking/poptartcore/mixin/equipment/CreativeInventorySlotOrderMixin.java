package dev.poptartking.poptartcore.mixin.equipment;

import dev.poptartking.poptartcore.equipment.CreativeSlotOrder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventorySlotOrderMixin {
    @Inject(method = "selectTab", at = @At("TAIL"))
    private void poptartcore$preserveInventorySlotOrder(CreativeModeTab tab, CallbackInfo ci) {
        if (tab.getType() != CreativeModeTab.Type.INVENTORY) return;
        var screen = (CreativeModeInventoryScreen) (Object) this;
        var inventory = Minecraft.getInstance().player.inventoryMenu;
        // Satchels skips its wrappers during selectTab. Restore their entries, not their visibility.
        CreativeSlotOrder.restoreMissing(
                screen.getMenu().slots,
                inventory.slots,
                slot -> slot instanceof CreativeSlotWrapperAccessor wrapper
                        ? wrapper.poptartcore$target()
                        : slot instanceof CreativeSlotOrder.HiddenSlot hidden ? hidden.target : null,
                CreativeSlotOrder.HiddenSlot::new);
    }
}
