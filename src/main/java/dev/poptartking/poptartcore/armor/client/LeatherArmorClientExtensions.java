package dev.poptartking.poptartcore.armor.client;

import dev.poptartking.poptartcore.armor.ArmorTextures;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class LeatherArmorClientExtensions implements IClientItemExtensions {

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(
            LivingEntity entity, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> original) {
        PoptartCoreArmorModel model = pick(armorSlot);

        if (model == null) {
            return original;
        }

        model.slot = armorSlot;
        // NeoForge copies the original model's animated pose after this method returns.

        return model;
    }

    @Override
    public int getArmorLayerTintColor(
            ItemStack stack, LivingEntity entity, ArmorMaterial.Layer layer, int layerIdx, int fallbackColor) {
        if (!layer.dyeable() || !ArmorTextures.isDyedLeather(stack)) {
            return -1;
        }

        return IClientItemExtensions.super.getArmorLayerTintColor(stack, entity, layer, layerIdx, fallbackColor);
    }

    private static PoptartCoreArmorModel pick(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> PoptartCoreModelLayers.LEATHER_HELM_MODEL;
            case CHEST -> PoptartCoreModelLayers.LEATHER_TUNIC_SKIRTLESS_MODEL;
            case LEGS -> PoptartCoreModelLayers.LEATHER_PANTS_MODEL;
            case FEET -> PoptartCoreModelLayers.LEATHER_BOOTS_MODEL;
            default -> null;
        };
    }
}
