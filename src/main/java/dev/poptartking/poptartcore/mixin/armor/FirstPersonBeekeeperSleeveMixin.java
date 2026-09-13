package dev.poptartking.poptartcore.mixin.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.armor.client.BeekeeperArmorModel;
import dev.poptartking.poptartcore.armor.client.PoptartCoreModelLayers;
import dev.poptartking.poptartcore.beekeeping.BeekeeperArmorItem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class FirstPersonBeekeeperSleeveMixin {
    @Inject(method = "renderRightHand", at = @At("TAIL"))
    private void poptartcore$renderRightBeekeeperSleeve(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            AbstractClientPlayer player,
            CallbackInfo info) {
        poptartcore$renderSleeve(poseStack, buffer, combinedLight, player, true);
    }

    @Inject(method = "renderLeftHand", at = @At("TAIL"))
    private void poptartcore$renderLeftBeekeeperSleeve(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            AbstractClientPlayer player,
            CallbackInfo info) {
        poptartcore$renderSleeve(poseStack, buffer, combinedLight, player, false);
    }

    private void poptartcore$renderSleeve(
            PoseStack poseStack, MultiBufferSource buffer, int light, AbstractClientPlayer player, boolean right) {
        if (!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof BeekeeperArmorItem)) {
            return;
        }

        BeekeeperArmorModel armorModel = PoptartCoreModelLayers.BEEKEEPER_ARMOR_MODEL;
        if (armorModel == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        PlayerModel<AbstractClientPlayer> playerModel =
                (PlayerModel<AbstractClientPlayer>) ((PlayerRenderer) (Object) this).getModel();
        ModelPart armorArm = right ? armorModel.rightArm : armorModel.leftArm;
        armorArm.copyFrom(right ? playerModel.rightArm : playerModel.leftArm);
        armorArm.visible = true;
        VertexConsumer consumer = buffer.getBuffer(
                RenderType.armorCutoutNoCull(PoptartCore.location("textures/armor/beekeeper_armor.png")));
        armorArm.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY);
    }
}
