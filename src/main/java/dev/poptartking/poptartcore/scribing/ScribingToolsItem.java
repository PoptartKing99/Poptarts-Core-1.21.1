package dev.poptartking.poptartcore.scribing;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ScribingToolsItem extends Item {
    public ScribingToolsItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(Items.INK_SAC);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        ItemStack copiedStack = itemStack.copy();
        copiedStack.setDamageValue(itemStack.getDamageValue() + 1);
        return !copiedStack.isEmpty() && copiedStack.getDamageValue() == copiedStack.getMaxDamage()
                ? new ItemStack(Items.GLASS_BOTTLE)
                : copiedStack;
    }
}
