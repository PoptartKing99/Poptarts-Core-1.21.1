package dev.poptartking.poptartcore.mixin.integration.create;

import dev.poptartking.poptartcore.integration.create.BackSlotBacktank;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorItem.class)
public abstract class BackSlotBacktankUseMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void poptartcore$preventSecondTank(
            Level level,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (!BackSlotBacktank.canEquipChest(player, stack)) cir.setReturnValue(InteractionResultHolder.fail(stack));
    }
}
