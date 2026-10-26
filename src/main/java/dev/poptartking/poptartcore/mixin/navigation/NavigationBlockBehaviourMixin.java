package dev.poptartking.poptartcore.mixin.navigation;

import dev.poptartking.poptartcore.navigation.Lodestones;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({BlockBehaviour.class})
public abstract class NavigationBlockBehaviourMixin {
    @Inject(
            method = {"useWithoutItem"},
            at = {@At("HEAD")},
            cancellable = true)
    private void poptartcore$editLodestone(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (poptartcore$edit(state, level, pos, player)) {
            cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
        }
    }

    @Inject(
            method = {"useItemOn"},
            at = {@At("HEAD")},
            cancellable = true)
    private void poptartcore$bindCompass(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (Lodestones.is(state) && stack.is(Items.COMPASS)) {
            cir.setReturnValue(ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION);
        }
    }

    @Unique
    private static boolean poptartcore$edit(BlockState state, Level level, BlockPos pos, Player player) {
        if (Lodestones.is(state) && !player.isSpectator()) {
            if (player instanceof ServerPlayer server) {
                Lodestones.open(server, pos);
            }

            return true;
        } else {
            return false;
        }
    }
}
