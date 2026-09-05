package dev.poptartking.poptartcore.leather;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class LeatherArmorTextures {

    public static final ResourceLocation DYED_TEXTURE = PoptartCore.location("textures/armor/armor_leather.png");

    public static final ResourceLocation UNDYED_TEXTURE =
            PoptartCore.location("textures/armor/armor_leather_brown.png");

    public static final ResourceLocation OVERLAY_TEXTURE =
            PoptartCore.location("textures/armor/armor_leather_overlay.png");

    private LeatherArmorTextures() {}

    public static boolean isLeatherArmor(ArmorItem item) {
        return item == Items.LEATHER_HELMET
                || item == Items.LEATHER_CHESTPLATE
                || item == Items.LEATHER_LEGGINGS
                || item == Items.LEATHER_BOOTS;
    }

    public static boolean isDyed(ItemStack stack) {
        return stack.has(DataComponents.DYED_COLOR);
    }

    public static ResourceLocation textureFor(ItemStack stack) {
        return isDyed(stack) ? DYED_TEXTURE : UNDYED_TEXTURE;
    }
}
