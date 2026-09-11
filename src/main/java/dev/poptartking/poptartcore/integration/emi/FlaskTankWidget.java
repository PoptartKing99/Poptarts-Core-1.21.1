package dev.poptartking.poptartcore.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TankWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Vector3f;

/** Wayfarer's shaped tank, with clipping transformed into EMI's screen coordinates. */
final class FlaskTankWidget extends TankWidget {
    private final EmiIngredient ingredient;
    private final int capacity;

    FlaskTankWidget(EmiIngredient ingredient, int x, int y, int capacity) {
        super(ingredient, x, y, 24, 31, capacity);
        this.ingredient = ingredient;
        this.capacity = capacity;
    }

    @Override
    public void drawStack(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (ingredient.getEmiStacks().isEmpty()) return;
        EmiStack stack = ingredient.getEmiStacks().getFirst();
        Fluid fluid = stack.getKeyOfType(Fluid.class);
        if (fluid == null || stack.getAmount() <= 0) return;
        FluidStack rendered = new FluidStack(fluid, (int) Math.min(Integer.MAX_VALUE, stack.getAmount()));
        rendered.applyComponents(stack.getComponentChanges());
        var extension = IClientFluidTypeExtensions.of(fluid);
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(extension.getStillTexture(rendered));
        int tint = extension.getTintColor(rendered);
        float fill = Mth.clamp((float) rendered.getAmount() / capacity, 0, 1);
        int x = getBounds().x(), y = getBounds().y();
        int bottom = Mth.ceil(Math.min(1, fill / 0.5f) * 16);
        if (bottom > 0) drawPart(graphics, sprite, tint, x, y + 12, y + 28 - bottom, y + 30, 18);
        if (fill > 0.5f) {
            int top = Mth.ceil((fill - 0.5f) / 0.5f * 12);
            drawPart(graphics, sprite, tint, x, y, y + 12 - top, y + 12, 16);
        }
    }

    private static void drawPart(
            GuiGraphics gui,
            TextureAtlasSprite sprite,
            int tint,
            int x,
            int textureY,
            int clipTop,
            int clipBottom,
            int textureHeight) {
        // GuiGraphics' scissor API takes absolute GUI coordinates, not the current pose.
        var matrix = gui.pose().last().pose();
        Vector3f start = matrix.transformPosition(new Vector3f(x, clipTop, 0));
        Vector3f end = matrix.transformPosition(new Vector3f(x + 24, clipBottom, 0));
        gui.enableScissor(Mth.floor(start.x), Mth.floor(start.y), Mth.ceil(end.x), Mth.ceil(end.y));
        float alpha = (tint >>> 24 & 255) / 255f;
        float red = (tint >>> 16 & 255) / 255f;
        float green = (tint >>> 8 & 255) / 255f;
        float blue = (tint & 255) / 255f;
        gui.blit(x, textureY, 0, 16, textureHeight, sprite, red, green, blue, alpha);
        gui.blit(x + 16, textureY, 0, 16, textureHeight, sprite, red, green, blue, alpha);
        gui.disableScissor();
    }
}
