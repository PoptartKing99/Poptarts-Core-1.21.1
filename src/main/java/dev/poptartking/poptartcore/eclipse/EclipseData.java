package dev.poptartking.poptartcore.eclipse;

import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.saveddata.SavedData;

/** One persisted eclipse controller per world, shared by natural cycles and manual previews. */
public final class EclipseData extends SavedData {
    private static final String FILE_ID = "poptartcore_eclipse";
    private static final int MAX_CATCH_UP_DAYS = 64;
    private static final Set<String> LEGACY_COVERED_STEPS =
            Set.of("gibbous_growing", "half_growing", "crescent_growing", "half_clearing", "gibbous_clearing");
    private static final Factory<EclipseData> FACTORY = new Factory<>(EclipseData::new, (tag, registries) -> load(tag));
    private EclipseState state = new EclipseState.Off();
    private EclipsePhase lastSent = EclipsePhase.CLEAR;
    private boolean migrateLegacyNatural;

    public static EclipseData get(MinecraftServer server) {
        return get(server.overworld());
    }

    /** Also works during Overworld construction, before server.overworld() has been assigned. */
    public static EclipseData get(ServerLevel overworld) {
        var storage = overworld.getDataStorage();
        var data = storage.get(FACTORY, FILE_ID);
        if (data == null) {
            data = new EclipseData();
            data.setState(EclipseState.Waiting.start(overworld.getDayTime(), overworld.getRandom()));
            storage.set(FILE_ID, data);
        }
        data.migrate(overworld.getDayTime(), overworld.getRandom());
        return data;
    }

    public EclipseState state() {
        return state;
    }

    public EclipsePhase phaseAt(long gameTime, long dayTime) {
        return state.phaseAt(gameTime, dayTime);
    }

    public void setState(EclipseState state) {
        this.state = java.util.Objects.requireNonNull(state);
        migrateLegacyNatural = false;
        setDirty();
    }

    /** Initialize a saved countdown once when upgrading an old clear/waiting state. */
    void migrate(long dayTime, RandomSource random) {
        if (migrateLegacyNatural) setState(EclipseState.Waiting.start(dayTime, random));
    }

    /** Returns true only when clients need a new phase, including expiry and clock jumps. */
    public boolean update(long gameTime, long dayTime, RandomSource random) {
        if (state instanceof EclipseState.Daily daily) {
            long today = EclipseState.Daily.dayAt(dayTime);
            if (today < daily.day()) {
                // Rebase the date on rewind; intraday coverage still follows the sun's new position.
                setState(daily.withDay(today));
            } else {
                for (int count = 0; daily.day() < today && count < MAX_CATCH_UP_DAYS; count++) {
                    daily = daily.advance(random);
                    setState(daily);
                }
            }
        }
        EclipsePhase phase = phaseAt(gameTime, dayTime);
        if (state instanceof EclipseState.Running && phase == EclipsePhase.CLEAR) {
            setState(new EclipseState.Off());
        }
        if (lastSent == phase) return false;
        lastSent = phase;
        return true;
    }

    public static EclipseData load(CompoundTag tag) {
        EclipseData data = new EclipseData();
        switch (tag.getString("mode")) {
            case "off" -> data.migrateLegacyNatural = !tag.contains("format_version");
            case "preview" ->
                EclipsePhase.byName(tag.getString("phase"))
                        .ifPresent(phase -> data.state = new EclipseState.Preview(phase));
            case "running" -> {
                int duration = tag.getInt("duration_ticks");
                long started = tag.getLong("started_at");
                if (duration > 0 && duration <= 72_000 && started >= 0) {
                    data.state = new EclipseState.Running(started, duration);
                }
            }
            case "partial" -> {
                String stepId = tag.getString("step");
                if (tag.getInt("format_version") >= 3 || LEGACY_COVERED_STEPS.contains(stepId)) {
                    data.state = new EclipseState.Daylight(tag.getLong("day"));
                }
                if (tag.getInt("format_version") < 2
                        && Set.of("rest", "new_growing", "new_clearing").contains(stepId)) {
                    data.migrateLegacyNatural = true;
                }
            }
            case "waiting" -> {
                int remaining = tag.getInt("clear_days_remaining");
                if (remaining >= 1 && remaining <= EclipseState.Waiting.MAX_CLEAR_DAYS) {
                    data.state = new EclipseState.Waiting(tag.getLong("day"), remaining);
                }
            }
            default -> {}
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("format_version", 3);
        switch (state) {
            case EclipseState.Off ignored -> tag.putString("mode", "off");
            case EclipseState.Preview preview -> {
                tag.putString("mode", "preview");
                tag.putString("phase", preview.phase().id());
            }
            case EclipseState.Running running -> {
                tag.putString("mode", "running");
                tag.putLong("started_at", running.startedAt());
                tag.putInt("duration_ticks", running.durationTicks());
            }
            case EclipseState.Daylight daylight -> {
                // Keep the saved mode ID used by existing worlds.
                tag.putString("mode", "partial");
                tag.putLong("day", daylight.day());
            }
            case EclipseState.Waiting waiting -> {
                tag.putString("mode", "waiting");
                tag.putLong("day", waiting.day());
                tag.putInt("clear_days_remaining", waiting.clearDaysRemaining());
            }
        }
        return tag;
    }
}
