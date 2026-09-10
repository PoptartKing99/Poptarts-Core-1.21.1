package dev.poptartking.poptartcore.blastfurnace.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.blastfurnace.BlastFurnaceBlockEntity;
import dev.poptartking.poptartcore.blastfurnace.menu.BlastFurnaceMenu;
import dev.poptartking.poptartcore.client.FluidTankRenderer;
import dev.poptartking.poptartcore.crucible.casting.CastingRecipe;
import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import dev.poptartking.poptartcore.registry.PoptartCoreRecipes;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;

public class BlastFurnaceScreen extends AbstractContainerScreen<BlastFurnaceMenu> {
    private static final ResourceLocation TEXTURE =
            PoptartCore.location("textures/gui/blast_furnace/blast_furnace.png");
    private static final ResourceLocation LIT = PoptartCore.location("blast_furnace/lit_progress");
    private static final ResourceLocation BURN = PoptartCore.location("blast_furnace/burn_progress");
    private static final ResourceLocation TANK = PoptartCore.location("blast_furnace/tank");
    private static final ResourceLocation CASTING = PoptartCore.location("blast_furnace/casting_progress");

    public BlastFurnaceScreen(BlastFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 190;
        titleLabelY = 4;
        inventoryLabelY = 96;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        gui.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        if (menu.getFluidAmount() > 0) {
            FluidStack fluid = new FluidStack(menu.getFluid(), menu.getFluidAmount());
            FluidTankRenderer.render(gui, fluid, x + 101, y + 25, BlastFurnaceBlockEntity.TANK_CAPACITY, 1);
            gui.blitSprite(TANK, 24, 3, 0, 0, x + 101, y + 53, 24, 3);
            renderCastingDisplay(gui, fluid, x, y);
        }
        if (menu.isBurning()) {
            int height = Mth.ceil(menu.getLitProgress() * 13) + 1;
            gui.blitSprite(LIT, 14, 14, 0, 14 - height, x + 36, y + 67 - height, 14, height);
        }
        int width = Mth.ceil(menu.getCookProgress() * 24);
        gui.blitSprite(BURN, 24, 16, 0, 0, x + 73, y + 23, width, 16);
        int castHeight = Mth.ceil(menu.getCastingProgress() * 16);
        if (castHeight > 0)
            gui.blitSprite(CASTING, 16, 16, 0, 16 - castHeight, x + 141, y + 38 - castHeight, 16, castHeight);
    }

    private void renderCastingDisplay(GuiGraphics gui, FluidStack fluid, int x, int y) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        ItemStack insertedMould = menu.getMould();
        ItemStack mould =
                insertedMould.isEmpty() ? PoptartCoreItems.INGOT_MOULD.get().getDefaultInstance() : insertedMould;
        Optional<CastingRecipe> castingRecipe =
                minecraft
                        .level
                        .getRecipeManager()
                        .getAllRecipesFor(PoptartCoreRecipes.CRUCIBLE_CASTING_TYPE.get())
                        .stream()
                        .map(RecipeHolder::value)
                        .filter(recipe -> recipe.ingredient().test(mould))
                        .filter(recipe -> FluidStack.isSameFluid(fluid, recipe.fluid()))
                        .findFirst();
        if (castingRecipe.isEmpty()) return;
        CastingRecipe recipe = castingRecipe.get();
        int count = Math.min(BlastFurnaceBlockEntity.TANK_CAPACITY, fluid.getAmount())
                / recipe.fluid().getAmount();
        if (count <= 0) return;
        ItemStack display = recipe.getResultItem(minecraft.level.registryAccess());
        if (display.isEmpty()) return;
        display.setCount(count);
        gui.renderItem(display, x + 105, y + 32);
        gui.renderItemDecorations(minecraft.font, display, x + 105, y + 32);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        renderTooltip(gui, mouseX, mouseY);
    }
}
