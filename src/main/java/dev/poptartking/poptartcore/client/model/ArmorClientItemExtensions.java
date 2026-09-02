package dev.poptartking.poptartcore.client.model;

import java.util.function.Supplier;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class ArmorClientItemExtensions implements IClientItemExtensions {

    private final Supplier<PoptartCoreArmorModel> model;

    public ArmorClientItemExtensions(Supplier<PoptartCoreArmorModel> model) {
        this.model = model;
    }

    @Override
    public PoptartCoreArmorModel getHumanoidArmorModel(
            LivingEntity entity,
            ItemStack itemStack,
            EquipmentSlot armorSlot,
            HumanoidModel<?> original
    ) {
        PoptartCoreArmorModel armorModel = this.model.get();

        armorModel.slot = armorSlot;

        return armorModel;
    }
}