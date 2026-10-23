package dev.poptartking.poptartcore.mixin.equipment;

import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class UtilityTotemMixin {
    // Supply the fallback after vanilla searches both hands, before its shared recovery code.
    @ModifyVariable(method = "checkTotemDeathProtection", at = @At(value = "LOAD", ordinal = 0), ordinal = 0)
    private ItemStack poptartcore$utilityTotem(ItemStack heldTotem, DamageSource source) {
        if (heldTotem != null || !((Object) this instanceof Player player)) return heldTotem;
        var inventory = AccessoryInventory.get(player);
        var totem = inventory.getStackInSlot(AccessoryInventory.UTILITY_SLOT);
        if (!totem.is(Items.TOTEM_OF_UNDYING)
                || !CommonHooks.onLivingUseTotem(player, source, totem, InteractionHand.OFF_HAND)) return null;
        var used = totem.copy();
        inventory.extractItem(AccessoryInventory.UTILITY_SLOT, 1, false);
        return used;
    }
}
