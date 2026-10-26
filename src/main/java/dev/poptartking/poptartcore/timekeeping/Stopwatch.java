package dev.poptartking.poptartcore.timekeeping;

import dev.poptartking.poptartcore.registry.PoptartCoreSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class Stopwatch {
    public static final int MAX_TICKS = 20000;
    private static final int TICK_INTERVAL = 10;
    private static final int TICKS_PER_SECOND = 20;

    private Stopwatch() {}

    public static boolean isRunning(ItemStack clock) {
        return clock.is(Items.CLOCK) && clock.has(TimekeepingComponents.STOPWATCH.get());
    }

    public static int getElapsed(Level level, ItemStack clock) {
        Long start = clock.get(TimekeepingComponents.STOPWATCH.get());
        return start == null ? 0 : (int) Math.min(20000L, Math.max(0L, level.getGameTime() - start));
    }

    public static void toggle(ItemStack clock, ServerPlayer player) {
        if (!clock.is(Items.CLOCK)) return;
        if (isRunning(clock)) {
            int elapsed = getElapsed(player.level(), clock);
            clock.remove(TimekeepingComponents.STOPWATCH.get());
            stop(player, elapsed);
        } else {
            clock.set(TimekeepingComponents.STOPWATCH.get(), player.level().getGameTime());
            play(player, PoptartCoreSounds.STOPWATCH_START.get(), 1.0F, 1.2F);
        }
    }

    public static void tickHeld(ItemStack clock, ServerPlayer player) {
        if (isRunning(clock) && tick(player, getElapsed(player.level(), clock))) {
            clock.remove(TimekeepingComponents.STOPWATCH.get());
        }
    }

    private static boolean tick(ServerPlayer player, int elapsed) {
        if (elapsed >= 20000) {
            stop(player, 20000);
            return true;
        } else {
            if (elapsed > 0 && elapsed % 10 == 0) {
                boolean tock = elapsed / 10 % 2 == 1;
                play(player, PoptartCoreSounds.STOPWATCH_TICK.get(), 0.6F, tock ? 0.9F : 1.3F);
            }

            return false;
        }
    }

    private static void stop(ServerPlayer player, int elapsed) {
        play(player, PoptartCoreSounds.STOPWATCH_STOP.get(), 1.0F, 0.9F);
        Component precise = Component.literal(formatPrecise(elapsed));
        HoverEvent hover = new HoverEvent(Action.SHOW_TEXT, precise);
        Component time = Component.literal(format(elapsed)).withStyle(style -> style.withHoverEvent(hover));
        player.sendSystemMessage(Component.translatable("poptartcore.stopwatch.total", new Object[] {time})
                .withStyle(ChatFormatting.GOLD));
    }

    private static void play(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.level()
                .playSound(
                        null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    public static String format(int ticks) {
        long totalSeconds = (long) (ticks / 20);
        long hours = totalSeconds / 3600L;
        long minutes = totalSeconds % 3600L / 60L;
        long seconds = totalSeconds % 60L;
        StringBuilder text = new StringBuilder();
        if (hours > 0L) {
            text.append(hours).append("h ");
        }

        if (hours > 0L || minutes > 0L) {
            text.append(minutes).append("m ");
        }

        return text.append(seconds).append('s').toString();
    }

    public static String formatPrecise(int ticks) {
        long minutes = (long) (ticks / 20) / 60L;
        long seconds = (long) (ticks / 20) % 60L;
        long millis = (long) (ticks % 20) * 50L;
        return String.format(java.util.Locale.ROOT, "%d:%02d:%03d", minutes, seconds, millis);
    }
}
