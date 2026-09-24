package dev.poptartking.poptartcore.mixin.integration.nomansland;

import com.farcr.nomansland.common.block.tap.TapBlock;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TapBlock.class)
public class TapBlockMixin {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void poptartcore$allowFluidBarrelSupport(
            BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Direction supportDirection = state.getValue(TapBlock.FACING).getOpposite();
        if (level.getBlockState(pos.relative(supportDirection)).is(PoptartCoreBlocks.FLUID_BARREL.get())) {
            cir.setReturnValue(true);
        }
    }
}
