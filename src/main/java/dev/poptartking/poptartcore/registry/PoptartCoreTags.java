package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class PoptartCoreTags {
    public static final TagKey<Item> HAMMERS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "tools/hammers"));
    public static final TagKey<Item> HAMMER_REPAIR_MATERIALS =
            TagKey.create(Registries.ITEM, PoptartCore.location("hammer_repair_materials"));
    public static final TagKey<Item> BLAST_FURNACE_ALLOWED =
            TagKey.create(Registries.ITEM, PoptartCore.location("blast_furnace_allowed"));
    public static final TagKey<Item> BLAST_FURNACE_EFFICIENT =
            TagKey.create(Registries.ITEM, PoptartCore.location("blast_furnace_efficient"));

    private PoptartCoreTags() {}

    public static boolean isBlastFurnaceFuel(ItemStack stack) {
        return stack.is(BLAST_FURNACE_ALLOWED) || stack.is(BLAST_FURNACE_EFFICIENT);
    }
}
