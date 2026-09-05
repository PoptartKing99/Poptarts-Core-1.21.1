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

public class MiningHelmetItem extends ArmorItem {
    public MiningHelmetItem(Item.Properties properties) {
        super(PoptartCoreArmorMaterials.MINING_ARMOR_MATERIAL, Type.HELMET, properties);
    }

    public ResourceLocation getArmorTexture(
            ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return PoptartCore.location("textures/armor/mining_helmet.png");
    }
}
