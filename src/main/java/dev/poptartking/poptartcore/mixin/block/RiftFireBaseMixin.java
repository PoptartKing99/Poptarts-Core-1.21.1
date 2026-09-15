package dev.poptartking.poptartcore.mixin.block;

import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseFireBlock.class)
public class RiftFireBaseMixin {
    @Inject(method = "getState", at = @At("HEAD"), cancellable = true)
    private static void poptartcore$useRiftFire(
            BlockGetter level, BlockPos pos, CallbackInfoReturnable<BlockState> callback) {
        if (level.getBlockState(pos.below()).is(PoptartCoreBlocks.RIFT_SEDIMENT_BLOCK.get())) {
            callback.setReturnValue(PoptartCoreBlocks.RIFT_FIRE.get().defaultBlockState());
        }
    }
}
