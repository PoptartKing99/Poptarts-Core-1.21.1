package dev.poptartking.poptartcore.mixin.spider;

import dev.poptartking.poptartcore.spider.WebShootingSpider;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpiderModel.class)
public abstract class SpiderWebPoseMixin {
    @Shadow
    @Final
    private ModelPart root;

    @Inject(method = "setupAnim", at = @At("HEAD"))
    private void poptartcore$resetWebPose(
            Entity entity, float swing, float amount, float age, float yaw, float pitch, CallbackInfo ci) {
        root.getChild("body1").resetPose();
    }

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void poptartcore$applyWebPose(
            Entity entity, float swing, float amount, float age, float yaw, float pitch, CallbackInfo ci) {
        if (entity instanceof WebShootingSpider spider && spider.poptartcore$isWebShooting()) {
            ModelPart abdomen = root.getChild("body1");
            abdomen.xRot += (float) Math.PI / 6;
            abdomen.y -= 5;
            abdomen.z -= 2;
        }
    }
}
