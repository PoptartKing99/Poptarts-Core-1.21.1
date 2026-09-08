package dev.poptartking.poptartcore.mixin.render;

import dev.poptartking.poptartcore.blastfurnace.BlastFurnaceBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class BlastFurnaceBreakRenderMixin {

    @Inject(method = "destroyBlockProgress", at = @At("HEAD"), cancellable = true)
    private void poptartcore$redirectBlastFurnaceBreaking(
            int breakerId, BlockPos pos, int progress, CallbackInfo callback) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        BlockState state = minecraft.level.getBlockState(pos);
        if (state.getBlock() instanceof BlastFurnaceBlock
                && state.getValue(BlastFurnaceBlock.HALF) == DoubleBlockHalf.UPPER) {
            ((LevelRenderer) (Object) this).destroyBlockProgress(breakerId, pos.below(), progress);
            callback.cancel();
        }
    }
}
