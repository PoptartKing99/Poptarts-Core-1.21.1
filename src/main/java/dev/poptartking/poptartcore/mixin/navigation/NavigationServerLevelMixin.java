package dev.poptartking.poptartcore.mixin.navigation;

import dev.poptartking.poptartcore.navigation.Lodestones;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ServerLevel.class})
public abstract class NavigationServerLevelMixin {
    @Inject(
            method = {"onBlockStateChange"},
            at = {@At("HEAD")})
    private void poptartcore$trackLodestone(BlockPos pos, BlockState previous, BlockState next, CallbackInfo ci) {
        Lodestones.onBlockChange((ServerLevel) (Object) this, pos, previous, next);
    }
}
