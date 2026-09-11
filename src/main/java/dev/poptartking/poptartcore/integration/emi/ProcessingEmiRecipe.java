package dev.poptartking.poptartcore.integration.emi;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

/** Wayfarer-style machine displays with standard item and fluid tooltips. */
final class ProcessingEmiRecipe extends BasicEmiRecipe {
    private final MachineLayout layout;
    private final boolean recipeTree;
    private final String process;

    ProcessingEmiRecipe(
            EmiRecipeCategory category,
            ResourceLocation id,
            List<EmiIngredient> inputs,
            List<EmiStack> outputs,
            List<EmiIngredient> catalysts,
            boolean recipeTree) {
        super(category, id, MachineLayout.WIDTH, 90);
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.catalysts = List.copyOf(catalysts);
        this.recipeTree = recipeTree;
        process = category.getId().getPath();
        layout = process.startsWith("blast_furnace")
                ? MachineLayout.BLAST_FURNACE
                : process.startsWith("crucible") || process.equals("casting")
                        ? MachineLayout.CRUCIBLE
                        : MachineLayout.GRINDING;
        height = layout.height();
    }

    @Override
    public boolean supportsRecipeTree() {
        return recipeTree;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        layout.background(widgets);
        if (process.equals("casting")) {
            layout.tank(widgets, inputs.getFirst());
            layout.slot(widgets, inputs.get(1), 105, 61);
            layout.slot(widgets, outputs.getFirst(), 141, 44).recipeContext(this);
            layout.casting(widgets);
        } else if (layout != MachineLayout.GRINDING) {
            boolean blast = layout == MachineLayout.BLAST_FURNACE;
            for (int index = 0; index < inputs.size(); index++) {
                layout.slot(widgets, inputs.get(index), 17 + index % 3 * 18, (blast ? 15 : 25) + index / 3 * 18);
            }
            for (EmiStack output : outputs) {
                if (output.getKeyOfType(Fluid.class) != null)
                    layout.tank(widgets, output).recipeContext(this);
                else layout.slot(widgets, output, 141, 44).recipeContext(this);
            }
            layout.arrow(widgets);
            layout.flame(widgets);
        } else {
            addGrindingWidgets(widgets);
        }
    }

    private void addGrindingWidgets(WidgetHolder widgets) {
        // Wayfarer uses its mortar panel for grinding, including the powered millstone.
        layout.slot(widgets, inputs.getFirst(), 53, 54);
        layout.slot(widgets, outputs.getFirst(), 114, 54).recipeContext(this);
        if (outputs.size() > 1) widgets.addSlot(outputs.get(1), 137, 45).recipeContext(this);
        EmiIngredient tool = EmiStack.of(
                process.equals("milling") ? PoptartCoreBlocks.MILLSTONE.get() : PoptartCoreBlocks.QUERN.get());
        layout.slot(widgets, tool, 80, 21).catalyst(true);
        widgets.addTexture(
                PoptartCore.location("textures/gui/emi/grinding_progress.png"), 72, 45, 24, 16, 0, 0, 24, 16, 24, 16);
    }
}
