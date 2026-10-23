package dev.poptartking.poptartcore.catalog;

import dev.poptartking.poptartcore.equipment.EquipmentRing;
import java.util.Set;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TieredItem;

/** One category per visible catalog entry; newly added entries cannot disappear between tabs. */
public enum CreativeCatalogGroup {
    BUILDING,
    TOOLS,
    EQUIPMENT,
    MATERIALS;

    private static final Set<String> TOOLS_IDS = Set.of(
            "anvil",
            "chipped_anvil",
            "damaged_anvil",
            "primitive_anvil",
            "chipped_primitive_anvil",
            "damaged_primitive_anvil",
            "workbench",
            "scribing_table",
            "quern",
            "millstone",
            "bloomery",
            "blast_furnace",
            "crucible",
            "fluid_barrel",
            "portable_engine",
            "unfired_ingot_mould",
            "ingot_mould",
            "unfired_plate_mould",
            "plate_mould",
            "firestarter",
            "scribing_tools",
            "bone_pick",
            "flint_axe",
            "flint_shovel",
            "hammer",
            "sledgehammer",
            "ruined_sword",
            "ruined_pickaxe",
            "ruined_axe",
            "ruined_shovel",
            "ruined_knife",
            "bronze_sword",
            "bronze_pickaxe",
            "bronze_axe",
            "bronze_shovel",
            "bronze_knife",
            "steel_sword",
            "steel_pickaxe",
            "steel_axe",
            "steel_shovel",
            "steel_knife",
            "repeating_crossbow",
            "bee_smoker",
            "tree_tap",
            "beehive_support",
            "lifegem",
            "heart_crystal");
    private static final Set<String> MATERIAL_BLOCK_IDS = Set.of(
            "tin_ore",
            "deepslate_tin_ore",
            "raw_tin",
            "raw_tin_block",
            "tin_ingot",
            "tin_nugget",
            "tin_block",
            "lead_ore",
            "deepslate_lead_ore",
            "raw_lead",
            "raw_lead_block",
            "lead_ingot",
            "lead_nugget",
            "lead_block",
            "magnetite",
            "magnetite_block",
            "cinnabar",
            "cinnabar_block",
            "mercury_droplet",
            "mercury_ingot",
            "mercury_block",
            "silver_ore",
            "deepslate_silver_ore",
            "raw_silver",
            "raw_silver_block",
            "silver_ingot",
            "silver_nugget",
            "silver_block",
            "titanium_ore",
            "deepslate_titanium_ore",
            "cassiterite_titanium_ore",
            "cylindrite_titanium_ore",
            "raw_titanium",
            "raw_titanium_block",
            "titanium_shard",
            "titanium_ingot",
            "titanium_nugget",
            "titanium_plate",
            "bronze_ingot",
            "bronze_nugget",
            "bronze_plate",
            "bronze_block",
            "iron_bloom",
            "steel_ingot",
            "steel_nugget",
            "steel_plate",
            "steel_block",
            "coal_coke_block",
            "wax_block",
            "rift_sediment",
            "rift_sediment_block");

    public static CreativeCatalogGroup forItem(String id, Item item) {
        if (item instanceof ArmorItem || item instanceof EquipmentRing || id.endsWith("_cape")) return EQUIPMENT;
        if (TOOLS_IDS.contains(id)
                || id.startsWith("beehive_")
                || item instanceof TieredItem
                || item instanceof ProjectileWeaponItem) return TOOLS;
        if (item instanceof BlockItem) return MATERIAL_BLOCK_IDS.contains(id) ? MATERIALS : BUILDING;
        return MATERIALS;
    }
}
