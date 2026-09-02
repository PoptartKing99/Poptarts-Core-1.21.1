package dev.poptartking.poptartcore.client.model;

import java.util.function.Supplier;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class ArmorClientItemExtensions implements IClientItemExtensions {

    private final Supplier<LodestoneArmorModel> model;

    public ArmorClientItemExtensions(Supplier<LodestoneArmorModel> model) {
        this.model = model;
    }

    @Override
    public LodestoneArmorModel getHumanoidArmorModel(
            LivingEntity entity,
            ItemStack itemStack,
            EquipmentSlot armorSlot,
            HumanoidModel<?> original
    ) {
        LodestoneArmorModel armorModel = this.model.get();

        armorModel.slot = armorSlot;

        return armorModel;
    }
}