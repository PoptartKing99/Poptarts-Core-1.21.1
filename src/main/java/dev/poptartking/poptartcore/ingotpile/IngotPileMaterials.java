package dev.poptartking.poptartcore.ingotpile;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class IngotPileMaterials {
    private IngotPileMaterials() {}

    public static boolean supports(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == Items.IRON_INGOT || item == Items.GOLD_INGOT || item == Items.COPPER_INGOT
                || item == PoptartCoreItems.TIN_INGOT.get() || item == PoptartCoreItems.LEAD_INGOT.get()
                || item == PoptartCoreItems.SILVER_INGOT.get() || item == PoptartCoreItems.BRONZE_INGOT.get()
                || item == PoptartCoreItems.STEEL_INGOT.get() || item == PoptartCoreItems.TITANIUM_INGOT.get();
    }

    public static ResourceLocation texture(Item item) {
        if (item == Items.IRON_INGOT) return PoptartCore.location("block/ingot_pile/iron");
        if (item == Items.GOLD_INGOT) return PoptartCore.location("block/ingot_pile/gold");
        if (item == Items.COPPER_INGOT) return PoptartCore.location("block/ingot_pile/copper");
        String name;
        if (item == PoptartCoreItems.TIN_INGOT.get()) name = "tin";
        else if (item == PoptartCoreItems.LEAD_INGOT.get()) name = "lead";
        else if (item == PoptartCoreItems.SILVER_INGOT.get()) name = "silver";
        else if (item == PoptartCoreItems.BRONZE_INGOT.get()) name = "bronze";
        else if (item == PoptartCoreItems.STEEL_INGOT.get()) name = "steel";
        else name = "titanium";
        return PoptartCore.location("block/ingot_pile/" + name);
    }
}
