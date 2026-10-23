package dev.poptartking.poptartcore.mixin.equipment;

import dev.poptartking.poptartcore.equipment.AccessorySlot;
import dev.poptartking.poptartcore.equipment.CreativeSlotOrder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$SlotWrapper")
public abstract class AccessoryCreativeScreenMixin {
    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    private void poptartcore$hideEquipment(CallbackInfoReturnable<Boolean> cir) {
        // Keep every menu entry and index intact for incoming slot synchronization.
        var target = ((CreativeSlotWrapperAccessor) this).poptartcore$target();
        if (target instanceof AccessorySlot || CreativeSlotOrder.isSatchelSlot(target)) cir.setReturnValue(false);
    }
}
