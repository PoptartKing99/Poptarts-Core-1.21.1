package dev.poptartking.poptartcore.integration.emi;

import com.mojang.logging.LogUtils;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import dev.poptartking.poptartcore.registry.PoptartCoreRecipes;
import dev.poptartking.poptartcore.workbench.menu.WorkbenchMenu;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in client test, never included in the distributable JAR. */
@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class EmiSmokeTest {
    private static boolean finished;
    private static int ticks;
    private static int previewTicks;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (finished) {
            if (++previewTicks == 15) {
                Screenshot.grab(
                        minecraft.gameDirectory, "emi-layouts.png", minecraft.getMainRenderTarget(), message -> {});
                minecraft.stop();
            }
            return;
        }
        if (minecraft.level == null || minecraft.player == null) return;
        ticks++;
        var manager = EmiApi.getRecipeManager();
        List<EmiRecipe> recipes = manager.getRecipes().stream()
                .filter(recipe -> recipe instanceof ProcessingEmiRecipe)
                .toList();
        if (recipes.isEmpty() && ticks < 1200) return;
        finished = true;
        try {
            require(!recipes.isEmpty(), "EMI plugin did not register recipes");
            Set<ResourceLocation> ids = new HashSet<>();
            Set<String> categories = new HashSet<>();
            for (EmiRecipe recipe : recipes) {
                require(ids.add(recipe.getId()), "Duplicate display ID: " + recipe.getId());
                categories.add(recipe.getCategory().getId().getPath());
                require(!recipe.getInputs().isEmpty() && !recipe.getOutputs().isEmpty(), "Empty recipe");
                require(
                        recipe.getInputs().stream().noneMatch(input -> input.isEmpty()),
                        "Empty ingredient: " + recipe.getId());
                require(
                        recipe.getOutputs().stream().noneMatch(output -> output.isEmpty()),
                        "Empty output: " + recipe.getId());
                recipe.addWidgets(new WidgetHolder() {
                    public int getWidth() {
                        return recipe.getDisplayWidth();
                    }

                    public int getHeight() {
                        return recipe.getDisplayHeight();
                    }

                    public <T extends Widget> T add(T widget) {
                        var bounds = widget.getBounds();
                        require(
                                bounds.left() >= 0
                                        && bounds.top() >= 0
                                        && bounds.right() <= getWidth()
                                        && bounds.bottom() <= getHeight(),
                                "Widget outside display: " + recipe.getId() + " " + bounds);
                        return widget;
                    }
                });
                for (EmiStack output : recipe.getOutputs()) {
                    require(
                            manager.getRecipesByOutput(output).contains(recipe),
                            "Output not indexed: " + recipe.getId());
                }
            }
            Set<String> expectedCategories = new HashSet<>(Set.of(
                    "crucible_melting",
                    "blast_furnace_melting",
                    "crucible_alloying",
                    "blast_furnace_alloying",
                    "casting",
                    "grinding",
                    "milling"));
            if (minecraft
                    .level
                    .getRecipeManager()
                    .getAllRecipesFor(PoptartCoreRecipes.BLAST_FURNACE_MELTING_TYPE.get())
                    .isEmpty()) {
                expectedCategories.remove("blast_furnace_melting");
            }
            require(categories.equals(expectedCategories), "Missing category: " + categories);
            var data = minecraft.level.getRecipeManager();
            int expected = data.getAllRecipesFor(PoptartCoreRecipes.CRUCIBLE_MELTING_TYPE.get())
                            .size()
                    + data.getAllRecipesFor(PoptartCoreRecipes.BLAST_FURNACE_MELTING_TYPE.get())
                            .size()
                    + data.getAllRecipesFor(PoptartCoreRecipes.CRUCIBLE_ALLOYING_TYPE.get())
                            .size()
                    + data.getAllRecipesFor(PoptartCoreRecipes.BLAST_FURNACE_ALLOYING_TYPE.get())
                            .size()
                    + data.getAllRecipesFor(PoptartCoreRecipes.CRUCIBLE_CASTING_TYPE.get())
                            .size()
                    + data.getAllRecipesFor(PoptartCoreRecipes.GRINDING_TYPE.get())
                            .size()
                    + data.getAllRecipesFor(PoptartCoreRecipes.MILLING_TYPE.get())
                            .size();
            require(recipes.size() == expected, "Missing recipes: " + recipes.size() + " expected " + expected);
            for (String shared : List.of("crucible_melting", "crucible_alloying")) {
                var category = recipes.stream()
                        .map(EmiRecipe::getCategory)
                        .filter(value -> value.getId().getPath().equals(shared))
                        .findFirst()
                        .orElseThrow();
                require(
                        manager.getWorkstations(category).size() == 2,
                        "Shared recipe lost its blast furnace workstation");
            }
            require(
                    manager.getWorkstations(VanillaEmiRecipeCategories.CRAFTING).stream()
                            .anyMatch(stack ->
                                    stack.getEmiStacks().contains(EmiStack.of(PoptartCoreBlocks.WORKBENCH.get()))),
                    "Workbench not registered");
            WorkbenchMenu menu = new WorkbenchMenu(0, minecraft.player.getInventory());
            WorkbenchEmiRecipeHandler handler = new WorkbenchEmiRecipeHandler();
            require(handler.getCraftingSlots(menu).size() == 9, "Wrong crafting slot count");
            require(handler.getInputSources(menu).size() == 61, "Wrong source slot count");
            require(
                    !handler.getInputSources(menu).contains(handler.getOutputSlot(menu)),
                    "Result included as an input");
            LogUtils.getLogger()
                    .info(
                            "EMI_SMOKE_PASS: {} recipes, {} populated categories, widget bounds, output indexing and workbench slots",
                            recipes.size(),
                            categories.size());
            minecraft.setScreen(new PreviewScreen(recipes));
        } catch (Throwable error) {
            LogUtils.getLogger().error("EMI_SMOKE_FAIL", error);
            minecraft.stop();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class PreviewScreen extends Screen {
        private final List<EmiRecipe> recipes;

        PreviewScreen(List<EmiRecipe> all) {
            super(Component.literal("EMI layout verification"));
            Set<String> seen = new HashSet<>();
            recipes = all.stream()
                    .filter(recipe -> seen.add(recipe.getCategory().getId().getPath()))
                    .toList();
        }

        @Override
        public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
            gui.fill(0, 0, width, height, 0xFF202020);
            float scale = Math.min(width / 354f, height / 450f);
            gui.pose().pushPose();
            gui.pose().scale(scale, scale, 1);
            for (int index = 0; index < recipes.size(); index++) {
                EmiRecipe recipe = recipes.get(index);
                int x = 5 + index % 2 * 176;
                int y = 4 + index / 2 * 112;
                gui.drawString(font, recipe.getCategory().getName(), x, y, 0xFFFFFF);
                List<Widget> widgets = new ArrayList<>();
                recipe.addWidgets(new WidgetHolder() {
                    public int getWidth() {
                        return recipe.getDisplayWidth();
                    }

                    public int getHeight() {
                        return recipe.getDisplayHeight();
                    }

                    public <T extends Widget> T add(T widget) {
                        widgets.add(widget);
                        return widget;
                    }
                });
                gui.pose().pushPose();
                gui.pose().translate(x, y + 12, 0);
                for (Widget widget : widgets) widget.render(gui, -100, -100, partialTick);
                gui.pose().popPose();
            }
            gui.pose().popPose();
        }
    }
}
