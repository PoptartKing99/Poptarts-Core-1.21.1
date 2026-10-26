package dev.poptartking.poptartcore.mixin.timekeeping;

import dev.poptartking.poptartcore.equipment.UtilityWield;
import dev.poptartking.poptartcore.timekeeping.Stopwatch;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ClockUseMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void poptartcore$toggleStopwatch(
            Level level,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> ci) {
        var stack = player.getItemInHand(hand);
        if (stack.is(Items.CLOCK) && !player.isSpectator()) {
            if (player instanceof ServerPlayer server) {
                Stopwatch.toggle(stack, server);
                if (UtilityWield.hand(player) == stack) UtilityWield.commit(player);
            }
            ci.setReturnValue(InteractionResultHolder.sidedSuccess(stack, level.isClientSide));
        }
    }
}
