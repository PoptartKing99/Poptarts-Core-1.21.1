package dev.poptartking.poptartcore.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.resources.ResourceLocation;

/** Wayfarer's cropped-machine layout, using the coordinates of our current menus. */
record MachineLayout(String machine, int cropY, int height, int capacity) {
    static final MachineLayout CRUCIBLE = new MachineLayout("crucible", 4, 90, 1000);
    static final MachineLayout BLAST_FURNACE = new MachineLayout("blast_furnace", 4, 90, 3000);
    static final MachineLayout GRINDING = new MachineLayout("emi/grinding", 8, 76, 0);
    static final int WIDTH = 168;

    void background(WidgetHolder widgets) {
        ResourceLocation texture = machine.startsWith("emi/")
                ? PoptartCore.location("textures/gui/" + machine + ".png")
                : PoptartCore.location("textures/gui/" + machine + "/" + machine + ".png");
        widgets.addTexture(texture, 0, 0, WIDTH, height, 4, cropY, WIDTH, height, 256, 256);
    }

    SlotWidget slot(WidgetHolder widgets, EmiIngredient ingredient, int menuX, int menuY) {
        return widgets.addSlot(ingredient, menuX - 5, menuY - 1 - cropY).drawBack(false);
    }

    SlotWidget tank(WidgetHolder widgets, EmiIngredient ingredient) {
        SlotWidget tank = widgets.add(new FlaskTankWidget(ingredient, 97, 25 - cropY, capacity))
                .drawBack(false);
        sprite(widgets, "tank", 101, 53, 24, 3);
        return tank;
    }

    void arrow(WidgetHolder widgets) {
        sprite(widgets, "burn_progress", 73, 23, 24, 16);
    }

    void flame(WidgetHolder widgets) {
        sprite(widgets, "lit_progress", 36, machine.equals("blast_furnace") ? 53 : 45, 14, 14);
    }

    void casting(WidgetHolder widgets) {
        sprite(widgets, "casting_progress", 141, 22, 16, 16);
    }

    private void sprite(WidgetHolder widgets, String name, int x, int y, int width, int height) {
        widgets.addTexture(
                PoptartCore.location("textures/gui/sprites/" + machine + "/" + name + ".png"),
                x - 4,
                y - cropY,
                width,
                height,
                0,
                0,
                width,
                height,
                width,
                height);
    }
}
