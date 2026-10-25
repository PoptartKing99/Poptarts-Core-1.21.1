package dev.poptartking.poptartcore.backpack.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.backpack.BackpackColorMenu;
import dev.poptartking.poptartcore.backpack.BackpackColors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class BackpackColorScreen extends AbstractContainerScreen<BackpackColorMenu> {
    private static final org.joml.Vector3f PREVIEW_LIGHT_LEFT = new org.joml.Vector3f(-0.8F, -0.8F, 1F).normalize();
    private static final org.joml.Vector3f PREVIEW_LIGHT_RIGHT = new org.joml.Vector3f(0.8F, -0.8F, 1F).normalize();
    private static final net.minecraft.resources.ResourceLocation BACKGROUND =
            PoptartCore.location("textures/gui/backpack/colors.png");
    private Button body, flaps, apply;
    private boolean mouseOriginSet;
    private int mouseOriginX, mouseOriginY;

    public BackpackColorScreen(BackpackColorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 180;
        imageHeight = 208;
        inventoryLabelX = 10;
        inventoryLabelY = 114;
    }

    private Button button(String key, int id, int x, int y, int width) {
        return addRenderableWidget(Button.builder(
                        Component.translatable("menu.poptartcore." + key),
                        button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id))
                .bounds(leftPos + x, topPos + y, width, 20)
                .build());
    }

    @Override
    protected void init() {
        super.init();
        mouseOriginSet = false;
        body = button("body", BackpackColorMenu.SELECT_BODY, 103, 24, 45);
        body.setHeight(16);
        flaps = button("flaps", BackpackColorMenu.SELECT_FLAPS, 103, 50, 45);
        flaps.setHeight(16);
        apply = button("apply", BackpackColorMenu.APPLY, 124, 86, 47);
        apply.setHeight(18);
        button("back", BackpackColorMenu.BACK, imageWidth - 55, 3, 48).setHeight(12);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, 250, 232);
        var preview = menu.preview();
        boolean rucksack = preview.is(dev.poptartking.poptartcore.registry.PoptartCoreBlocks.RUCKSACK
                .get()
                .asItem());
        if (!mouseOriginSet) {
            mouseOriginX = mouseX;
            mouseOriginY = mouseY;
            mouseOriginSet = true;
        }
        float yaw = net.minecraft.util.Mth.clamp((float) Math.atan((mouseX - mouseOriginX) / 80F) * 25F, -25F, 25F);
        float pitch = net.minecraft.util.Mth.clamp((float) Math.atan((mouseY - mouseOriginY) / 80F) * 15F, -15F, 15F);
        graphics.flush();
        graphics.enableScissor(leftPos + 10, topPos + 21, leftPos + 95, topPos + 109);
        graphics.pose().pushPose();
        graphics.pose().translate(leftPos + 53, topPos + 65, 150);
        float scale = rucksack ? 34F : 47.5F;
        graphics.pose().scale(scale, -scale, scale);
        // Orbit around the model's center, with yaw around its upright axis.
        // Applying the GUI item transform afterward would introduce a sideways lean.
        graphics.pose().mulPose(Axis.XP.rotationDegrees(pitch));
        graphics.pose().mulPose(Axis.YP.rotationDegrees(yaw));
        graphics.pose().translate(0, rucksack ? -0.34375 : 0.03125, -0.125);
        com.mojang.blaze3d.systems.RenderSystem.setShaderLights(PREVIEW_LIGHT_LEFT, PREVIEW_LIGHT_RIGHT);
        var renderer = minecraft.getItemRenderer();
        renderer.render(
                preview,
                net.minecraft.world.item.ItemDisplayContext.NONE,
                false,
                graphics.pose(),
                graphics.bufferSource(),
                15728880,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                renderer.getModel(preview, minecraft.level, minecraft.player, 0));
        graphics.flush();
        Lighting.setupFor3DItems();
        graphics.pose().popPose();
        graphics.disableScissor();
        for (var part : BackpackColors.Part.values()) {
            int color = BackpackColors.has(preview, part) ? BackpackColors.color(preview, part) : 0x7D4D33;
            int y = topPos + (part == BackpackColors.Part.BODY ? 27 : 53);
            graphics.fill(leftPos + 156, y, leftPos + 167, y + 11, 0xFF000000 | color);
        }
        graphics.drawString(
                font, Component.translatable("menu.poptartcore.dye"), leftPos + 103, topPos + 75, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        body.active = menu.selectedPart() != BackpackColors.Part.BODY;
        flaps.active = menu.selectedPart() != BackpackColors.Part.FLAPS;
        apply.active = menu.getSlot(0).hasItem() && !menu.current().isEmpty();
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        for (var part : BackpackColors.Part.values()) {
            int y = part == BackpackColors.Part.BODY ? 26 : 52;
            if (isHovering(155, y, 13, 13, mouseX, mouseY)) {
                var preview = menu.preview();
                int color = BackpackColors.has(preview, part) ? BackpackColors.color(preview, part) : 0x7D4D33;
                graphics.renderTooltip(
                        font,
                        Component.literal(String.format(java.util.Locale.ROOT, "#%06X", color & 0xFFFFFF)),
                        mouseX,
                        mouseY);
            }
        }
    }
}
