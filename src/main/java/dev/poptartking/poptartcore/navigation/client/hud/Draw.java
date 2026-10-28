package dev.poptartking.poptartcore.navigation.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class Draw {
    public static final int WHITE = -1;

    private Draw() {}

    public static int withAlpha(int rgb, float alpha) {
        return Mth.clamp((int) (alpha * 255.0F), 0, 255) << 24 | rgb & 16777215;
    }

    public static float easeOutCubic(float progress) {
        float remaining = 1.0F - progress;
        return 1.0F - remaining * remaining * remaining;
    }

    public static void sprite(
            GuiGraphics graphics,
            ResourceLocation sprite,
            int x,
            int y,
            int width,
            int height,
            float red,
            float green,
            float blue,
            float alpha) {
        RenderSystem.enableBlend();
        graphics.setColor(red, green, blue, alpha);
        graphics.blitSprite(sprite, x, y, width, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void sprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        sprite(graphics, sprite, x, y, width, height, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void fill(GuiGraphics graphics, int left, int top, int right, int bottom, int argb) {
        graphics.fill(left, top, right, bottom, argb);
    }

    public static void text(GuiGraphics graphics, Font font, Component text, int x, int y, int argb, boolean shadow) {
        if (argb >>> 24 >= 4) {
            graphics.drawString(font, text, x, y, argb, shadow);
        }
    }

    public static void centeredText(GuiGraphics graphics, Font font, Component text, int centerX, int y, int argb) {
        if (argb >>> 24 >= 4) {
            graphics.drawCenteredString(font, text, centerX, y, argb);
        }
    }
}
