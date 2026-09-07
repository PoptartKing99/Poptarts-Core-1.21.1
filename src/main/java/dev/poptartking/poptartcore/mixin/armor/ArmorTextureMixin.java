package dev.poptartking.poptartcore.mixin.armor;

import dev.poptartking.poptartcore.armor.IronArmorTextures;
import dev.poptartking.poptartcore.armor.LeatherArmorTextures;
import dev.poptartking.poptartcore.armor.SteelArmorTextures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ArmorItem.class)
public abstract class ArmorTextureMixin {

    @Nullable
    public ResourceLocation getArmorTexture(
            ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        if (LeatherArmorTextures.isLeatherArmor((ArmorItem) (Object) this)) {
            return layer.dyeable() ? LeatherArmorTextures.textureFor(stack) : LeatherArmorTextures.OVERLAY_TEXTURE;
        }
        ResourceLocation ironTexture = IronArmorTextures.textureFor(stack);
        return ironTexture != null ? ironTexture : SteelArmorTextures.textureFor(stack);
    }
}
