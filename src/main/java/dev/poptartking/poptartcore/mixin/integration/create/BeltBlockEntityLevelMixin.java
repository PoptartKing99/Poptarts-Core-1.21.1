package dev.poptartking.poptartcore.mixin.integration.create;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import dev.poptartking.poptartcore.integration.create.PoptartBeltCasingAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BeltBlockEntityLevelMixin {
    @Inject(method = "setLevel", at = @At("TAIL"))
    private void poptartcore$refreshLoadedBelt(Level level, CallbackInfo callback) {
        if (!level.isClientSide || !((Object) this instanceof BeltBlockEntity belt)
                || ((PoptartBeltCasingAccess) belt).poptartcore$getCasing() == null) {
            return;
        }
        belt.requestModelDataUpdate();
        level.sendBlockUpdated(belt.getBlockPos(), belt.getBlockState(), belt.getBlockState(), 16);
    }
}
