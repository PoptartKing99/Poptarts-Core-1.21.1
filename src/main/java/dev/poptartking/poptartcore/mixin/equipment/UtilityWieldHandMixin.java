package dev.poptartking.poptartcore.mixin.equipment;

import dev.poptartking.poptartcore.equipment.UtilityWield;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class UtilityWieldHandMixin {
    @Inject(method = "getItemBySlot", at = @At("HEAD"), cancellable = true)
    private void poptartcore$utilityHand(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        if (slot == EquipmentSlot.MAINHAND) {
            var stack = UtilityWield.hand((Player) (Object) this);
            if (stack != null) cir.setReturnValue(stack);
        }
    }

    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true)
    private void poptartcore$storeUtilityHand(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        if (slot == EquipmentSlot.MAINHAND && UtilityWield.store((Player) (Object) this, stack)) ci.cancel();
    }
}
