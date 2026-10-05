package dev.poptartking.poptartcore.mixin.integration.create;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.belt.BeltSlicer;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeltSlicer.class)
public abstract class BeltSlicerCostMixin {
    @Inject(method = "useConnector", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V",
            ordinal = 0, shift = At.Shift.AFTER))
    private static void poptartcore$chargeExtension(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit, BeltSlicer.Feedback feedback,
            CallbackInfoReturnable<ItemInteractionResult> callback) {
        if (!player.isCreative() && !level.isClientSide) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() instanceof BeltConnectorItem) {
                held.shrink(1);
            }
        }
    }

    @Inject(method = "useWrench", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/level/Level;levelEvent(ILnet/minecraft/core/BlockPos;I)V",
            shift = At.Shift.AFTER))
    private static void poptartcore$refundRetraction(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit, BeltSlicer.Feedback feedback,
            CallbackInfoReturnable<ItemInteractionResult> callback) {
        if (!player.isCreative() && !level.isClientSide) {
            player.getInventory().placeItemBackInInventory(AllItems.BELT_CONNECTOR.asStack());
        }
    }
}
