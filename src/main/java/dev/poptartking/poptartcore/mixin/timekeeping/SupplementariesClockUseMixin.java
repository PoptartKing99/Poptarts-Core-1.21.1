package dev.poptartking.poptartcore.mixin.timekeeping;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The continuous clock HUD replaces the vanilla clock item's temporary time overlay. */
@Pseudo
@Mixin(targets = "net.mehvahdjukaar.supplementaries.common.events.overrides.ClockItemBehavior")
public abstract class SupplementariesClockUseMixin {
    @Inject(method = "tryPerformingAction", at = @At("HEAD"), cancellable = true)
    private void poptartcore$keepStopwatchReadoutVisible(
            Level level,
            Player player,
            InteractionHand hand,
            ItemStack stack,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> ci) {
        ci.setReturnValue(InteractionResult.PASS);
    }
}
