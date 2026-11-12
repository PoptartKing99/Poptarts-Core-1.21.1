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

    public static final DeferredHolder<SoundEvent, SoundEvent> PIXIE_PHASE = registerSound("pixie.phase");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIXIE_VANISH = registerSound("pixie.vanish");
    public static final DeferredHolder<SoundEvent, SoundEvent> ALLAY_SUMMON = registerSound("pixie.allay_summon");
    public static final DeferredHolder<SoundEvent, SoundEvent> VEX_SUMMON = registerSound("pixie.vex_summon");
    public static final DeferredHolder<SoundEvent, SoundEvent> PIXIE_SHIMMER = registerSound("pixie.shimmer");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(PoptartCore.location(name)));
    }

    public static final DeferredHolder<SoundEvent, SoundEvent> CONDUIT_HEARTBEAT_SINGLE = SOUNDS.register(
            "conduit.heartbeat_single",
            () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("conduit.heartbeat_single")));

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

    public static final DeferredHolder<SoundEvent, SoundEvent> COMPASS_FULL = SOUNDS.register(
            "compass_full", () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("compass_full")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WAYPOINT_SELECT = SOUNDS.register(
            "waypoint_select", () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("waypoint_select")));
    public static final DeferredHolder<SoundEvent, SoundEvent> WAYPOINT_HIDE = SOUNDS.register(
            "waypoint_hide", () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("waypoint_hide")));
    public static final DeferredHolder<SoundEvent, SoundEvent> NAVIGATION_TOGGLE = SOUNDS.register(
            "navigation_toggle", () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("navigation_toggle")));

    public static final DeferredHolder<SoundEvent, SoundEvent> STOPWATCH_START = SOUNDS.register(
            "stopwatch_start", () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("stopwatch_start")));
    public static final DeferredHolder<SoundEvent, SoundEvent> STOPWATCH_STOP = SOUNDS.register(
            "stopwatch_stop", () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("stopwatch_stop")));
    public static final DeferredHolder<SoundEvent, SoundEvent> STOPWATCH_TICK = SOUNDS.register(
            "stopwatch_tick", () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("stopwatch_tick")));

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }

    public static final DeferredHolder<SoundEvent, SoundEvent> MILLSTONE_LOOP = SOUNDS.register(
            "millstone.loop", () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("millstone.loop")));
    public static final DeferredHolder<SoundEvent, SoundEvent> MILLSTONE_USE = SOUNDS.register(
            "millstone.use", () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("millstone.use")));
    public static final DeferredHolder<SoundEvent, SoundEvent> FLESH_GEODE_BREATHING = SOUNDS.register(
            "flesh_geode.breathing",
            () -> SoundEvent.createVariableRangeEvent(PoptartCore.location("flesh_geode.breathing")));
    public static final DeferredHolder<SoundEvent, SoundEvent> BLOOD_MOON_MUSIC = registerSound("blood_moon");
}
