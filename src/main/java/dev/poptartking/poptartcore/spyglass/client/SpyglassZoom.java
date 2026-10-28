package dev.poptartking.poptartcore.spyglass.client;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

@net.neoforged.fml.common.EventBusSubscriber(
        modid = dev.poptartking.poptartcore.PoptartCore.MOD_ID,
        value = net.neoforged.api.distmarker.Dist.CLIENT)
public final class SpyglassZoom {
    private static final float[] STEPS = new float[] {0.05F, 0.1F, 0.2F, 0.4F};
    private static final int DEFAULT_STEP = 1;
    private static final float ZOOM_IN_PITCH = 1.25F;
    private static final float ZOOM_OUT_PITCH = 0.8F;
    private static int step = 1;
    private static int lastScopingTick = Integer.MIN_VALUE;

    private SpyglassZoom() {}

    public static boolean isZooming() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        return player != null
                && player.isScoping()
                && minecraft.options.getCameraType().isFirstPerson();
    }

    public static float getModifier(Player player, float vanilla) {
        if (player != Minecraft.getInstance().player) {
            return vanilla;
        } else {
            track(player);
            return STEPS[step];
        }
    }

    public static float getMinimumModifier(float vanilla) {
        return isZooming() ? Math.min(vanilla, STEPS[0]) : vanilla;
    }

    public static boolean scroll(double amount) {
        if (amount != 0.0 && isZooming()) {
            Player player = Minecraft.getInstance().player;
            track(player);
            int next = Mth.clamp(step + (amount > 0.0 ? -1 : 1), 0, STEPS.length - 1);
            if (next != step) {
                player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, next < step ? ZOOM_IN_PITCH : ZOOM_OUT_PITCH);
                step = next;
            }

            return true;
        } else {
            return false;
        }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        if (!isZooming()) reset();
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void logout(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        reset();
    }

    private static void reset() {
        step = DEFAULT_STEP;
        lastScopingTick = Integer.MIN_VALUE;
    }

    private static void track(Player player) {
        if (player.tickCount < lastScopingTick || player.tickCount - lastScopingTick > 1) {
            step = 1;
        }

        lastScopingTick = player.tickCount;
    }
}
