package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

import static net.minecraft.sounds.SoundEvents.*;
import static net.minecraft.world.item.Items.*;

public class PoptartCoreArmorMaterials {

    public static final Holder<ArmorMaterial> MINING_ARMOR_MATERIAL = register("mining",
            protection(2, 0, 0, 0), 1, ARMOR_EQUIP_IRON, 1.0F, 0.0F, () -> Ingredient.of(IRON_INGOT));

    public static final Holder<ArmorMaterial> RAW_HIDE_ARMOR_MATERIAL = register("raw_hide",
            protection(2, 4, 3, 2), 8, ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(LEATHER));

    private static EnumMap<ArmorItem.Type, Integer> protection(int helmet, int chestplate, int leggings, int boots) {
        return Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.HELMET, helmet);
            map.put(ArmorItem.Type.CHESTPLATE, chestplate);
            map.put(ArmorItem.Type.LEGGINGS, leggings);
            map.put(ArmorItem.Type.BOOTS, boots);
        });
    }

    private static Holder<ArmorMaterial> register(String name, EnumMap<ArmorItem.Type, Integer> protection,
                                                  int enchantability, Holder<SoundEvent> sound, float toughness, float knockback, Supplier<Ingredient> repair) {
        var location = PoptartCore.location(name);
        return Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL, location,
                new ArmorMaterial(protection, enchantability, sound, repair,
                        List.of(new ArmorMaterial.Layer(location)), toughness, knockback));
    }
}