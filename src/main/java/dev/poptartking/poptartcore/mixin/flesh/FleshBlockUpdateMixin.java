package dev.poptartking.poptartcore.mixin.flesh;

import dev.poptartking.poptartcore.flesh.FleshWaterTint;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientLevel.class})
public class FleshBlockUpdateMixin {
    @Inject(
            method = {"sendBlockUpdated"},
            at = {@At("HEAD")})
    private void poptartcore$trackBuds(
            BlockPos pos, BlockState before, BlockState after, int flags, CallbackInfo callback) {
        FleshWaterTint.blockChanged(pos, before, after);
    }
}
