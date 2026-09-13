package dev.poptartking.poptartcore.beekeeping;

import dev.poptartking.poptartcore.registry.PoptartCoreArmorMaterials;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;

public class BeekeeperArmorItem extends ArmorItem {
    public BeekeeperArmorItem(Type type, Properties properties) {
        super(PoptartCoreArmorMaterials.BEEKEEPER_ARMOR_MATERIAL, type, properties);
    }

    public static int pieceCount(LivingEntity entity) {
        int pieces = 0;
        for (EquipmentSlot slot :
                new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (entity.getItemBySlot(slot).getItem() instanceof BeekeeperArmorItem) {
                pieces++;
            }
        }
        return pieces;
    }
}
