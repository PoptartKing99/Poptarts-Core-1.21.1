package dev.poptartking.poptartcore.mixin.integration.reliablebackpacks;

import dev.poptartking.poptartcore.integration.reliablebackpacks.BackSlotBackpack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "com.evandev.reliable_backpacks.common.items.BackpackItem", remap = false)
public abstract class BackSlotBackpackItemMixin {
    public boolean canEquip(ItemStack stack, EquipmentSlot slot, LivingEntity entity) {
        return entity.getEquipmentSlotForItem(stack) == slot && BackSlotBackpack.canEquipChest(entity, stack);
    }
}
