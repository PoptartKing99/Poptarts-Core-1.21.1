package dev.poptartking.poptartcore.lunar;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

/** The selected night and the last rolls belong to the Overworld save. */
public final class BloodMoonData extends SavedData {
    static final String FILE_ID = "poptartcore_blood_moon";
    public static final int NIGHT_START = 12_500;
    public static final int MIDNIGHT = 18_000;
    public static final int DAY_LENGTH = 24_000;
    private static final long NONE = Long.MIN_VALUE;
    private static final int MIN_CLEAR_NIGHTS = 4;
    private static final int MAX_NIGHTS_BETWEEN = 98;
    private static final int BLOOD_MOON_WEIGHT = 10;
    private static final int ORDINARY_NIGHT_WEIGHT = 100;
    private static final Factory<BloodMoonData> FACTORY = new Factory<>(BloodMoonData::new, BloodMoonData::load);

    private boolean natural = true;
    private long observedDay = NONE;
    private long checkedNight = NONE;
    private long bloodMoonNight = NONE;
    private long siegeNight = NONE;
    private boolean canceled;
    // Null on load so the first sync can resume an event without repeating its announcement.
    private Boolean published;

    public static BloodMoonData get(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
    }

    public static boolean active(Level level) {
        return level instanceof ServerLevel serverLevel
                && level.dimension() == Level.OVERWORLD
                && !level.dimensionType().hasFixedTime()
                && !level.isRaining()
                && get(serverLevel).isBloodMoon(serverLevel.getDayTime());
    }

    public static int timeOfDay(long dayTime) {
        return Math.floorMod(dayTime, DAY_LENGTH);
    }

    private static long day(long dayTime) {
        return Math.floorDiv(dayTime, DAY_LENGTH);
    }

    public boolean scheduled(long dayTime) {
        return !canceled && bloodMoonNight == day(dayTime);
    }

    public boolean isBloodMoon(long dayTime) {
        return timeOfDay(dayTime) >= NIGHT_START && scheduled(dayTime);
    }

    public boolean natural() {
        return natural;
    }

    public void setNatural(boolean enabled, long dayTime) {
        if (natural != enabled) {
            natural = enabled;
            setDirty();
        }
        if (!enabled) stop(dayTime);
    }

    public void start(long dayTime) {
        observeDay(day(dayTime));
        bloodMoonNight = checkedNight = day(dayTime);
        canceled = false;
        setDirty();
    }

    public void stop(long dayTime) {
        long day = day(dayTime);
        observeDay(day);
        if (scheduled(dayTime)) {
            canceled = true;
            setDirty();
        }
        if (timeOfDay(dayTime) >= NIGHT_START && checkedNight != day) {
            checkedNight = day;
            setDirty();
        }
    }

    /** Returns whether this tick selected a new natural event. */
    boolean update(long dayTime, RandomSource random) {
        long day = day(dayTime);
        observeDay(day);
        if (!natural || timeOfDay(dayTime) < NIGHT_START || day == checkedNight) return false;
        checkedNight = day;
        setDirty();
        long gap = bloodMoonNight == NONE ? Long.MAX_VALUE : day - bloodMoonNight;
        if (gap <= MIN_CLEAR_NIGHTS) return false;
        boolean overdue = bloodMoonNight != NONE && gap >= MAX_NIGHTS_BETWEEN;
        if (!overdue && random.nextInt(ORDINARY_NIGHT_WEIGHT + BLOOD_MOON_WEIGHT) >= BLOOD_MOON_WEIGHT) {
            return false;
        }
        start(dayTime);
        return true;
    }

    /** A slow or frozen midnight still gets a single village siege roll. */
    public boolean claimSiegeRoll(long dayTime) {
        long day = day(dayTime);
        if (timeOfDay(dayTime) < MIDNIGHT || !isBloodMoon(dayTime) || siegeNight == day) return false;
        siegeNight = day;
        setDirty();
        return true;
    }

    Boolean publish(boolean value) {
        Boolean previous = published;
        published = value;
        return previous;
    }

    private void observeDay(long day) {
        if (day == observedDay) return;
        // Preserve the age of the last event and rolls when a time command rewinds the calendar.
        if (observedDay != NONE && day < observedDay) {
            long shift = day - observedDay;
            if (checkedNight != NONE) checkedNight += shift;
            if (bloodMoonNight != NONE) bloodMoonNight += shift;
            if (siegeNight != NONE) siegeNight += shift;
        }
        observedDay = day;
        setDirty();
    }

    static BloodMoonData load(CompoundTag tag, HolderLookup.Provider registries) {
        var data = new BloodMoonData();
        data.natural = !tag.contains("natural", Tag.TAG_BYTE) || tag.getBoolean("natural");
        if (tag.contains("observed_day", Tag.TAG_LONG)) data.observedDay = tag.getLong("observed_day");
        if (tag.contains("checked_night", Tag.TAG_LONG)) data.checkedNight = tag.getLong("checked_night");
        if (tag.contains("blood_moon_night", Tag.TAG_LONG)) data.bloodMoonNight = tag.getLong("blood_moon_night");
        if (tag.contains("siege_night", Tag.TAG_LONG)) data.siegeNight = tag.getLong("siege_night");
        data.canceled = tag.getBoolean("canceled");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("natural", natural);
        tag.putLong("observed_day", observedDay);
        tag.putLong("checked_night", checkedNight);
        tag.putLong("blood_moon_night", bloodMoonNight);
        tag.putLong("siege_night", siegeNight);
        tag.putBoolean("canceled", canceled);
        return tag;
    }
}
