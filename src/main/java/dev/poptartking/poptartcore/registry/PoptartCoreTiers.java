package dev.poptartking.poptartcore.registry;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public final class PoptartCoreTiers {
    public static final Tier FLINT =
            new SimpleTier(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 100, 3.0F, 0.0F, 5, () -> Ingredient.of(Items.FLINT));
    public static final Tier BONE =
            new SimpleTier(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 150, 3.0F, 0.0F, 3, () -> Ingredient.of(Items.BONE));
    public static final Tier BRONZE = new SimpleTier(
            BlockTags.INCORRECT_FOR_STONE_TOOL,
            275,
            4.0F,
            1.0F,
            10,
            () -> Ingredient.of(PoptartCoreItems.BRONZE_INGOT.get()));
    public static final Tier STEEL = new SimpleTier(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1250,
            8.0F,
            3.0F,
            15,
            () -> Ingredient.of(PoptartCoreItems.STEEL_INGOT.get()));

    private PoptartCoreTiers() {}
}
