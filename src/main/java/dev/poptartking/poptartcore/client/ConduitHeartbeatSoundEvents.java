package dev.poptartking.poptartcore.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundSourceEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class ConduitHeartbeatSoundEvents {
    private ConduitHeartbeatSoundEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ConduitEyeAnimation.tickPlaybackCleanup();
    }

    @SubscribeEvent
    public static void onHeartbeatSound(PlaySoundSourceEvent event) {
        var sound = event.getSound();
        boolean singlePair = sound.getLocation().equals(
                PoptartCoreSounds.CONDUIT_HEARTBEAT_SINGLE.get().getLocation());
        if (!singlePair && !sound.getLocation().equals(SoundEvents.CONDUIT_AMBIENT.getLocation())) return;
        BlockPos position = BlockPos.containing(sound.getX(), sound.getY(), sound.getZ());
        long start = System.nanoTime();
        Minecraft.getInstance().execute(() -> ConduitEyeAnimation.heartbeatStarted(position, start, singlePair));
    }
}
