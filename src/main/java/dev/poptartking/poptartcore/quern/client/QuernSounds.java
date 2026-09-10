package dev.poptartking.poptartcore.quern.client;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.quern.QuernBlockEntity;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class QuernSounds {
    private static final Map<QuernBlockEntity, QuernSoundInstance> ACTIVE = new HashMap<>();

    private QuernSounds() {}

    public static void update(QuernBlockEntity quern) {
        if (!quern.isRotating() || quern.isRemoved()) {
            return;
        }
        var manager = Minecraft.getInstance().getSoundManager();
        QuernSoundInstance sound = ACTIVE.get(quern);
        if (sound == null || sound.isStopped() || !manager.isActive(sound)) {
            if (sound != null) {
                manager.stop(sound);
            }
            sound = new QuernSoundInstance(quern);
            ACTIVE.put(quern, sound);
            manager.play(sound);
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ACTIVE.entrySet().removeIf(entry -> {
            QuernBlockEntity quern = entry.getKey();
            QuernSoundInstance sound = entry.getValue();
            if (quern.isRemoved() || quern.getLevel() != minecraft.level || sound.isStopped()) {
                minecraft.getSoundManager().stop(sound);
                return true;
            }
            return false;
        });
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        ACTIVE.values().forEach(Minecraft.getInstance().getSoundManager()::stop);
        ACTIVE.clear();
    }
}
