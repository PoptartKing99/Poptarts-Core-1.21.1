package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public final class PoptartCoreTags {
    public static final TagKey<Item> HAMMERS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "tools/hammers"));
    public static final TagKey<Item> KNIVES =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("farmersdelight", "tools/knives"));
    public static final TagKey<Item> HAMMER_REPAIR_MATERIALS =
            TagKey.create(Registries.ITEM, PoptartCore.location("hammer_repair_materials"));
    public static final TagKey<Item> BLAST_FURNACE_ALLOWED =
            TagKey.create(Registries.ITEM, PoptartCore.location("blast_furnace_allowed"));
    public static final TagKey<Item> BLAST_FURNACE_EFFICIENT =
            TagKey.create(Registries.ITEM, PoptartCore.location("blast_furnace_efficient"));
    public static final TagKey<Item> PORTABLE_ENGINE_ALLOWED_FUEL =
            TagKey.create(Registries.ITEM, PoptartCore.location("portable_engine_allowed_fuel"));
    public static final TagKey<Block> HAMMER_NO_SPREAD =
            TagKey.create(Registries.BLOCK, PoptartCore.location("hammer_no_spread"));
    public static final TagKey<Block> UNWAXABLE = TagKey.create(Registries.BLOCK, PoptartCore.location("unwaxable"));
    public static final TagKey<Fluid> BARREL_BLACKLISTED_FLUIDS =
            TagKey.create(Registries.FLUID, PoptartCore.location("barrel_blacklisted_fluids"));

    private PoptartCoreTags() {}

    public static boolean isBlastFurnaceFuel(ItemStack stack) {
        return stack.is(BLAST_FURNACE_ALLOWED) || stack.is(BLAST_FURNACE_EFFICIENT);
    }
}
