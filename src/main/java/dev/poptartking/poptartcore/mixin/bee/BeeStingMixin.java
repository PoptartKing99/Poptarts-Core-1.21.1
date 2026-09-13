package dev.poptartking.poptartcore.mixin.bee;

import dev.poptartking.poptartcore.beekeeping.BeekeeperArmorItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Bee.class)
public abstract class BeeStingMixin {
    @Unique
    private static final EquipmentSlot[] POPTARTCORE$ARMOR_SLOTS = {
        EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
    };

    @Inject(method = "doHurtTarget", at = @At("HEAD"), cancellable = true)
    private void poptartcore$beekeeperBlocksSting(Entity target, CallbackInfoReturnable<Boolean> callback) {
        if (!(target instanceof LivingEntity living)) {
            return;
        }

        int pieces = BeekeeperArmorItem.pieceCount(living);
        if (pieces == 0 || living.getRandom().nextFloat() >= 0.25F * pieces) {
            return;
        }

        for (EquipmentSlot slot : POPTARTCORE$ARMOR_SLOTS) {
            ItemStack stack = living.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                stack.hurtAndBreak(1, living, slot);
            }
        }
        callback.setReturnValue(false);
    }
}
