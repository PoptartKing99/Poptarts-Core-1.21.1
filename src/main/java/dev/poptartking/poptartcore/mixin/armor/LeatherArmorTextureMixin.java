package dev.poptartking.poptartcore.mixin.armor;

import dev.poptartking.poptartcore.leather.LeatherArmorDesigns;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ArmorItem.class)
public abstract class LeatherArmorTextureMixin {

    @Nullable
    public ResourceLocation getArmorTexture(
            ItemStack stack,
            Entity entity,
            EquipmentSlot slot,
            ArmorMaterial.Layer layer,
            boolean innerModel
    ) {
        if (LeatherArmorDesigns.isLeatherArmor((ArmorItem) (Object) this)) {
            return layer.dyeable()
                    ? LeatherArmorDesigns.textureFor(stack)
                    : LeatherArmorDesigns.OVERLAY_TEXTURE;
        }

        return null;
    }
}