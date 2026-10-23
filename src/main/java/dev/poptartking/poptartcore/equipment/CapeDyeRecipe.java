package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.registry.PoptartCoreRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class CapeDyeRecipe extends CustomRecipe {
    public CapeDyeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !result(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return result(input);
    }

    private static ItemStack result(CraftingInput input) {
        ItemStack cape = ItemStack.EMPTY;
        DyeItem dye = null;
        for (int i = 0; i < input.size(); i++) {
            var stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (CapeColors.isCape(stack) && cape.isEmpty()) cape = stack;
            else if (stack.getItem() instanceof DyeItem item && dye == null) dye = item;
            else return ItemStack.EMPTY;
        }
        if (cape.isEmpty() || dye == null) return ItemStack.EMPTY;
        var result = cape.copyWithCount(1);
        CapeColors.setColor(result, dye.getDyeColor());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PoptartCoreRecipes.CAPE_DYE_SERIALIZER.get();
    }
}
