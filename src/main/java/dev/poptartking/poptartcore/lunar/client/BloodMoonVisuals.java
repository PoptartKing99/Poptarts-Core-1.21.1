package dev.poptartking.poptartcore.lunar.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.lunar.BloodMoonData;
import dev.poptartking.poptartcore.registry.PoptartCoreSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler.LoopSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class BloodMoonVisuals {
    private static final float TWILIGHT_TICKS = 500;
    private static final Vec3 SKY_COLOR = Vec3.fromRGB24(0x200C17);
    private static boolean tonight;
    private static LoopSoundInstance soundtrack;

    private BloodMoonVisuals() {}

    public static void accept(boolean selectedNight) {
        tonight = selectedNight;
        Minecraft.getInstance().gameRenderer.lightTexture().tick();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void suppressBackgroundMusic(SelectMusicEvent event) {
        if (active(Minecraft.getInstance().level)) event.overrideMusic(null);
    }

    private static boolean active(ClientLevel level) {
        return tonight
                && level != null
                && level.dimension() == Level.OVERWORLD
                && !level.dimensionType().hasFixedTime()
                && BloodMoonData.timeOfDay(level.getDayTime()) >= BloodMoonData.NIGHT_START;
    }

    public static float strength(float partialTick) {
        var level = Minecraft.getInstance().level;
        float blend = nightStrength(level);
        return blend > 0 ? blend * (1 - 0.28F * level.getRainLevel(partialTick)) : 0;
    }

    static float nightStrength(ClientLevel level) {
        if (!active(level)) return 0;
        int time = BloodMoonData.timeOfDay(level.getDayTime());
        int ticksFromDaylight = Math.min(time - BloodMoonData.NIGHT_START, BloodMoonData.DAY_LENGTH - time);
        float blend = Mth.clamp(ticksFromDaylight / TWILIGHT_TICKS, 0, 1);
        return blend * blend * (3 - 2 * blend);
    }

    public static float rainStrength(float partialTick) {
        var level = Minecraft.getInstance().level;
        float blend = nightStrength(level);
        return blend > 0 && level.getRainLevel(partialTick) > 0 ? blend : 0;
    }

    public static void tintSkyLight(Vector3f color, float partialTick) {
        float blend = strength(partialTick);
        if (blend <= 0) return;
        // Give dark, green terrain a visible red cast when it multiplies its texture by the lightmap.
        color.lerp(new Vector3f(1.45F, 0.27F, 0.24F), blend);
    }

    public static Vec3 tintSky(Vec3 color, Vec3 position, float partialTick) {
        float blend = strength(partialTick);
        if (blend <= 0) return color;
        return color.lerp(SKY_COLOR, 0.65 * blend * skyExposure(BlockPos.containing(position)));
    }

    public static Vec3 tintClouds(Vec3 color, float partialTick) {
        float blend = strength(partialTick);
        if (blend <= 0) return color;
        return new Vec3(
                Math.min(1, color.x + color.z * 0.16 * blend),
                color.y * (1 - 0.22 * blend),
                color.z * (1 - 0.38 * blend));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void tintFog(ViewportEvent.ComputeFogColor event) {
        var camera = event.getCamera();
        if (camera.getEntity() instanceof LivingEntity living
                && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS))) return;
        var fluid = camera.getFluidInCamera();
        if (fluid == FogType.WATER) {
            tintUnderwaterFog(event);
            return;
        }
        if (fluid != FogType.NONE) return;
        float blend = strength((float) event.getPartialTick());
        if (blend <= 0) return;

        // Stored sky light measures exposure even when the world clock makes daylight dark.
        float amount = blend * skyExposure(camera.getBlockPosition());
        if (amount <= 0) return;

        float red = event.getRed();
        event.setRed(Mth.lerp(0.30F * amount, red, Math.max(red, 0.21F)));
        event.setGreen(event.getGreen() * (1 - 0.24F * amount));
        event.setBlue(event.getBlue() * (1 - 0.38F * amount));
    }

    private static void tintUnderwaterFog(ViewportEvent.ComputeFogColor event) {
        float amount = 0.90F * nightStrength(Minecraft.getInstance().level);
        if (amount <= 0) return;
        float light = Math.max(event.getRed(), Math.max(event.getGreen(), event.getBlue()));
        event.setRed(Mth.lerp(amount, event.getRed(), light * 0.86F));
        event.setGreen(Mth.lerp(amount, event.getGreen(), light * 0.10F));
        event.setBlue(Mth.lerp(amount, event.getBlue(), light * 0.08F));
    }

    private static float skyExposure(BlockPos position) {
        return Minecraft.getInstance().level.getBrightness(LightLayer.SKY, position) / 15F;
    }

    public static void drawMoon(float partialTick, Runnable draw) {
        var level = Minecraft.getInstance().level;
        float blend = nightStrength(level);
        if (blend <= 0) {
            draw.run();
            return;
        }
        // Vanilla fades the moon's alpha to zero as the rain reaches full strength.
        float alpha = RenderSystem.getShaderColor()[3];
        float moonAlpha = Math.min(1, alpha + 0.75F * blend * level.getRainLevel(partialTick));
        drawTinted(Mth.lerp(blend, 1F, 0.6F), 1 - blend, 1 - blend, moonAlpha, draw);
    }

    public static void drawRain(float blend, Runnable draw) {
        // Boost the contrast of the existing rain streaks, including blue resource-pack textures.
        float rainAlpha = RenderSystem.getShaderColor()[3] * Mth.lerp(blend, 1F, 1.65F);
        drawTinted(Mth.lerp(blend, 1F, 3.5F), Mth.lerp(blend, 1F, 0.29F), Mth.lerp(blend, 1F, 0.17F), rainAlpha, draw);
    }

    private static void drawTinted(
            float redScale, float greenScale, float blueScale, float tintedAlpha, Runnable draw) {
        float[] color = RenderSystem.getShaderColor();
        float red = color[0];
        float green = color[1];
        float blue = color[2];
        float alpha = color[3];
        RenderSystem.setShaderColor(red * redScale, green * greenScale, blue * blueScale, tintedAlpha);
        try {
            draw.run();
        } finally {
            RenderSystem.setShaderColor(red, green, blue, alpha);
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        boolean active = active(level);
        BloodMoonWater.update(minecraft, nightStrength(level));
        if (active && !level.isRaining() && minecraft.player != null && minecraft.player.isAlive()) {
            if (soundtrack == null || soundtrack.isStopped()) {
                soundtrack = new LoopSoundInstance(PoptartCoreSounds.BLOOD_MOON_MUSIC.get());
                minecraft.getSoundManager().play(soundtrack);
            }
            soundtrack.fadeIn();
        } else if (soundtrack != null) {
            soundtrack.fadeOut();
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        tonight = false;
        BloodMoonWater.clear();
        if (soundtrack != null) {
            Minecraft.getInstance().getSoundManager().stop(soundtrack);
            soundtrack = null;
        }
    }
}
