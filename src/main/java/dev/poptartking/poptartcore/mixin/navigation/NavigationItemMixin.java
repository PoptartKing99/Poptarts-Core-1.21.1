package dev.poptartking.poptartcore.mixin.navigation;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Item.class})
public abstract class NavigationItemMixin {
    @Unique
    @Inject(
            method = {"use"},
            at = {@At("HEAD")},
            cancellable = true)
    private void poptartcore$openSelector(
            Level level,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!player.isSpectator() && player.getItemInHand(hand).is(Items.COMPASS)) {
            cir.setReturnValue(ItemUtils.startUsingInstantly(level, player, hand));
        }
    }

    @Inject(
            method = {"getUseDuration"},
            at = {@At("HEAD")},
            cancellable = true)
    private void poptartcore$selectorDuration(
            ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (stack.is(Items.COMPASS)) {
            cir.setReturnValue(72000);
        }
    }
}
