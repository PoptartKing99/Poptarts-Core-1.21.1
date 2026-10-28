package dev.poptartking.poptartcore.catalog;

import dev.poptartking.poptartcore.backpack.BackpackItem;
import dev.poptartking.poptartcore.equipment.EquipmentRing;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TieredItem;

/** Sections of the single creative tab, in display order. Definitions can override the type-based defaults. */
public enum CreativeCatalogGroup {
    BUILDING,
    TOOLS,
    EQUIPMENT,
    MATERIALS;

    public static CreativeCatalogGroup forItem(Item item) {
        if (item instanceof ArmorItem || item instanceof EquipmentRing || item instanceof BackpackItem)
            return EQUIPMENT;
        if (item instanceof TieredItem || item instanceof ProjectileWeaponItem) return TOOLS;
        return item instanceof BlockItem ? BUILDING : MATERIALS;
    }
}
