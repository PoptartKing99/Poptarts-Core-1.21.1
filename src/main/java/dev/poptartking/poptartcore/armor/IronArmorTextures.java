package dev.poptartking.poptartcore.armor;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public final class IronArmorTextures {

    private static final ResourceLocation HELMET_TEXTURE =
            PoptartCore.location("textures/armor/iron_helmet.png");
    private static final ResourceLocation CHESTPLATE_TEXTURE =
            PoptartCore.location("textures/armor/iron_chestplate.png");
    private static final ResourceLocation LEGGINGS_TEXTURE =
            PoptartCore.location("textures/armor/iron_leggings.png");
    private static final ResourceLocation BOOTS_TEXTURE =
            PoptartCore.location("textures/armor/iron_boots.png");

    private IronArmorTextures() {}

    @Nullable
    public static ResourceLocation textureFor(ItemStack stack) {
        if (stack.is(Items.IRON_HELMET)) {
            return HELMET_TEXTURE;
        }
        if (stack.is(Items.IRON_CHESTPLATE)) {
            return CHESTPLATE_TEXTURE;
        }
        if (stack.is(Items.IRON_LEGGINGS)) {
            return LEGGINGS_TEXTURE;
        }
        if (stack.is(Items.IRON_BOOTS)) {
            return BOOTS_TEXTURE;
        }
        return null;
    }
}
