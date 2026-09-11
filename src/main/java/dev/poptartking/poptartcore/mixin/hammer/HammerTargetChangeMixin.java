package dev.poptartking.poptartcore.mixin.hammer;

import dev.poptartking.poptartcore.hammer.HammerMining;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class HammerTargetChangeMixin {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void poptartcore$invalidateHammerTarget(
            BlockPos pos, BlockState state, boolean moving, CallbackInfoReturnable<BlockState> callback) {
        if (callback.getReturnValue() != null) {
            HammerMining.onBlockChanged(((LevelChunk) (Object) this).getLevel(), pos);
        }
    }
}
