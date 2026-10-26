package dev.poptartking.poptartcore.mixin.integration.ragdoll;

import com.farcr.ragdoll.content.entity.CorpseEntity;
import dev.poptartking.poptartcore.navigation.CorpseDeathMarkers;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CorpseEntity.class)
public abstract class CorpseDeathMarkerMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    private void poptartcore$clearDestroyedCorpseMarker(Entity.RemovalReason reason, CallbackInfo ci) {
        CorpseDeathMarkers.removed((CorpseEntity) (Object) this, reason);
    }
}
