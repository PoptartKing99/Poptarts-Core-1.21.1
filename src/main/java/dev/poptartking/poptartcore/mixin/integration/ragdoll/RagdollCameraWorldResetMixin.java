package dev.poptartking.poptartcore.mixin.integration.ragdoll;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Camera.class, priority = 900)
public abstract class RagdollCameraWorldResetMixin {
    @Unique
    private static final Logger POPTARTCORE$LOGGER = LogUtils.getLogger();

    @Unique
    private BlockGetter poptartcore$previousCameraLevel;

    @Inject(method = "setup", at = @At("HEAD"))
    private void poptartcore$forgetPreviousWorldPose(
            BlockGetter level,
            Entity entity,
            boolean detached,
            boolean thirdPersonReverse,
            float partialTick,
            CallbackInfo ci) {
        if (poptartcore$previousCameraLevel != level) {
            try {
                poptartcore$setRagdollField("ragdoll$headPoseValid", false);
                poptartcore$setRagdollField("ragdoll$frozenPosition", null);
                poptartcore$setRagdollField("ragdoll$entryAngleChosen", false);
            } catch (ReflectiveOperationException e) {
                POPTARTCORE$LOGGER.error("Could not clear Ragdoll's camera state on world change", e);
            }
            poptartcore$previousCameraLevel = level;
        }
    }

    @Unique
    private void poptartcore$setRagdollField(String name, Object value) throws ReflectiveOperationException {
        Field field = Camera.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(this, value);
    }
}
