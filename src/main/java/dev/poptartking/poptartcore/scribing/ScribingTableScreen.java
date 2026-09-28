package dev.poptartking.poptartcore.scribing;

import dev.poptartking.poptartcore.PoptartCore;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class ScribingTableScreen extends AbstractContainerScreen<ScribingTableMenu> {
    private static final ResourceLocation BACKGROUND = PoptartCore.location("textures/gui/scribing_table.png");
    private static final int TEXT_X = 46;
    private static final int TEXT_W = 85;
    private static final int NAME_Y = 54;
    private static final int LORE_Y = 70;
    private static final int LINE_H = 9;
    private static final Style LORE_STYLE =
            Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(true);
    private static final int HINT_COLOUR = -11184811;
    private static final Component NAME_HINT = Component.translatable("menu.hint.poptartcore.item_name");
    private static final Component LORE_HINT = Component.translatable("menu.hint.poptartcore.item_description");
    private EditBox nameField;
    private Style nameStyle = Style.EMPTY;
    private WrappingTextArea loreArea;
    private ItemStack lastInput = ItemStack.EMPTY;
    private boolean suppressSend;
    private boolean editName;
    private boolean editLore;

    public ScribingTableScreen(ScribingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 221;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelY = 5;
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.nameField = new EditBox(this.font, this.leftPos + 46, this.topPos + 54, 85, 9, CommonComponents.EMPTY);
        this.nameField.setBordered(false);
        this.nameField.setCanLoseFocus(true);
        this.nameField.setFormatter((text, index) -> FormattedCharSequence.forward(text, this.nameStyle));
        this.nameField.setMaxLength(50);
        this.nameField.setValue(this.menu.getScribedName());
        this.nameField.setResponder(text -> {
            this.editName = true;
            this.send();
        });
        this.addWidget(this.nameField);
        this.loreArea = new WrappingTextArea(this.font, this.leftPos + 46, this.topPos + 70, 85, 9, 5, 256);
        this.loreArea.setStyle(LORE_STYLE);
        this.loreArea.setValue(this.menu.getScribedLore());
        this.loreArea.setResponder(text -> {
            this.editLore = true;
            this.send();
        });
        this.addWidget(this.loreArea);
        this.lastInput = this.menu.getSlot(0).getItem().copy();
    }

    private void refreshNameStyle() {
        ItemStack input = this.menu.getSlot(0).getItem();
        ChatFormatting colour =
                input.isEmpty() ? ChatFormatting.WHITE : input.getRarity().color();
        this.nameStyle = Style.EMPTY.withColor(colour);
        if (colour.getColor() != null) {
            this.nameField.setTextColor(colour.getColor());
            this.nameField.setTextColorUneditable(colour.getColor());
        }
    }

    private String loreText() {
        return String.join("\n", this.loreArea.getLoreLines());
    }

    private void send() {
        if (!this.suppressSend && this.nameField != null && this.loreArea != null) {
            PacketDistributor.sendToServer(new ScribingTextPayload(
                    this.menu.containerId,
                    this.menu.getSlot(0).getItem().copy(),
                    this.nameField.getValue(),
                    this.loreText(),
                    this.editName,
                    this.editLore));
        }
    }

    private void adoptFrom(ItemStack stack) {
        this.suppressSend = true;
        this.nameField.setValue(stack.isEmpty() ? "" : stack.getHoverName().getString());
        ItemLore lore = stack.isEmpty() ? null : stack.get(DataComponents.LORE);
        List<String> gathered = new ArrayList<>();
        if (lore != null) {
            for (Component line : lore.lines()) {
                gathered.add(line.getString());
            }
        }

        this.loreArea.setValue(String.join("\n", gathered));
        this.editName = false;
        this.editLore = false;
        this.suppressSend = false;
        this.send();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        ItemStack input = this.menu.getSlot(0).getItem();
        boolean had = !this.lastInput.isEmpty();
        if (!ItemStack.isSameItemSameComponents(input, this.lastInput)) {
            this.lastInput = input.copy();
            this.adoptFrom(input);
            if (!input.isEmpty() && !had) {
                this.setFocused(this.nameField);
                this.nameField.setFocused(true);
            }
        }

        if (input.isEmpty()) {
            this.nameField.setFocused(false);
            this.loreArea.setFocused(false);
            if (this.getFocused() == this.nameField || this.getFocused() == this.loreArea) {
                this.setFocused(null);
            }
        }

        this.nameField.setEditable(!input.isEmpty());
        this.loreArea.active = !input.isEmpty();
        this.refreshNameStyle();
    }

    private boolean hasTools() {
        return !this.menu.getSlot(1).getItem().isEmpty();
    }

    private boolean hasInput() {
        return !this.menu.getSlot(0).getItem().isEmpty();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.hasInput()
                        || !this.nameField.isMouseOver(mouseX, mouseY) && !this.loreArea.isMouseOver(mouseX, mouseY)
                ? super.mouseClicked(mouseX, mouseY, button)
                : false;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        if (this.hasTools()) {
            if (this.nameField != null && this.nameField.getValue().isBlank()) {
                guiGraphics.drawString(this.font, NAME_HINT, 46, 54, -11184811, false);
            }

            if (this.loreArea != null && this.loreArea.getValue().isBlank()) {
                guiGraphics.drawString(this.font, LORE_HINT, 46, 70, -11184811, false);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.nameField.render(guiGraphics, mouseX, mouseY, partialTick);
        this.loreArea.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.minecraft.player.closeContainer();
            return true;
        } else if (this.getFocused() == this.loreArea && this.loreArea.isFocused()) {
            return this.loreArea.keyPressed(keyCode, scanCode, modifiers) || keyCode != 258;
        } else {
            return this.getFocused() == this.nameField && this.nameField.isFocused()
                    ? this.nameField.keyPressed(keyCode, scanCode, modifiers) || this.nameField.canConsumeInput()
                    : super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public void resize(Minecraft client, int width, int height) {
        String name = this.nameField.getValue();
        String lore = this.loreText();
        boolean nameWasEdited = this.editName;
        boolean loreWasEdited = this.editLore;
        this.init(client, width, height);
        this.suppressSend = true;
        this.nameField.setValue(name);
        this.loreArea.setValue(lore);
        this.editName = nameWasEdited;
        this.editLore = loreWasEdited;
        this.suppressSend = false;
    }
}
