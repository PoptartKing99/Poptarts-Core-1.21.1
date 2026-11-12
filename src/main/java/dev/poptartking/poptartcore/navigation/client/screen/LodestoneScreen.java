package dev.poptartking.poptartcore.navigation.client.screen;

import dev.poptartking.poptartcore.navigation.WaypointIcons;
import dev.poptartking.poptartcore.navigation.client.hud.Draw;
import dev.poptartking.poptartcore.navigation.network.OpenLodestonePayload;
import dev.poptartking.poptartcore.navigation.network.UpdateLodestonePayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public final class LodestoneScreen extends Screen {
    private static final Component TITLE = Component.translatable("poptartcore.lodestone.title");
    private static final Component NAME = Component.translatable("poptartcore.lodestone.name");
    private static final int ICON = 20;
    private static final int GAP = 8;
    private final BlockPos pos;
    private final List<String> icons;
    private String name;
    private String icon;
    private int titleY;
    private int gridLeft;
    private int gridTop;

    private LodestoneScreen(OpenLodestonePayload packet) {
        super(TITLE);
        this.pos = packet.pos();
        this.name = packet.name();
        this.icon = packet.icon();
        this.icons = findIcons();
    }

    public static void open(OpenLodestonePayload packet) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new LodestoneScreen(packet));
    }

    protected void init() {
        int rows = (this.icons.size() + 10 - 1) / 10;
        int height = 9 + 8 + 20 + 8 + rows * 24 + 8 + 20;
        int centerX = this.width / 2;
        this.titleY = (this.height - height) / 2;
        int fieldTop = this.titleY + 9 + 8;
        EditBox field = new EditBox(this.font, centerX - 100, fieldTop, 200, 20, NAME);
        field.setMaxLength(32);
        field.setValue(this.name);
        field.setHint(NAME);
        field.setResponder(value -> this.name = value);
        this.addRenderableWidget(field);
        this.setInitialFocus(field);
        this.gridLeft = centerX - 120;
        this.gridTop = fieldTop + 20 + 8;
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(centerX - 100, this.gridTop + rows * 24 + 8, 200, 20)
                .build());
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void removed() {
        if (this.minecraft != null && this.minecraft.getConnection() != null) {
            PacketDistributor.sendToServer(new UpdateLodestonePayload(this.pos, this.name.trim(), this.icon));
        }
    }

    private void drawIcons(GuiGraphics graphics, int mouseX, int mouseY) {
        Draw.centeredText(graphics, this.font, this.title, this.width / 2, this.titleY, -1);
        int hovered = this.getIconAt((double) mouseX, (double) mouseY);

        for (int index = 0; index < this.icons.size(); index++) {
            String id = this.icons.get(index);
            int x = this.gridLeft + index % 10 * 24;
            int y = this.gridTop + index / 10 * 24;
            boolean selected = id.equals(this.icon);
            if (selected || index == hovered) {
                Draw.fill(graphics, x, y, x + 24, y + 24, selected ? 1627389951 : 822083583);
            }

            Draw.sprite(graphics, WaypointIcons.getSprite(id, selected), x + 2, y + 2, 20, 20);
        }
    }

    private boolean clickIcon(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        } else {
            int index = this.getIconAt(mouseX, mouseY);
            if (index < 0) {
                return false;
            } else {
                this.icon = this.icons.get(index);
                net.minecraft.client.Minecraft.getInstance()
                        .getSoundManager()
                        .play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                                net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1F));
                return true;
            }
        }
    }

    private int getIconAt(double mouseX, double mouseY) {
        if (!(mouseX < (double) this.gridLeft) && !(mouseY < (double) this.gridTop)) {
            int column = (int) (mouseX - (double) this.gridLeft) / 24;
            int row = (int) (mouseY - (double) this.gridTop) / 24;
            int index = row * 10 + column;
            return column < 10 && index < this.icons.size() ? index : -1;
        } else {
            return -1;
        }
    }

    private boolean confirm(int key) {
        if (key != 257 && key != 335) {
            return false;
        } else {
            this.onClose();
            return true;
        }
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.drawIcons(graphics, mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.clickIcon(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.confirm(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static List<String> findIcons() {
        List<String> icons = new ArrayList<>();

        for (ResourceLocation id : Minecraft.getInstance()
                .getResourceManager()
                .listResources(
                        "textures/gui/sprites/waypoints", path -> path.getPath().endsWith(".png"))
                .keySet()) {
            if (id.getNamespace().equals("poptartcore")) {
                String name = id.getPath()
                        .substring(
                                "textures/gui/sprites/waypoints".length() + 1,
                                id.getPath().length() - ".png".length());
                if (!name.endsWith("_selected") && WaypointIcons.isValid(name)) {
                    icons.add(name);
                }
            }
        }

        icons.sort(null);
        return icons;
    }
}
