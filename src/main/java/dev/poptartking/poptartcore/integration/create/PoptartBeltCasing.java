package dev.poptartking.poptartcore.integration.create;

import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public enum PoptartBeltCasing {
    INDUSTRIAL("industrial_plating"),
    TREATED_WOOD("treated_wood");

    private final String id;

    PoptartBeltCasing(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public Block block() {
        return this == INDUSTRIAL ? PoptartCoreBlocks.INDUSTRIAL_PLATING.get()
                : PoptartCoreBlocks.TREATED_WOOD_CASING.get();
    }

    public static PoptartBeltCasing fromItem(ItemStack stack) {
        for (PoptartBeltCasing casing : values()) {
            if (stack.is(casing.block().asItem())) {
                return casing;
            }
        }
        return null;
    }

    public static PoptartBeltCasing fromId(String id) {
        for (PoptartBeltCasing casing : values()) {
            if (casing.id.equals(id)) {
                return casing;
            }
        }
        return null;
    }
}
