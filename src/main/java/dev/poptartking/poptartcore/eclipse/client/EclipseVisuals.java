package dev.poptartking.poptartcore.eclipse.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.eclipse.EclipseLighting;
import dev.poptartking.poptartcore.eclipse.EclipsePhase;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** Client-only adaptation of Wayfarer's eclipse lighting and atlas sampling. */
@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class EclipseVisuals {
    private static final ResourceLocation SUN = PoptartCore.location("textures/environment/eclipse_phases.png");
    // Keep the warm totality tint subtle instead of replacing the darkened fog color.
    private static final float MAX_FOG_GLOW = 0.15F;

    private EclipseVisuals() {}

    public static void accept(EclipsePhase updated) {
        EclipseLighting.setClientPhase(updated);
        var level = Minecraft.getInstance().level;
        if (level != null) level.updateSkyBrightness();
        Minecraft.getInstance().gameRenderer.lightTexture().tick();
    }

    public static EclipsePhase visiblePhase() {
        var level = Minecraft.getInstance().level;
        return level == null ? EclipsePhase.CLEAR : EclipseLighting.phase(level);
    }

    public static float sunFactor() {
        var level = Minecraft.getInstance().level;
        return level == null ? 0 : EclipseLighting.sunFactor(level);
    }

    public static float darkness() {
        var level = Minecraft.getInstance().level;
        return level == null ? 0 : EclipseLighting.darkness(level);
    }

    public static float brightness() {
        return 1 - darkness() / 15;
    }

    public static ResourceLocation sunTexture(ResourceLocation original) {
        return visiblePhase() == EclipsePhase.CLEAR ? original : SUN;
    }

    public static float sunU(float original) {
        return visiblePhase() == EclipsePhase.CLEAR ? original : (visiblePhase().frame() % 4 + original) / 4;
    }

    public static float sunV(float original) {
        return visiblePhase() == EclipsePhase.CLEAR ? original : (visiblePhase().frame() / 4 + original) / 2;
    }

    public static Vec3 darken(Vec3 original) {
        return original.scale(brightness());
    }

    public static float starBrightness(float original) {
        float dark = darkness() / 15;
        return Math.max(original, dark * dark * 0.5F);
    }

    public static void darkenLightmap(NativeImage pixels, int[] scratch) {
        float darkness = darkness();
        if (darkness <= 0) return;
        for (int sky = 0; sky < 16; sky++) {
            for (int block = 0; block < 16; block++) scratch[sky * 16 + block] = pixels.getPixelRGBA(block, sky);
        }
        float coldness = visiblePhase().coverage() / 4F * sunFactor() * 0.6F;
        for (int sky = 0; sky < 16; sky++) {
            float source = sky - darkness;
            int lower = Mth.floor(source);
            float fraction = source - lower;
            for (int block = 0; block < 16; block++) {
                int color = lower < 0
                        ? scratch[block]
                        : FastColor.ARGB32.lerp(
                                fraction, scratch[lower * 16 + block], scratch[Math.min(lower + 1, 15) * 16 + block]);
                float cold = coldness * sky / 15F * (1 - block / 15F);
                pixels.setPixelRGBA(block, sky, cold > 0 ? chill(color, cold) : color);
            }
        }
    }

    private static int chill(int color, float amount) {
        int red = FastColor.ABGR32.red(color);
        int green = FastColor.ABGR32.green(color);
        int blue = FastColor.ABGR32.blue(color);
        float luminance = red * 0.299F + green * 0.587F + blue * 0.114F;
        return FastColor.ABGR32.color(
                FastColor.ABGR32.alpha(color),
                Math.round(Mth.lerp(amount, blue, Math.min(255, luminance * 1.18F))),
                Math.round(Mth.lerp(amount, green, Math.min(255, luminance))),
                Math.round(Mth.lerp(amount, red, Math.min(255, luminance * 0.9F))));
    }

    @SubscribeEvent
    public static void fog(ViewportEvent.ComputeFogColor event) {
        float brightness = brightness();
        if (brightness >= 1) return;
        float glow = 0;
        var camera = event.getCamera();
        boolean obscured = camera.getEntity() instanceof LivingEntity living
                && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS));
        if (visiblePhase() == EclipsePhase.TOTAL && camera.getFluidInCamera() == FogType.NONE && !obscured) {
            var level = Minecraft.getInstance().level;
            glow = MAX_FOG_GLOW
                    * sunFactor()
                    * (1 - level.getRainLevel(1) * 0.5F)
                    * (1 - level.getThunderLevel(1) * 0.5F);
        }
        event.setRed(Mth.lerp(glow, event.getRed() * brightness, 0.55F));
        event.setGreen(Mth.lerp(glow, event.getGreen() * brightness, 0.3F));
        event.setBlue(Mth.lerp(glow, event.getBlue() * brightness, 0.2F));
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        EclipseLighting.setClientPhase(EclipsePhase.CLEAR);
    }
}
