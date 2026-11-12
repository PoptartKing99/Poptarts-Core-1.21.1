package dev.poptartking.poptartcore.eclipse;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/** Shared daylight calculations, safe to load on a dedicated server. */
public final class EclipseLighting {
    private static EclipsePhase clientPhase = EclipsePhase.CLEAR;

    private EclipseLighting() {}

    public static EclipsePhase phase(Level level) {
        if (level.dimension() != Level.OVERWORLD) return EclipsePhase.CLEAR;
        return level instanceof ServerLevel serverLevel
                ? EclipseData.get(serverLevel).phaseAt(level.getGameTime(), level.getDayTime())
                : clientPhase;
    }

    public static void setClientPhase(EclipsePhase phase) {
        clientPhase = phase;
    }

    public static float sunFactor(Level level) {
        return 0.5F + 2 * Mth.clamp(Mth.cos(level.getTimeOfDay(1) * Mth.TWO_PI), -0.25F, 0.25F);
    }

    public static float darkness(Level level) {
        return (15 - phase(level).skylight()) * sunFactor(level);
    }

    public static boolean keepsNormalBedtime(Level level) {
        return level.dimension() == Level.OVERWORLD
                && !level.dimensionType().hasFixedTime()
                && darkness(level) > 0
                && !level.isDay()
                && ((EclipseDaylight) level).poptartcore$skyDarkenWithoutEclipse() < 4;
    }
}
