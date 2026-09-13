package dev.poptartking.poptartcore.mixin.bee;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.poptartking.poptartcore.beekeeping.BeeSmokerItem;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
public abstract class BeeSmokerReequipMixin {
    @WrapOperation(
            method = "renderArmWithItem",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private void poptartcore$holdOutBeeSmoker(
            ItemInHandRenderer renderer,
            LivingEntity entity,
            ItemStack stack,
            ItemDisplayContext context,
            boolean leftHand,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            Operation<Void> original) {
        boolean holdingOutSmoker =
                stack.getItem() instanceof BeeSmokerItem && entity.isUsingItem() && entity.getUseItem() == stack;
        if (!holdingOutSmoker) {
            original.call(renderer, entity, stack, context, leftHand, poseStack, buffer, light);
            return;
        }

        poseStack.pushPose();
        poseStack.translate(leftHand ? 0.11F : -0.11F, 0.0F, -0.07F);
        original.call(renderer, entity, stack, context, leftHand, poseStack, buffer, light);
        poseStack.popPose();
    }

    @WrapOperation(
            method = "tick",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/neoforged/neoforge/client/ClientHooks;shouldCauseReequipAnimation(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)Z"))
    private boolean poptartcore$ignoreBeeSmokerDurabilityReequip(
            ItemStack from, ItemStack to, int slot, Operation<Boolean> original) {
        boolean shouldReequip = original.call(from, to, slot);
        if (!shouldReequip || !(from.getItem() instanceof BeeSmokerItem) || !(to.getItem() instanceof BeeSmokerItem)) {
            return shouldReequip;
        }

        ItemStack previous = from.copy();
        ItemStack current = to.copy();
        previous.setDamageValue(0);
        current.setDamageValue(0);
        return !ItemStack.matches(previous, current);
    }
}
