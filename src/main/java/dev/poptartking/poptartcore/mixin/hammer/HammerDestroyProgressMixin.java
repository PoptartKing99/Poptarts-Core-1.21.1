package dev.poptartking.poptartcore.mixin.hammer;

import dev.poptartking.poptartcore.hammer.HammerMining;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class HammerDestroyProgressMixin {

    @Inject(method = "getDestroyProgress", at = @At("RETURN"), cancellable = true)
    private void poptartcore$synchronizeHammerSpeed(
            Player player, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> callback) {
        callback.setReturnValue(HammerMining.synchronizeDestroyProgress(callback.getReturnValue(), player, level, pos));
    }
}
