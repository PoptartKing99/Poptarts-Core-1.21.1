package dev.poptartking.poptartcore.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.poptartking.poptartcore.workbench.menu.WorkbenchMenu;
import java.util.List;
import net.minecraft.world.inventory.Slot;

final class WorkbenchEmiRecipeHandler implements StandardRecipeHandler<WorkbenchMenu> {
    @Override
    public List<Slot> getInputSources(WorkbenchMenu menu) {
        // Grid, workbench storage and player inventory, but never the computed result.
        return menu.slots.subList(1, menu.slots.size());
    }

    @Override
    public List<Slot> getCraftingSlots(WorkbenchMenu menu) {
        return menu.slots.subList(1, 1 + WorkbenchMenu.GRID_SIZE);
    }

    @Override
    public Slot getOutputSlot(WorkbenchMenu menu) {
        return menu.slots.getFirst();
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree();
    }
}
