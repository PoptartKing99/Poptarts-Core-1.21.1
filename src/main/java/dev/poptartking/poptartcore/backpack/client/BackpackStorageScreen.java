package dev.poptartking.poptartcore.backpack.client;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class BackpackStorageScreen extends ContainerScreen {
    private static final net.minecraft.resources.ResourceLocation BACKPACK_PANEL =
            PoptartCore.location("textures/gui/backpack/backpack_attachments_panel.png");
    private static final net.minecraft.resources.ResourceLocation RUCKSACK_PANEL =
            PoptartCore.location("textures/gui/backpack/rucksack_attachments_panel.png");
    private static final net.minecraft.resources.ResourceLocation BACKGROUND =
            PoptartCore.location("textures/gui/backpack/storage.png");
    private static final net.minecraft.resources.ResourceLocation RUCKSACK_BACKGROUND =
            PoptartCore.location("textures/gui/backpack/rucksack_storage.png");

    public BackpackStorageScreen(net.minecraft.world.inventory.ChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(
                        Component.translatable("menu.poptartcore.colors"),
                        button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0))
                .bounds(leftPos + 121, topPos + 3, 48, 12)
                .build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(
                menu.getRowCount() == 6 ? RUCKSACK_BACKGROUND : BACKGROUND,
                leftPos,
                topPos,
                0,
                0,
                imageWidth,
                imageHeight,
                256,
                256);
        if (menu instanceof dev.poptartking.poptartcore.backpack.BackpackMenu backpack
                && backpack.canEditAttachments()) {
            boolean rucksack = menu.getRowCount() == 6;
            int panelTop = rucksack ? 7 : 11;
            graphics.blit(
                    rucksack ? RUCKSACK_PANEL : BACKPACK_PANEL,
                    leftPos - 35,
                    topPos + panelTop,
                    179,
                    panelTop,
                    32,
                    rucksack ? 106 : 48,
                    256,
                    256);
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        if (menu instanceof dev.poptartking.poptartcore.backpack.BackpackMenu backpack
                && backpack.canEditAttachments()) {
            int panelTop = menu.getRowCount() == 6 ? 7 : 11;
            int panelHeight = menu.getRowCount() == 6 ? 106 : 48;
            if (mouseX >= left - 35
                    && mouseX < left - 3
                    && mouseY >= top + panelTop
                    && mouseY < top + panelTop + panelHeight) return false;
        }
        return super.hasClickedOutside(mouseX, mouseY, left, top, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int mountStart = menu.getRowCount() * 9 + 36;
        if (hoveredSlot != null && hoveredSlot.index >= mountStart && !hoveredSlot.hasItem()) {
            int mount = hoveredSlot.index - mountStart;
            graphics.renderTooltip(
                    font,
                    Component.translatable(
                            mount == 0
                                    ? "menu.poptartcore.attachment_top"
                                    : mount == 1
                                            ? "menu.poptartcore.attachment_primary"
                                            : "menu.poptartcore.attachment_side"),
                    mouseX,
                    mouseY);
        }
    }
}
