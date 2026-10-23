package dev.poptartking.poptartcore.mixin.integration.ragdoll;

import com.farcr.ragdoll.system.EntityGeometry;
import com.farcr.ragdoll.system.RagdollRenderManager;
import com.farcr.ragdoll.util.NameHolder;
import dev.poptartking.poptartcore.integration.ragdoll.CorpseBackpackRender;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RagdollRenderManager.class)
public abstract class CorpseBackpackGeometryMixin {
    @Inject(
            method =
                    "findMatchingGeometry(Lcom/farcr/ragdoll/util/NameHolder;)Lcom/farcr/ragdoll/system/EntityGeometry;",
            at = @At("HEAD"),
            cancellable = true)
    private static void poptartcore$preserveAccessoryGeometry(
            NameHolder part, CallbackInfoReturnable<EntityGeometry> cir) {
        // Mapped child parts must not switch back to the ragdoll stack during raw accessory rendering.
        if (CorpseBackpackRender.drawing()) cir.setReturnValue(null);
    }

    @Inject(method = "getBoundRenderer", at = @At("HEAD"), cancellable = true)
    private static void poptartcore$drawBackpackParts(CallbackInfoReturnable<LivingEntityRenderer<?, ?>> cir) {
        if (CorpseBackpackRender.drawing()) cir.setReturnValue(null);
    }
}
