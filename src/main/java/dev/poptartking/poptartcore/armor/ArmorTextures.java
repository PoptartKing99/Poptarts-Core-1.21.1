package dev.poptartking.poptartcore.armor;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/** Selects the custom texture used by each supported piece of armor. */
public final class ArmorTextures {

    private static final ResourceLocation LEATHER_DYED_TEXTURE =
            PoptartCore.location("textures/armor/armor_leather.png");
    private static final ResourceLocation LEATHER_UNDYED_TEXTURE =
            PoptartCore.location("textures/armor/armor_leather_brown.png");
    private static final ResourceLocation LEATHER_OVERLAY_TEXTURE =
            PoptartCore.location("textures/armor/armor_leather_overlay.png");

    private static final Map<Item, ResourceLocation> TEXTURES = Map.ofEntries(
            Map.entry(Items.IRON_HELMET, PoptartCore.location("textures/armor/iron_helmet.png")),
            Map.entry(Items.IRON_CHESTPLATE, PoptartCore.location("textures/armor/iron_chestplate.png")),
            Map.entry(Items.IRON_LEGGINGS, PoptartCore.location("textures/armor/iron_leggings.png")),
            Map.entry(Items.IRON_BOOTS, PoptartCore.location("textures/armor/iron_boots.png")),
            Map.entry(PoptartCoreItems.STEEL_HELMET.get(), PoptartCore.location("textures/armor/steel_helmet.png")),
            Map.entry(
                    PoptartCoreItems.STEEL_CHESTPLATE.get(),
                    PoptartCore.location("textures/armor/steel_chestplate.png")),
            Map.entry(PoptartCoreItems.STEEL_LEGGINGS.get(), PoptartCore.location("textures/armor/steel_leggings.png")),
            Map.entry(PoptartCoreItems.STEEL_BOOTS.get(), PoptartCore.location("textures/armor/steel_boots.png")),
            Map.entry(
                    PoptartCoreItems.BEEKEEPER_HELMET.get(),
                    PoptartCore.location("textures/armor/beekeeper_armor.png")),
            Map.entry(
                    PoptartCoreItems.BEEKEEPER_CHESTPLATE.get(),
                    PoptartCore.location("textures/armor/beekeeper_armor.png")),
            Map.entry(
                    PoptartCoreItems.BEEKEEPER_LEGGINGS.get(),
                    PoptartCore.location("textures/armor/beekeeper_armor.png")),
            Map.entry(
                    PoptartCoreItems.BEEKEEPER_BOOTS.get(), PoptartCore.location("textures/armor/beekeeper_armor.png")),
            Map.entry(PoptartCoreItems.MINING_HELMET.get(), PoptartCore.location("textures/armor/mining_helmet.png")),
            Map.entry(
                    PoptartCoreItems.RAW_HIDE_HELMET.get(), PoptartCore.location("textures/armor/raw_hide_armor.png")),
            Map.entry(
                    PoptartCoreItems.RAW_HIDE_CHESTPLATE.get(),
                    PoptartCore.location("textures/armor/raw_hide_armor.png")),
            Map.entry(
                    PoptartCoreItems.RAW_HIDE_LEGGINGS.get(),
                    PoptartCore.location("textures/armor/raw_hide_armor.png")));

    private ArmorTextures() {}

    @Nullable
    public static ResourceLocation textureFor(ItemStack stack, ArmorMaterial.Layer layer) {
        if (isLeatherArmor(stack)) {
            if (!layer.dyeable()) {
                return LEATHER_OVERLAY_TEXTURE;
            }
            return isDyedLeather(stack) ? LEATHER_DYED_TEXTURE : LEATHER_UNDYED_TEXTURE;
        }

        // Returning null lets Minecraft use the armor material's normal texture.
        return TEXTURES.get(stack.getItem());
    }

    public static boolean isDyedLeather(ItemStack stack) {
        return stack.has(DataComponents.DYED_COLOR);
    }

    private static boolean isLeatherArmor(ItemStack stack) {
        return stack.is(Items.LEATHER_HELMET)
                || stack.is(Items.LEATHER_CHESTPLATE)
                || stack.is(Items.LEATHER_LEGGINGS)
                || stack.is(Items.LEATHER_BOOTS);
    }
}
