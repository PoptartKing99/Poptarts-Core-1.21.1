package dev.poptartking.poptartcore.item;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreArmorMaterials;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RawHideArmorItem extends ArmorItem {
    public RawHideArmorItem(ArmorItem.Type type, Item.Properties properties) {
        super(PoptartCoreArmorMaterials.RAW_HIDE_ARMOR_MATERIAL, type, properties);
    }

    public ResourceLocation getArmorTexture(
            ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return PoptartCore.location("textures/armor/raw_hide_armor.png");
    }
}
