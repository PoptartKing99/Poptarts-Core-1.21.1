package dev.poptartking.poptartcore.anvil;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AnvilScreen extends ItemCombinerScreen<AnvilMenu> {
    private static final ResourceLocation TEXTBOX = PoptartCore.location("anvil_textbox");
    private static final ResourceLocation ANVIL_LOCATION = PoptartCore.location("textures/gui/anvil.png");
    private static final int RESULT_SLOT = 3;
    private static final long WARNING_DURATION = 1000L;
    private static final int NORMAL_COLOUR = 14737632;
    private static final int WARNING_COLOUR = 16736352;
    private long warningUntil;

    public AnvilScreen(AnvilMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, ANVIL_LOCATION);
        this.titleLabelY = 4;
    }

    protected void init() {
        super.init();
        this.titleLabelX = 90 - this.font.width(this.title) / 2;
    }

    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (slotId == 3 && slot != null && slot.hasItem() && !((AnvilMenu) this.menu).hasWorkingHammer()) {
            this.warningUntil = Util.getMillis() + 1000L;
        }

        super.slotClicked(slot, slotId, mouseButton, type);
    }

    protected void renderErrorIcon(GuiGraphics guiGraphics, int x, int y) {
        if (Util.getMillis() < this.warningUntil) {
            this.drawTextBox(guiGraphics, Component.translatable("menu.message.poptartcore.needs_hammer"), 16736352);
        } else if (((AnvilMenu) this.menu).getSlot(0).hasItem()) {
            ItemStack stack = ((AnvilMenu) this.menu).getSlot(0).getItem();
            String message = "not_upgradeable";
            String count = "";
            if (stack.is(PoptartCoreTags.UPGRADEABLE)) {
                if (AnvilMenu.getUpgradeCount(stack) < 3) {
                    message = "upgrade_cost";
                    count = String.valueOf(AnvilMenu.getUpgradeCount(stack) + 4);
                } else {
                    message = "upgraded";
                }
            }

            this.drawTextBox(
                    guiGraphics,
                    Component.translatable("menu.message.poptartcore." + message)
                            .append(count),
                    14737632);
        }
    }

    private void drawTextBox(GuiGraphics guiGraphics, Component component, int colour) {
        guiGraphics.blitSprite(TEXTBOX, this.leftPos + 51, this.topPos + 23, 100, 11);
        guiGraphics.drawString(
                this.font, component, this.leftPos + 101 - this.font.width(component) / 2, this.topPos + 25, colour);
    }

    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
    }
}
