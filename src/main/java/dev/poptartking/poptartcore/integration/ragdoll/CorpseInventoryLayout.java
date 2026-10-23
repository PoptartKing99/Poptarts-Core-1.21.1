package dev.poptartking.poptartcore.integration.ragdoll;

import dev.poptartking.poptartcore.mixin.equipment.SlotPositionAccessor;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class CorpseInventoryLayout {
    public static final int WIDTH = 182;
    public static final int HEIGHT = 274;

    private CorpseInventoryLayout() {}

    public static int accessoryX(int index) {
        return switch (index) {
            case 0, 1, 3, 4 -> 131;
            case 2, 5, 6 -> 113;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    public static int accessoryY(int index) {
        return switch (index) {
            case 3, 6 -> 19;
            case 0, 2 -> 37;
            case 1, 5 -> 55;
            case 4 -> 73;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    public static void alignVanillaSlots(AbstractContainerMenu menu) {
        for (int i = 0; i < 9; i++) position(menu, i, 11 + i * 18, 153);
        for (int i = 0; i < 27; i++) position(menu, 9 + i, 11 + i % 9 * 18, 95 + i / 9 * 18);
        for (int i = 0; i < 4; i++) position(menu, 36 + i, 23, 73 - i * 18);
        position(menu, 40, 113, 73);
        for (int i = 0; i < 9; i++) position(menu, 41 + i, 11 + i * 18, 250);
        for (int i = 0; i < 27; i++) position(menu, 50 + i, 11 + i % 9 * 18, 192 + i / 9 * 18);
    }

    private static void position(AbstractContainerMenu menu, int index, int x, int y) {
        var slot = (SlotPositionAccessor) menu.getSlot(index);
        slot.poptartcore$setX(x);
        slot.poptartcore$setY(y);
    }
}
