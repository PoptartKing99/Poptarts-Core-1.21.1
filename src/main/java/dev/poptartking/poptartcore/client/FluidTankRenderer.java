package dev.poptartking.poptartcore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidTankRenderer {

    public static void render(
            GuiGraphics gui,
            FluidStack stack,
            int x,
            int y,
            float tankCapacity,
            float alphaModifier
    ) {
        IClientFluidTypeExtensions fluidTypeExtensions =
                IClientFluidTypeExtensions.of(stack.getFluid());

        ResourceLocation stillTexture =
                fluidTypeExtensions.getStillTexture(stack);

        TextureAtlasSprite sprite =
                Minecraft.getInstance()
                        .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                        .apply(stillTexture);

        int tintColor =
                fluidTypeExtensions.getTintColor(stack);

        float alpha =
                ((tintColor >> 24) & 255) / 255.0F
                        * alphaModifier;

        float red =
                ((tintColor >> 16) & 255) / 255.0F;

        float green =
                ((tintColor >> 8) & 255) / 255.0F;

        float blue =
                (tintColor & 255) / 255.0F;

        float capacity =
                Mth.clamp(
                        stack.getAmount() / tankCapacity,
                        0.0F,
                        1.0F
                );

        int bottomHeight =
                Mth.ceil(
                        Math.min(1.0F, capacity / 0.5F)
                                * 16.0F
                );

        if (bottomHeight > 0) {
            int bottomY =
                    y + 28 - bottomHeight;

            gui.enableScissor(
                    x,
                    bottomY,
                    x + 16,
                    y + 30
            );

            gui.blit(
                    x,
                    y + 12,
                    0,
                    16,
                    18,
                    sprite,
                    red,
                    green,
                    blue,
                    alpha
            );

            gui.disableScissor();

            gui.enableScissor(
                    x + 16,
                    bottomY,
                    x + 24,
                    y + 30
            );

            gui.blit(
                    x + 16,
                    y + 12,
                    0,
                    16,
                    18,
                    sprite,
                    red,
                    green,
                    blue,
                    alpha
            );

            gui.disableScissor();
        }

        if (capacity > 0.5F) {
            float topCapacity =
                    (capacity - 0.5F) / 0.5F;

            int topHeight =
                    Mth.ceil(topCapacity * 12.0F);

            int topY =
                    y + 12 - topHeight;

            gui.enableScissor(
                    x,
                    topY,
                    x + 16,
                    y + 12
            );

            gui.blit(
                    x,
                    y,
                    0,
                    16,
                    16,
                    sprite,
                    red,
                    green,
                    blue,
                    alpha
            );

            gui.disableScissor();

            gui.enableScissor(
                    x + 16,
                    topY,
                    x + 24,
                    y + 12
            );

            gui.blit(
                    x + 16,
                    y,
                    0,
                    16,
                    16,
                    sprite,
                    red,
                    green,
                    blue,
                    alpha
            );

            gui.disableScissor();
        }
    }
}