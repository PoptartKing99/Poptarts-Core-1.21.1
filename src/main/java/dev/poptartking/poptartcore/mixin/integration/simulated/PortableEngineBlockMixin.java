package dev.poptartking.poptartcore.mixin.integration.simulated;

import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlock;
import net.mehvahdjukaar.moonlight.api.block.IWashable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PortableEngineBlock.class)
public abstract class PortableEngineBlockMixin implements IWashable {
    @Shadow
    public abstract DyeColor getColor();

    @Inject(method = "onRemove", at = @At("HEAD"), cancellable = true)
    private void poptartcore$keepInventoryOnStateChange(
            BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving, CallbackInfo callback) {
        if (newState.is(PoptartCoreBlocks.PORTABLE_ENGINE.get())) {
            callback.cancel();
        }
    }

    @Override
    public boolean tryWash(Level level, BlockPos pos, BlockState state, Vec3 hitVec) {
        if (getColor() == null) {
            return false;
        }
        if (!level.isClientSide()) {
            level.setBlockAndUpdate(pos, PoptartCoreBlocks.PORTABLE_ENGINE.get().withPropertiesOf(state));
        }
        return true;
    }
}
