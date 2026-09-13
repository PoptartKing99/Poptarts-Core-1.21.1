package dev.poptartking.poptartcore.mixin.bee;

import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class BeeSmokerArmPoseMixin {
    @Unique
    private static final float POPTARTCORE$SMOKER_YAW = 0.45F;

    @Shadow
    @Final
    public ModelPart rightArm;

    @Shadow
    @Final
    public ModelPart leftArm;

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void poptartcore$faceSmokerForward(
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo info) {
        if (!entity.isUsingItem() || !entity.getUseItem().is(PoptartCoreItems.BEE_SMOKER.get())) {
            return;
        }

        boolean rightArmUsed =
                (entity.getUsedItemHand() == InteractionHand.MAIN_HAND) == (entity.getMainArm() == HumanoidArm.RIGHT);
        if (rightArmUsed) {
            rightArm.yRot += POPTARTCORE$SMOKER_YAW;
        } else {
            leftArm.yRot -= POPTARTCORE$SMOKER_YAW;
        }
    }
}
