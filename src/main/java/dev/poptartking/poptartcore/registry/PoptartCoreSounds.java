package dev.poptartking.poptartcore.registry;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PoptartCoreSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, PoptartCore.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> QUERN =
            SOUNDS.register("quern", () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("quern")));

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }

    public static final DeferredHolder<SoundEvent, SoundEvent> MILLSTONE_LOOP = SOUNDS.register(
            "millstone.loop", () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("millstone.loop")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MILLSTONE_USE = SOUNDS.register(
            "millstone.use", () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("millstone.use")));
}
