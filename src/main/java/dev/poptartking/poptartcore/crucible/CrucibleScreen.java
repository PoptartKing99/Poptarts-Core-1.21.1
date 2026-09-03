package dev.poptartking.poptartcore.crucible;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.crucible.menu.CrucibleMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import dev.poptartking.poptartcore.client.FluidTankRenderer;
import net.neoforged.neoforge.fluids.FluidStack;
import dev.poptartking.poptartcore.crucible.casting.CastingRecipe;
import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import dev.poptartking.poptartcore.registry.PoptartCoreRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Optional;

public class CrucibleScreen extends AbstractContainerScreen<CrucibleMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    PoptartCore.MOD_ID,
                    "textures/gui/crucible.png"
            );

    private static final ResourceLocation LIT_PROGRESS_SPRITE =
            ResourceLocation.fromNamespaceAndPath(
                    PoptartCore.MOD_ID,
                    "crucible_lit_progress"
            );

    private static final ResourceLocation COOK_PROGRESS_SPRITE =
            ResourceLocation.fromNamespaceAndPath(
                    PoptartCore.MOD_ID,
                    "crucible_burn_progress"
            );

    private static final ResourceLocation TANK_SPRITE =
            ResourceLocation.fromNamespaceAndPath(
                    PoptartCore.MOD_ID,
                    "crucible_tank"
            );

    public CrucibleScreen(
            CrucibleMenu menu,
            Inventory playerInventory,
            Component title
    ) {
        super(menu, playerInventory, title);

        this.imageWidth = 176;
        this.imageHeight = 190;
        this.titleLabelY -= 20;
    }

    private void renderFluid(
            GuiGraphics guiGraphics,
            int x,
            int y
    ) {
        int fluidAmount = menu.getFluidAmount();

        if (fluidAmount <= 0) {
            return;
        }

        FluidStack stack =
                new FluidStack(
                        menu.getFluid(),
                        fluidAmount
                );

        FluidTankRenderer.render(
                guiGraphics,
                stack,
                x,
                y,
                CrucibleBlockEntity.TANK_CAPACITY,
                1.0F
        );
    }

    private void renderCastingDisplay(
            GuiGraphics guiGraphics,
            int x,
            int y
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null
                || menu.getFluidAmount() <= 0) {
            return;
        }

        FluidStack fluid =
                new FluidStack(
                        menu.getFluid(),
                        menu.getFluidAmount()
                );

        ItemStack ingotMould =
                PoptartCoreItems.INGOT_MOULD.get()
                        .getDefaultInstance();

        Optional<CastingRecipe> castingRecipe =
                minecraft.level
                        .getRecipeManager()
                        .getAllRecipesFor(
                                PoptartCoreRecipes.CRUCIBLE_CASTING_TYPE.get()
                        )
                        .stream()
                        .map(RecipeHolder::value)
                        .filter(recipe ->
                                recipe.ingredient().test(ingotMould)
                        )
                        .filter(recipe ->
                                FluidStack.isSameFluid(
                                        fluid,
                                        recipe.fluid()
                                )
                        )
                        .findFirst();

        if (castingRecipe.isEmpty()) {
            return;
        }

        CastingRecipe recipe =
                castingRecipe.get();

        int pourCount =
                Math.min(
                        CrucibleBlockEntity.TANK_CAPACITY,
                        menu.getFluidAmount()
                ) / recipe.fluid().getAmount();

        if (pourCount <= 0) {
            return;
        }

        ItemStack display =
                recipe.getResultItem(
                        minecraft.level.registryAccess()
                );

        if (display.isEmpty()) {
            return;
        }

        display.setCount(pourCount);

        guiGraphics.renderItem(
                display,
                x + 105,
                y + 32
        );

        guiGraphics.renderItemDecorations(
                minecraft.font,
                display,
                x + 105,
                y + 32
        );
    }

    @Override
    protected void init() {
        super.init();

        this.titleLabelX =
                (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(
            GuiGraphics guiGraphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        int x = leftPos;
        int y = topPos - 24;

        guiGraphics.blit(
                TEXTURE,
                x,
                y,
                0,
                0,
                imageWidth,
                imageHeight
        );

        renderFluid(
                guiGraphics,
                x + 101,
                y + 25
        );

        if (menu.getFluidAmount() > 0) {
            guiGraphics.blitSprite(
                    TANK_SPRITE,
                    24,
                    3,
                    0,
                    0,
                    x + 101,
                    y + 53,
                    24,
                    3
            );
        }

        renderCastingDisplay(
                guiGraphics,
                x,
                y
        );

        if (menu.isBurning()) {
            int flameHeight =
                    Mth.ceil(menu.getLitProgress() * 13.0F) + 1;

            guiGraphics.blitSprite(
                    LIT_PROGRESS_SPRITE,
                    14,
                    14,
                    0,
                    14 - flameHeight,
                    x + 36,
                    (y + 45 + 14) - flameHeight,
                    14,
                    flameHeight
            );
        }

        int progressWidth =
                Mth.ceil(menu.getCookProgress() * 24.0F);

        guiGraphics.blitSprite(
                COOK_PROGRESS_SPRITE,
                24,
                16,
                0,
                0,
                x + 73,
                y + 24,
                progressWidth,
                16
        );
    }

    @Override
    public void render(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.render(
                guiGraphics,
                mouseX,
                mouseY,
                partialTick
        );

        renderTooltip(
                guiGraphics,
                mouseX,
                mouseY
        );
    }
}