package dev.poptartking.poptartcore.mixin.integration.create;

import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import dev.poptartking.poptartcore.integration.create.PoptartBeltCasingAccess;
import dev.poptartking.poptartcore.integration.create.PoptartBeltCasingClient;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeltBlockEntity.class)
public abstract class BeltBlockEntityClientMixin {
    @Inject(method = "getModelData", at = @At("RETURN"), cancellable = true)
    private void poptartcore$addCasingModelData(CallbackInfoReturnable<ModelData> callback) {
        PoptartBeltCasingAccess access = (PoptartBeltCasingAccess) this;
        if (access.poptartcore$getCasing() != null) {
            callback.setReturnValue(callback.getReturnValue().derive()
                    .with(PoptartBeltCasingClient.CASING_PROPERTY, access.poptartcore$getCasing())
                    .build());
        }
    }
}
