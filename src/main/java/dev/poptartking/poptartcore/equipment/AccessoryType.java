package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public enum AccessoryType {
    RING("rings"),
    CAPE("capes"),
    NECKLACE("necklaces"),
    BELT("belts"),
    UTILITY("utility"),
    BACK("back");

    private final TagKey<Item> tag;

    AccessoryType(String path) {
        this.tag = TagKey.create(Registries.ITEM, PoptartCore.location("equipment/" + path));
    }

    public boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && stack.is(tag);
    }

    public String translationKey() {
        return "slot.poptartcore." + name().toLowerCase(java.util.Locale.ROOT);
    }

    public net.minecraft.resources.ResourceLocation emptySlotTexture() {
        return PoptartCore.location("textures/gui/empty_" + name().toLowerCase(java.util.Locale.ROOT) + "_slot.png");
    }
}
