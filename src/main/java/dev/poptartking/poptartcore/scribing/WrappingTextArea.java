package dev.poptartking.poptartcore.scribing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WrappingTextArea extends AbstractWidget {
    private static final int SELECTION_COLOUR = -2143592705;
    private static final int PACKED_LINES = 2;
    private final Font font;
    private final int lineHeight;
    private final int maxLines;
    private final int maxLength;
    private final List<int[]> lines = new ArrayList<>();
    private String value = "";
    private int cursor;
    private int anchor;
    private int textColour = -1;
    private Style style = Style.EMPTY;
    private long focusTime = Util.getMillis();
    private Consumer<String> responder;

    public WrappingTextArea(Font font, int x, int y, int width, int lineHeight, int maxLines, int maxLength) {
        super(x, y, width, lineHeight * maxLines, CommonComponents.EMPTY);
        this.font = font;
        this.lineHeight = lineHeight;
        this.maxLines = maxLines;
        this.maxLength = maxLength;
        this.rebuild();
    }

    public void setTextColour(int colour) {
        this.textColour = colour;
    }

    public void setStyle(Style style) {
        this.style = style;
        if (style.getColor() != null) {
            this.textColour = 0xFF000000 | style.getColor().getValue();
        }
    }

    public void setResponder(Consumer<String> responder) {
        this.responder = responder;
    }

    public String getValue() {
        return this.value;
    }

    public List<String> getLoreLines() {
        List<String> out = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int soft = 0;

        for (int[] line : this.lines) {
            current.append(this.value, line[0], line[1]);
            if (line[2] == 1 || ++soft == 2) {
                out.add(current.toString());
                current.setLength(0);
                soft = 0;
            }
        }

        if (current.length() > 0) {
            out.add(current.toString());
        }

        while (!out.isEmpty() && out.get(out.size() - 1).isBlank()) {
            out.remove(out.size() - 1);
        }

        return out;
    }

    public void setValue(String text) {
        String clean = StringUtil.filterText(text, true);
        if (clean.length() > this.maxLength) {
            clean = clean.substring(0, this.maxLength);
        }

        this.value = clean;
        this.rebuild();

        while (this.lines.size() > this.maxLines && !this.value.isEmpty()) {
            this.value = this.value.substring(0, this.value.length() - 1);
            this.rebuild();
        }

        this.cursor = this.anchor = this.value.length();
        this.notifyChange();
    }

    private void notifyChange() {
        if (this.responder != null) {
            this.responder.accept(this.value);
        }
    }

    private void rebuild() {
        this.lines.clear();
        int length = this.value.length();
        int index = 0;

        while (true) {
            int hard = this.value.indexOf(10, index);
            int limit = hard < 0 ? length : hard;
            int end = index
                    + this.font
                            .plainSubstrByWidth(this.value.substring(index, limit), this.width)
                            .length();
            if (end < limit) {
                int space = this.value.lastIndexOf(32, end);
                if (space > index) {
                    end = space + 1;
                } else if (end == index) {
                    end = index + 1;
                }
            }

            this.lines.add(new int[] {index, end, 0});
            if (end >= limit) {
                if (hard < 0) {
                    break;
                }

                this.lines.get(this.lines.size() - 1)[2] = 1;
                index = hard + 1;
                if (index >= length) {
                    this.lines.add(new int[] {length, length, 0});
                    break;
                }
            } else {
                index = end;
            }
        }

        if (this.lines.isEmpty()) {
            this.lines.add(new int[] {0, 0, 0});
        }
    }

    private int lineAt(int position) {
        for (int i = 0; i < this.lines.size(); i++) {
            int[] line = this.lines.get(i);
            if (position >= line[0] && position <= line[1]) {
                return i;
            }
        }

        return this.lines.size() - 1;
    }

    private int indexAt(int lineIndex, int pixel) {
        int[] line = this.lines.get(lineIndex);
        String text = this.value.substring(line[0], line[1]);
        return line[0] + this.font.plainSubstrByWidth(text, Math.max(pixel, 0)).length();
    }

    private int pixelOf(int position) {
        int[] line = this.lines.get(this.lineAt(position));
        return this.font.width(this.value.substring(line[0], position));
    }

    private int selectionStart() {
        return Math.min(this.cursor, this.anchor);
    }

    private int selectionEnd() {
        return Math.max(this.cursor, this.anchor);
    }

    private void moveTo(int position, boolean extend) {
        this.cursor = Mth.clamp(position, 0, this.value.length());
        if (!extend) {
            this.anchor = this.cursor;
        }

        this.focusTime = Util.getMillis();
    }

    private void insert(String text) {
        String filtered = StringUtil.filterText(text, true);
        int start = this.selectionStart();
        int end = this.selectionEnd();
        String next = this.value.substring(0, start) + filtered + this.value.substring(end);
        if (next.length() <= this.maxLength) {
            String previous = this.value;
            this.value = next;
            this.rebuild();
            if (this.lines.size() > this.maxLines) {
                this.value = previous;
                this.rebuild();
            } else {
                this.cursor = this.anchor = start + filtered.length();
                this.focusTime = Util.getMillis();
                this.notifyChange();
            }
        }
    }

    private void deleteRange(int start, int end) {
        if (start < end) {
            this.value = this.value.substring(0, start) + this.value.substring(end);
            this.rebuild();
            this.cursor = this.anchor = start;
            this.focusTime = Util.getMillis();
            this.notifyChange();
        }
    }

    private int wordBoundary(int from, int direction) {
        int position = from;
        if (direction >= 0) {
            int length = this.value.length();

            while (position < length && this.value.charAt(position) == ' ') {
                position++;
            }

            while (position < length && this.value.charAt(position) != ' ' && this.value.charAt(position) != '\n') {
                position++;
            }
        } else {
            while (position > 0 && this.value.charAt(position - 1) == ' ') {
                position--;
            }

            while (position > 0 && this.value.charAt(position - 1) != ' ' && this.value.charAt(position - 1) != '\n') {
                position--;
            }
        }

        return position;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.isFocused() && StringUtil.isAllowedChatCharacter(codePoint)) {
            this.insert(Character.toString(codePoint));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isFocused()) {
            return false;
        } else if (Screen.isSelectAll(keyCode)) {
            this.anchor = 0;
            this.cursor = this.value.length();
            return true;
        } else if (Screen.isCopy(keyCode)) {
            Minecraft.getInstance()
                    .keyboardHandler
                    .setClipboard(this.value.substring(this.selectionStart(), this.selectionEnd()));
            return true;
        } else if (Screen.isPaste(keyCode)) {
            this.insert(Minecraft.getInstance().keyboardHandler.getClipboard());
            return true;
        } else if (Screen.isCut(keyCode)) {
            Minecraft.getInstance()
                    .keyboardHandler
                    .setClipboard(this.value.substring(this.selectionStart(), this.selectionEnd()));
            this.deleteRange(this.selectionStart(), this.selectionEnd());
            return true;
        } else {
            boolean extend = Screen.hasShiftDown();
            switch (keyCode) {
                case 257:
                case 335:
                    this.insert("\n");
                    return true;
                case 259:
                    if (this.selectionStart() != this.selectionEnd()) {
                        this.deleteRange(this.selectionStart(), this.selectionEnd());
                    } else if (this.cursor > 0) {
                        this.deleteRange(
                                Screen.hasControlDown() ? this.wordBoundary(this.cursor, -1) : this.cursor - 1,
                                this.cursor);
                    }

                    return true;
                case 261:
                    if (this.selectionStart() != this.selectionEnd()) {
                        this.deleteRange(this.selectionStart(), this.selectionEnd());
                    } else if (this.cursor < this.value.length()) {
                        this.deleteRange(
                                this.cursor,
                                Screen.hasControlDown() ? this.wordBoundary(this.cursor, 1) : this.cursor + 1);
                    }

                    return true;
                case 262:
                    this.moveTo(Screen.hasControlDown() ? this.wordBoundary(this.cursor, 1) : this.cursor + 1, extend);
                    return true;
                case 263:
                    this.moveTo(Screen.hasControlDown() ? this.wordBoundary(this.cursor, -1) : this.cursor - 1, extend);
                    return true;
                case 264: {
                    int line = this.lineAt(this.cursor);
                    this.moveTo(
                            line >= this.lines.size() - 1
                                    ? this.value.length()
                                    : this.indexAt(line + 1, this.pixelOf(this.cursor)),
                            extend);
                    return true;
                }
                case 265: {
                    int line = this.lineAt(this.cursor);
                    this.moveTo(line == 0 ? 0 : this.indexAt(line - 1, this.pixelOf(this.cursor)), extend);
                    return true;
                }
                case 268:
                    this.moveTo(this.lines.get(this.lineAt(this.cursor))[0], extend);
                    return true;
                case 269:
                    this.moveTo(this.lines.get(this.lineAt(this.cursor))[1], extend);
                    return true;
                default:
                    return false;
            }
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int line =
                Mth.clamp((int) ((mouseY - (double) this.getY()) / (double) this.lineHeight), 0, this.lines.size() - 1);
        this.moveTo(this.indexAt(line, (int) (mouseX - (double) this.getX())), Screen.hasShiftDown());
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        int line =
                Mth.clamp((int) ((mouseY - (double) this.getY()) / (double) this.lineHeight), 0, this.lines.size() - 1);
        this.moveTo(this.indexAt(line, (int) (mouseX - (double) this.getX())), true);
    }

    @Override
    public void playDownSound(SoundManager handler) {}

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        this.focusTime = Util.getMillis();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int start = this.selectionStart();
        int end = this.selectionEnd();

        for (int i = 0; i < this.lines.size(); i++) {
            int[] line = this.lines.get(i);
            int y = this.getY() + i * this.lineHeight;
            if (end > start && end > line[0] && start <= line[1]) {
                int from = Math.max(start, line[0]);
                int to = Math.min(end, line[1]);
                int x1 = this.getX() + this.font.width(this.value.substring(line[0], from));
                int x2 = this.getX() + this.font.width(this.value.substring(line[0], to));
                if (x2 > x1) {
                    guiGraphics.fill(x1, y - 1, x2, y + this.lineHeight - 1, -2143592705);
                }
            }

            guiGraphics.drawString(
                    this.font,
                    FormattedCharSequence.forward(this.value.substring(line[0], line[1]), this.style),
                    this.getX(),
                    y,
                    this.textColour,
                    false);
        }

        if (this.isFocused() && (Util.getMillis() - this.focusTime) / 300L % 2L == 0L) {
            int line = this.lineAt(this.cursor);
            int x = this.getX() + this.pixelOf(this.cursor);
            int y = this.getY() + line * this.lineHeight;
            guiGraphics.fill(x, y - 1, x + 1, y + this.lineHeight - 1, this.textColour);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}
}
