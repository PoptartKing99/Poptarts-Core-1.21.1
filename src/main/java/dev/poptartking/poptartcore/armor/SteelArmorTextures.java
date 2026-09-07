package dev.poptartking.poptartcore.armor;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class SteelArmorTextures {

    private static final ResourceLocation HELMET_TEXTURE =
            PoptartCore.location("textures/armor/steel_helmet.png");
    private static final ResourceLocation CHESTPLATE_TEXTURE =
            PoptartCore.location("textures/armor/steel_chestplate.png");
    private static final ResourceLocation LEGGINGS_TEXTURE =
            PoptartCore.location("textures/armor/steel_leggings.png");
    private static final ResourceLocation BOOTS_TEXTURE =
            PoptartCore.location("textures/armor/steel_boots.png");

    private SteelArmorTextures() {}

    @Nullable
    public static ResourceLocation textureFor(ItemStack stack) {
        if (stack.is(PoptartCoreItems.STEEL_HELMET.get())) {
            return HELMET_TEXTURE;
        }
        if (stack.is(PoptartCoreItems.STEEL_CHESTPLATE.get())) {
            return CHESTPLATE_TEXTURE;
        }
        if (stack.is(PoptartCoreItems.STEEL_LEGGINGS.get())) {
            return LEGGINGS_TEXTURE;
        }
        if (stack.is(PoptartCoreItems.STEEL_BOOTS.get())) {
            return BOOTS_TEXTURE;
        }
        return null;
    }
}
