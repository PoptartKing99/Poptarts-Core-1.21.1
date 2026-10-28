package dev.poptartking.poptartcore.timekeeping.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.navigation.client.hud.Draw;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ClockHud {
    private static final ResourceLocation SUN = PoptartCore.location("sun");
    private static final ResourceLocation MOON = PoptartCore.location("moon");
    private static final long DAY_LENGTH = 24000L;
    private static final long NIGHTFALL = 12000L;
    private static final int MARGIN = 4;
    private static final int ICON = 12;
    private static final int GAP = 3;

    private ClockHud() {}

    public static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player != null
                && level != null
                && !minecraft.options.hideGui
                && minecraft.screen == null
                && !player.isSpectator()) {
            if (dev.poptartking.poptartcore.depthmeter.DepthMeter.carries(player, Items.CLOCK)) {
                long timeOfDay = getTimeOfDay(level);
                Font font = minecraft.font;
                Draw.sprite(graphics, timeOfDay < NIGHTFALL ? SUN : MOON, MARGIN, MARGIN, ICON, ICON);
                Draw.text(
                        graphics,
                        font,
                        Component.literal(format(timeOfDay)),
                        MARGIN + ICON + GAP,
                        MARGIN + (ICON - 9) / 2,
                        -1,
                        true);
            }
        }
    }

    public static void appendTooltip(ItemStack stack, Consumer<Component> lines) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && stack.is(Items.CLOCK)) {
            lines.accept(Component.translatable("poptartcore.clock.time", new Object[] {getTime(level)})
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    public static String getTime(ClientLevel level) {
        return format(getTimeOfDay(level));
    }

    private static long getTimeOfDay(ClientLevel level) {
        return Math.floorMod(level.getDayTime(), DAY_LENGTH);
    }

    public static String format(long timeOfDay) {
        timeOfDay = Math.floorMod(timeOfDay, DAY_LENGTH);
        long hours = (timeOfDay / 1000L + 6L) % 24L;
        long minutes = timeOfDay % 1000L * 60L / 1000L;
        return String.format(java.util.Locale.ROOT, "%02d:%02d", hours, minutes);
    }
}
