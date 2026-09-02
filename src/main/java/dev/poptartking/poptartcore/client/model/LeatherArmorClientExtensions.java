package dev.poptartking.poptartcore.client.model;

import dev.poptartking.poptartcore.client.PoptartCoreModelLayers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class LeatherArmorClientExtensions implements IClientItemExtensions {

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(
            LivingEntity entity,
            ItemStack itemStack,
            EquipmentSlot armorSlot,
            HumanoidModel<?> original
    ) {
        PoptartCoreArmorModel model = pick(armorSlot);

        if (model == null) {
            return original;
        }

        float partialTick = Minecraft.getInstance().getFrameTimeNs() / 20000000000L;
        float bodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float headRot = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);

        model.slot = armorSlot;
        model.copyFromDefault(original);
        model.setupAnim(
                entity,
                entity.walkAnimation.position(),
                entity.walkAnimation.speed(),
                entity.tickCount + partialTick,
                headRot - bodyRot,
                Mth.lerp(partialTick, entity.xRotO, entity.getXRot())
        );

        return model;
    }

    private static PoptartCoreArmorModel pick(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> PoptartCoreModelLayers.LEATHER_HELM_MODEL;
            case CHEST -> PoptartCoreModelLayers.LEATHER_TUNIC_MODEL;
            case LEGS -> PoptartCoreModelLayers.LEATHER_PANTS_MODEL;
            case FEET -> PoptartCoreModelLayers.LEATHER_BOOTS_MODEL;
            default -> null;
        };
    }
}