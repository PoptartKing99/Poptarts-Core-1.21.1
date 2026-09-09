package dev.poptartking.poptartcore.workbench;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.workbench.menu.WorkbenchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WorkbenchScreen extends AbstractContainerScreen<WorkbenchMenu> {
    private static final ResourceLocation TEXTURE = PoptartCore.location("textures/gui/workbench.png");

    public WorkbenchScreen(WorkbenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 221;
        titleLabelY = 6;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        gui.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
