package dev.poptartking.poptartcore.mixin.integration.create;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.simibubi.create.content.equipment.armor.BacktankItem;
import dev.poptartking.poptartcore.integration.create.BackSlotBacktank;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BacktankItem.class)
public abstract class BackSlotBacktankMixin {
    public boolean canEquip(ItemStack stack, EquipmentSlot slot, LivingEntity entity) {
        return entity.getEquipmentSlotForItem(stack) == slot && BackSlotBacktank.canEquipChest(entity, stack);
    }

    @ModifyReturnValue(method = "getWornBy", at = @At("RETURN"))
    private static BacktankItem poptartcore$backSlot(BacktankItem original, Entity entity) {
        if (original != null) return original;
        var stack = BackSlotBacktank.get(entity);
        return stack.getItem() instanceof BacktankItem item ? item : null;
    }
}
