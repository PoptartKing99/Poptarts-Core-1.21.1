package dev.poptartking.poptartcore.catalog;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class ToolTestSupport {
    private ToolTestSupport() {}

    static Item item(String id) {
        var key = id.contains(":") ? ResourceLocation.parse(id) : PoptartCore.location(id);
        if (!BuiltInRegistries.ITEM.containsKey(key))
            throw new IllegalStateException("Missing registered item: " + key);
        return BuiltInRegistries.ITEM.get(key);
    }

    static double effectiveAttribute(ItemStack stack, Holder<Attribute> attribute, double base) {
        var instance = new AttributeInstance(attribute, ignored -> {});
        instance.setBaseValue(base);
        stack.forEachModifier(EquipmentSlot.MAINHAND, (holder, modifier) -> {
            if (holder.equals(attribute)) instance.addTransientModifier(modifier);
        });
        return instance.getValue();
    }
}
