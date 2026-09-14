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
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIDER_PREPARE_SHOOT = SOUNDS.register(
            "spider.prepare_shoot",
            () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("spider.prepare_shoot")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIDER_SHOOT = SOUNDS.register(
            "spider.shoot", () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("spider.shoot")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPIDER_WEB_IMPACT = SOUNDS.register(
            "spider.web_impact", () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("spider.web_impact")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BEE_SMOKER_BLOW = SOUNDS.register(
            "bee_smoker.blow", () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("bee_smoker.blow")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BEE_SMOKER_RETRACT = SOUNDS.register(
            "bee_smoker.retract",
            () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("bee_smoker.retract")));

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }

    public static final DeferredHolder<SoundEvent, SoundEvent> MILLSTONE_LOOP = SOUNDS.register(
            "millstone.loop", () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("millstone.loop")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MILLSTONE_USE = SOUNDS.register(
            "millstone.use", () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("millstone.use")));
}
