package dev.poptartking.poptartcore.mixin.integration.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import dev.poptartking.poptartcore.integration.create.BeltConnectorCost;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeltConnectorItem.class)
public abstract class BeltConnectorCostMixin {
    @Inject(method = "useOn", at = @At(value = "INVOKE", target =
            "Lcom/simibubi/create/content/kinetics/belt/item/BeltConnectorItem;createBelts(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)V"), cancellable = true)
    private void poptartcore$checkCost(UseOnContext context, CallbackInfoReturnable<InteractionResult> callback,
            @Local Player player, @Local(ordinal = 0) BlockPos last, @Local(ordinal = 1) BlockPos first) {
        if (player != null && !player.isCreative()
                && BeltConnectorCost.available(player) < BeltConnectorCost.length(first, last)) {
            callback.setReturnValue(InteractionResult.FAIL);
        }
    }

    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void poptartcore$consumeCost(ItemStack held, int originalAmount, Operation<Void> original,
            @Local Player player, @Local(ordinal = 0) BlockPos last, @Local(ordinal = 1) BlockPos first) {
        BeltConnectorCost.consume(player, held, BeltConnectorCost.length(first, last));
    }
}
