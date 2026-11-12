package dev.poptartking.poptartcore.mixin.summoning;

import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import dev.poptartking.poptartcore.summoning.AllaySummonController;
import dev.poptartking.poptartcore.summoning.PixieSummons;
import dev.poptartking.poptartcore.summoning.PixieType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.allay.Allay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Allay.class)
public abstract class SummonedAllayMixin {
    @Inject(method = "customServerAiStep", at = @At("HEAD"), cancellable = true)
    private void poptartcore$summonAi(CallbackInfo ci) {
        var self = (Allay) (Object) this;
        if (!PixieSummons.isSummoned(self) || !(self.level() instanceof ServerLevel level)) return;
        var data = self.getData(PoptartCoreAttachments.ALLAY_DATA);
        if (data.hasOwner()) AllaySummonController.tick(level, self, data);
        else PixieSummons.tickOwnerlessLifespan(level, self, data, PixieType.ALLAY);
        ci.cancel();
    }

    @Inject(method = "canPickUpLoot", at = @At("HEAD"), cancellable = true)
    private void poptartcore$useSummonPickup(CallbackInfoReturnable<Boolean> cir) {
        if (PixieSummons.isSummoned((Allay) (Object) this)) cir.setReturnValue(false);
    }

    @Inject(method = "setDancing", at = @At("HEAD"), cancellable = true)
    private void poptartcore$noSummonDancing(boolean dancing, CallbackInfo ci) {
        if (PixieSummons.isSummoned((Allay) (Object) this)) ci.cancel();
    }
}
