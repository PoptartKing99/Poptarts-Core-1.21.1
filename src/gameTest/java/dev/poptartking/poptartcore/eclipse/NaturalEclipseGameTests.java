package dev.poptartking.poptartcore.eclipse;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NaturalEclipseGameTests {
    @GameTest(template = "millstone_test")
    public static void rareStartsReachTotalityAndClearAtSunset(GameTestHelper h) {
        var minimum = new DelayRoll(0);
        var maximum = new DelayRoll(10);
        var data = new EclipseData();
        data.setState(EclipseState.Waiting.start(6000, minimum));
        h.assertTrue(
                data.state().equals(new EclipseState.Waiting(0, 20)) && minimum.calls == 1,
                "The minimum clear period is chosen once as twenty days");
        for (int day = 1; day < 20; day++) {
            data.update(day, day * 24000L + 6000, new NoRolls());
            h.assertTrue(
                    data.state().equals(new EclipseState.Waiting(day, 20 - day))
                            && data.phaseAt(day, day * 24000L + 6000) == EclipsePhase.CLEAR,
                    "Each waiting day stays clear without another roll");
        }
        EclipsePhase[] sequence = {
            EclipsePhase.WANING_GIBBOUS,
            EclipsePhase.WANING_HALF,
            EclipsePhase.WANING_CRESCENT,
            EclipsePhase.TOTAL,
            EclipsePhase.WAXING_CRESCENT,
            EclipsePhase.WAXING_HALF,
            EclipsePhase.WAXING_GIBBOUS
        };
        int[] boundaries = {1000, 2600, 4200, 5800, 6200, 8100, 10000, 12000};
        for (int index = 0; index < sequence.length; index++) {
            long time = 20 * 24000L + boundaries[index];
            data.update(100 + index, time, new NoRolls());
            h.assertTrue(
                    data.state() instanceof EclipseState.Daylight daylight
                            && daylight.day() == 20
                            && data.phaseAt(100 + index, time) == sequence[index]
                            && data.phaseAt(100 + index, 20 * 24000L + boundaries[index + 1] - 1) == sequence[index],
                    "The daylight event shows " + sequence[index].id() + " from tick " + boundaries[index]);
        }
        data.update(107, 20 * 24000L + 12000, new NoRolls());
        h.assertTrue(
                data.phaseAt(107, 20 * 24000L + 12000) == EclipsePhase.CLEAR,
                "The sky clears at sunset without rerolling the waiting period");
        data.update(108, 20 * 24000L + 18000, maximum);
        h.assertTrue(
                data.state().equals(new EclipseState.Waiting(21, 30)) && maximum.calls == 1,
                "Midnight after the event schedules thirty complete clear days at the maximum roll");
        data.update(109, 50 * 24000L + 6000, new NoRolls());
        h.assertTrue(data.state().equals(new EclipseState.Waiting(50, 1)), "The final clear day is still unobscured");
        data.update(110, 51 * 24000L + 1000, new NoRolls());
        h.assertTrue(
                data.phaseAt(110, 51 * 24000L + 1000) == EclipsePhase.WANING_GIBBOUS,
                "The next eclipse starts after exactly thirty clear days");
        h.succeed();
    }

    @GameTest(template = "millstone_test")
    public static void midnightReloadAndTimeRewindPreserveTheSchedule(GameTestHelper h) {
        var data = new EclipseData();
        data.setState(new EclipseState.Waiting(0, 1));
        h.assertTrue(!data.update(100, 17999, new NoRolls()), "Nothing advances before midnight");
        h.assertTrue(
                !data.update(101, 18000, new NoRolls()) && data.state().equals(new EclipseState.Daylight(1)),
                "The final waiting midnight schedules the next morning without a nighttime eclipse");
        h.assertTrue(!data.update(102, 24999, new NoRolls()), "Dawn stays clear until the first morning phase");
        h.assertTrue(data.update(103, 25000, new NoRolls()), "The first covered phase starts in the morning");
        var loaded = EclipseData.load(data.save(new CompoundTag(), h.getLevel().registryAccess()));
        loaded.migrate(25000, new NoRolls());
        loaded.update(104, 25000, new NoRolls());
        h.assertTrue(
                loaded.state().equals(data.state()) && !loaded.update(105, 25001, new NoRolls()),
                "Reloading preserves the event date and does not restart the morning");
        loaded.update(106, 30000, new NoRolls());
        h.assertTrue(
                loaded.phaseAt(106, 30000) == EclipsePhase.TOTAL,
                "The persisted event reaches totality at noon on the same day");
        loaded.update(107, 6000, new NoRolls());
        h.assertTrue(
                loaded.state().equals(new EclipseState.Daylight(0)) && loaded.phaseAt(107, 6000) == EclipsePhase.TOTAL,
                "Rewinding the date keeps the event and follows the new sun position");
        loaded.update(108, 18000, new DelayRoll(0));
        h.assertTrue(loaded.state().equals(new EclipseState.Waiting(1, 20)), "The next midnight starts the clear wait");
        loaded.setState(new EclipseState.Waiting(10, 7));
        loaded.update(109, 6000, new NoRolls());
        h.assertTrue(
                loaded.state().equals(new EclipseState.Waiting(0, 7)), "Rewinding also preserves remaining clear days");
        var saved = loaded.save(new CompoundTag(), h.getLevel().registryAccess());
        loaded = EclipseData.load(saved);
        loaded.migrate(6000, new NoRolls());
        loaded.update(110, 18000, new NoRolls());
        h.assertTrue(
                loaded.state().equals(new EclipseState.Waiting(1, 6)),
                "A saved countdown resumes without resampling its delay");
        h.assertTrue(
                EclipseState.Daily.dayAt(-6001) == -1
                        && EclipseState.Daily.dayAt(-6000) == 0
                        && EclipseState.Daily.dayAt(Long.MAX_VALUE) > 0,
                "Calendar conversion handles negative and very large times");
        h.succeed();
    }

    @GameTest(template = "millstone_test")
    public static void slowAndFrozenWorldClocksDoNotUseElapsedGameTicks(GameTestHelper h) {
        var data = new EclipseData();
        data.setState(new EclipseState.Daylight(0));
        data.update(1, 1000, new NoRolls());
        data.update(1_000_000, 2599, new NoRolls());
        h.assertTrue(
                data.phaseAt(1_000_000, 2599) == EclipsePhase.WANING_GIBBOUS,
                "A long real-time delay cannot skip ahead of a slowly advancing world clock");
        h.assertTrue(
                data.update(1_000_001, 2600, new NoRolls()),
                "Crossing the next sun-position boundary advances coverage");
        for (long gameTime = 1_000_002; gameTime < 1_000_100; gameTime++) {
            h.assertTrue(
                    !data.update(gameTime, 2600, new NoRolls()), "Frozen daylight neither advances nor resends phases");
        }
        data.update(1_000_100, 12000, new NoRolls());
        h.assertTrue(
                data.phaseAt(1_000_100, 12000) == EclipsePhase.CLEAR,
                "A forward clock jump clears the eclipse at sunset");
        for (long time : new long[] {0, 999, 12000, 17999, 18000, 23999}) {
            h.assertTrue(
                    new EclipseState.Daylight(0).phaseAt(0, time) == EclipsePhase.CLEAR,
                    "Dawn, sunset and night remain uncovered");
        }
        h.succeed();
    }

    @GameTest(template = "millstone_test")
    public static void largeTimeJumpsCatchUpWithoutSkippingScheduledEvents(GameTestHelper h) {
        var batched = new EclipseData();
        var sequential = new EclipseData();
        batched.setState(new EclipseState.Waiting(0, 25));
        sequential.setState(new EclipseState.Waiting(0, 25));
        var firstRandom = RandomSource.create(73);
        var secondRandom = RandomSource.create(73);
        long target = 10_000;
        long time = target * 24000 + 6000;
        batched.update(1, time, firstRandom);
        h.assertTrue(
                batched.state() instanceof EclipseState.Daily daily && daily.day() > 0 && daily.day() < target,
                "A huge calendar jump processes only a bounded number of days per tick");
        for (int pass = 0;
                pass < 2000 && batched.state() instanceof EclipseState.Daily daily && daily.day() < target;
                pass++) {
            batched.update(2 + pass, time, firstRandom);
        }
        for (long day = 1; day <= target; day++) sequential.update(day, day * 24000 + 6000, secondRandom);
        h.assertTrue(
                batched.state().equals(sequential.state()),
                "Batched and daily updates preserve the same events and chosen waits");
        h.assertTrue(
                !batched.update(99999, time, new NoRolls()), "Repeating the same update consumes no extra randomness");
        h.succeed();
    }

    @GameTest(template = "millstone_test")
    public static void savedAndLegacyModesPreserveProgressAndExplicitOff(GameTestHelper h) {
        var legacy = new CompoundTag();
        legacy.putString("mode", "off");
        var data = EclipseData.load(legacy);
        data.migrate(6000, new DelayRoll(0));
        h.assertTrue(
                data.state().equals(new EclipseState.Waiting(0, 20)),
                "The old default off state gains a clear waiting period");
        data.migrate(999999, new NoRolls());
        h.assertTrue(
                data.state().equals(new EclipseState.Waiting(0, 20)), "Migration only schedules the first wait once");
        for (String step : java.util.List.of("rest", "new_growing", "new_clearing")) {
            legacy.putInt("format_version", 1);
            legacy.putString("mode", "partial");
            legacy.putString("step", step);
            legacy.putLong("day", 44);
            data = EclipseData.load(legacy);
            data.migrate(6000, new DelayRoll(10));
            h.assertTrue(
                    data.state().equals(new EclipseState.Waiting(0, 30)),
                    "Old unobscured partial states gain the new rare-event wait");
        }
        legacy.putString("step", "half_growing");
        data = EclipseData.load(legacy);
        data.migrate(6000, new NoRolls());
        h.assertTrue(
                data.state().equals(new EclipseState.Daylight(44))
                        && data.phaseAt(0, 44 * 24000L + 6000) == EclipsePhase.TOTAL,
                "An existing five-day event adopts the new intraday coverage schedule");
        var saved = data.save(new CompoundTag(), h.getLevel().registryAccess());
        h.assertTrue(
                saved.getInt("format_version") == 3 && !saved.contains("step"),
                "Only the event date is saved in the new format");
        var loaded = EclipseData.load(saved);
        loaded.migrate(6000, new NoRolls());
        h.assertTrue(loaded.state().equals(data.state()), "Event position and calendar checkpoint survive saving");
        data.setState(new EclipseState.Off());
        loaded = EclipseData.load(data.save(new CompoundTag(), h.getLevel().registryAccess()));
        loaded.migrate(999999, new NoRolls());
        loaded.update(2, 999999, new NoRolls());
        h.assertTrue(loaded.state() instanceof EclipseState.Off, "An explicit off command remains off after reload");
        saved.putInt("format_version", 2);
        saved.putString("step", "total");
        h.assertTrue(
                EclipseData.load(saved).state() instanceof EclipseState.Off,
                "Invalid active phases cannot restore totality");
        saved.putString("mode", "waiting");
        for (int remaining : new int[] {0, 31}) {
            saved.putInt("clear_days_remaining", remaining);
            h.assertTrue(
                    EclipseData.load(saved).state() instanceof EclipseState.Off,
                    "Invalid saved countdowns are rejected at load");
        }
        h.succeed();
    }

    @GameTest(template = "millstone_test")
    public static void registeredCommandsAndTickEventControlNaturalMode(GameTestHelper h) throws Exception {
        var level = h.getLevel();
        var server = level.getServer();
        var data = EclipseData.get(server);
        var prior = data.state();
        long oldTime = level.getDayTime();
        var source = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        var dispatcher = server.getCommands().getDispatcher();
        try {
            level.setDayTime(6000);
            dispatcher.execute("poptartcore eclipse natural off", source);
            dispatcher.execute("poptartcore eclipse natural on", source);
            h.assertTrue(
                    data.state() instanceof EclipseState.Waiting waiting
                            && waiting.day() == 0
                            && waiting.clearDaysRemaining() >= 20
                            && waiting.clearDaysRemaining() <= 30,
                    "Enabling natural mode chooses a rare clear period");
            var initial = (EclipseState.Waiting) data.state();
            dispatcher.execute("time add 12000", source);
            NeoForge.EVENT_BUS.post(new LevelTickEvent.Post(() -> true, level));
            h.assertTrue(
                    data.state().equals(new EclipseState.Waiting(1, initial.clearDaysRemaining() - 1)),
                    "The real midnight tick decrements the wait without starting an early eclipse");
            var waiting = data.state();
            dispatcher.execute("poptartcore eclipse natural on", source);
            h.assertTrue(data.state().equals(waiting), "Repeated enabling does not reroll the wait");
            var current = new EclipseState.Daylight(EclipseState.Daily.dayAt(level.getDayTime()));
            data.setState(current);
            EclipseEvents.sync(server);
            dispatcher.execute("poptartcore eclipse natural on", source);
            h.assertTrue(data.state().equals(current), "Repeated enabling also preserves active progress");
            dispatcher.execute("poptartcore eclipse phase total", source);
            level.setDayTime(18000 + 24000 * 50);
            NeoForge.EVENT_BUS.post(new LevelTickEvent.Post(() -> true, level));
            h.assertTrue(
                    data.state() instanceof EclipseState.Preview
                            && data.phaseAt(level.getGameTime(), level.getDayTime()) == EclipsePhase.TOTAL,
                    "Manual previews pause natural progression");
            dispatcher.execute("poptartcore eclipse natural on", source);
            dispatcher.execute("poptartcore eclipse start 60", source);
            h.assertTrue(data.state() instanceof EclipseState.Running, "Timed tests also replace natural mode");
            dispatcher.execute("poptartcore eclipse stop", source);
            NeoForge.EVENT_BUS.post(new LevelTickEvent.Post(() -> true, level));
            h.assertTrue(data.state() instanceof EclipseState.Off, "Stop remains a reliable persistent off switch");
            dispatcher.execute("poptartcore eclipse natural on", source);
            dispatcher.execute("poptartcore eclipse natural off", source);
            h.assertTrue(data.state() instanceof EclipseState.Off, "Natural off also clears the active cycle");
        } finally {
            level.setDayTime(oldTime);
            data.setState(prior);
            EclipseEvents.sync(server);
            level.updateSkyBrightness();
        }
        h.succeed();
    }

    private static class NoRolls extends LegacyRandomSource {
        NoRolls() {
            super(0);
        }

        @Override
        public float nextFloat() {
            throw new AssertionError("Daily phases must not roll chances");
        }

        @Override
        public int nextInt(int bound) {
            throw new AssertionError("The saved delay must not be resampled");
        }
    }

    private static final class DelayRoll extends NoRolls {
        private final int value;
        private int calls;

        DelayRoll(int value) {
            this.value = value;
        }

        @Override
        public int nextInt(int bound) {
            if (bound != 11) throw new AssertionError("The inclusive 20-30 range must have eleven possible delays");
            calls++;
            return value;
        }
    }
}
