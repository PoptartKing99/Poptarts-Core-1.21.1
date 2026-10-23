package dev.poptartking.poptartcore.mixin.integration.reliablebackpacks;

import com.simibubi.create.content.equipment.armor.BacktankItem;
import dev.poptartking.poptartcore.integration.reliablebackpacks.BackSlotBackpack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.evandev.reliable_backpacks.platform.NeoForgePlatformHelper", remap = false)
public abstract class BackSlotBackpackPlatformMixin {
    @Shadow
    public abstract boolean isBackpackEquipped(LivingEntity entity);

    @Inject(method = "getEquippedBackpack", at = @At("HEAD"), cancellable = true)
    private void poptartcore$backpack(LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = BackSlotBackpack.get(entity);
        if (!stack.isEmpty()) cir.setReturnValue(stack);
    }

    @Inject(method = "isBackpackVisible", at = @At("HEAD"), cancellable = true)
    private void poptartcore$visible(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (BacktankItem.getWornBy(entity) != null) cir.setReturnValue(false);
        else if (!BackSlotBackpack.get(entity).isEmpty()) cir.setReturnValue(true);
    }

    @Inject(method = "canEquipBackpack", at = @At("HEAD"), cancellable = true)
    private void poptartcore$canEquip(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (isBackpackEquipped(player)) cir.setReturnValue(false);
        else if (!BackSlotBackpack.isBackpack(player.getItemBySlot(EquipmentSlot.CHEST))
                && BackSlotBackpack.hasEmptyBackSlot(player)) cir.setReturnValue(true);
    }

    @Inject(method = "equipBackpack", at = @At("HEAD"), cancellable = true)
    private void poptartcore$equip(Player player, ItemStack stack, CallbackInfo ci) {
        if (BackSlotBackpack.hasEmptyBackSlot(player) && BackSlotBackpack.canEquipBack(player, stack)) {
            BackSlotBackpack.equip(player, stack);
            ci.cancel();
        }
    }

    @Inject(method = "unequipBackpack", at = @At("HEAD"), cancellable = true)
    private void poptartcore$unequip(Player player, CallbackInfo ci) {
        if (!BackSlotBackpack.get(player).isEmpty()) {
            BackSlotBackpack.equip(player, ItemStack.EMPTY);
            ci.cancel();
        }
    }
}
