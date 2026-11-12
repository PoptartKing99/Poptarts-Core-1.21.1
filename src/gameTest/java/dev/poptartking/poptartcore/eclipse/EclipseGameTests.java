package dev.poptartking.poptartcore.eclipse;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EclipseGameTests {
    @GameTest(template = "millstone_test")
    public static void timedCycleTraversesBothHalvesThenRestoresNormal(GameTestHelper h) {
        var cycle = new EclipseState.Running(100, 140);
        EclipsePhase[] expected = {
            EclipsePhase.WANING_GIBBOUS,
            EclipsePhase.WANING_HALF,
            EclipsePhase.WANING_CRESCENT,
            EclipsePhase.TOTAL,
            EclipsePhase.WAXING_CRESCENT,
            EclipsePhase.WAXING_HALF,
            EclipsePhase.WAXING_GIBBOUS
        };
        for (int i = 0; i < expected.length; i++) {
            h.assertTrue(
                    cycle.phaseAt(100 + i * 20, 6000) == expected[i]
                            && cycle.phaseAt(119 + i * 20, 18000) == expected[i],
                    "Each phase occupies its full time interval");
        }
        h.assertTrue(
                cycle.phaseAt(99, 6000) == expected[0]
                        && cycle.phaseAt(240, 6000) == EclipsePhase.CLEAR
                        && cycle.phaseAt(10_000, 6000) == EclipsePhase.CLEAR,
                "Time boundaries never index outside the atlas");
        var preview = new EclipseState.Preview(EclipsePhase.TOTAL);
        h.assertTrue(
                preview.phaseAt(0, 0) == EclipsePhase.TOTAL
                        && preview.phaseAt(Long.MAX_VALUE, Long.MAX_VALUE) == EclipsePhase.TOTAL,
                "Held previews do not expire");
        h.succeed();
    }

    @GameTest(template = "millstone_test")
    public static void savingPreservesCycleAndInvalidDataStaysOff(GameTestHelper h) {
        var original = new EclipseData();
        original.setState(new EclipseState.Running(100, 140));
        var loaded =
                EclipseData.load(original.save(new CompoundTag(), h.getLevel().registryAccess()));
        h.assertTrue(
                loaded.phaseAt(160, 6000) == EclipsePhase.TOTAL
                        && loaded.phaseAt(239, 18000) == EclipsePhase.WAXING_GIBBOUS,
                "Reloading keeps the original timeline rather than restarting it");
        var random = net.minecraft.util.RandomSource.create(42);
        h.assertTrue(
                loaded.update(160, 6000, random) && !loaded.update(160, 6000, random),
                "Broadcast only actual phase changes");
        h.assertTrue(
                loaded.update(240, 6000, random) && loaded.state() instanceof EclipseState.Off && loaded.isDirty(),
                "Expiry restores clear visuals and saves the off state");
        original.setState(new EclipseState.Preview(EclipsePhase.WANING_HALF));
        loaded = EclipseData.load(original.save(new CompoundTag(), h.getLevel().registryAccess()));
        h.assertTrue(loaded.phaseAt(999_999, 6000) == EclipsePhase.WANING_HALF, "Preview also survives saving");
        var invalid = new CompoundTag();
        invalid.putString("mode", "preview");
        invalid.putString("phase", "old");
        h.assertTrue(
                EclipseData.load(invalid).state() instanceof EclipseState.Off,
                "Old Sun is not an available preview phase");
        invalid.putString("mode", "running");
        invalid.putInt("duration_ticks", -1);
        h.assertTrue(EclipseData.load(invalid).state() instanceof EclipseState.Off, "Invalid durations are discarded");
        h.succeed();
    }

    @GameTest(template = "millstone_test")
    public static void commandsControlVisualStateWithoutChangingWorldTime(GameTestHelper h)
            throws CommandSyntaxException {
        var server = h.getLevel().getServer();
        var data = EclipseData.get(server);
        var prior = data.state();
        var dispatcher = server.getCommands().getDispatcher();
        var source = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        long day = server.overworld().getDayTime();
        try {
            dispatcher.execute("poptartcore eclipse start 7", source);
            h.assertTrue(
                    data.state() instanceof EclipseState.Running running && running.durationTicks() == 140,
                    "Duration is accepted through the registered command");
            dispatcher.execute("poptartcore eclipse phase total", source);
            h.assertTrue(
                    data.phaseAt(
                                    server.overworld().getGameTime(),
                                    server.overworld().getDayTime())
                            == EclipsePhase.TOTAL,
                    "Preview holds totality");
            h.assertTrue(
                    server.overworld().getDayTime() == day
                            && server.overworld().getSkyDarken() >= (int) EclipseLighting.darkness(server.overworld()),
                    "Commands apply gameplay darkness without changing the clock");
            reject(h, () -> dispatcher.execute("poptartcore eclipse start 0", source));
            reject(h, () -> dispatcher.execute("poptartcore eclipse phase old", source));
            reject(h, () -> dispatcher.execute("poptartcore eclipse stop", source.withPermission(0)));
            h.assertTrue(
                    data.phaseAt(
                                    server.overworld().getGameTime(),
                                    server.overworld().getDayTime())
                            == EclipsePhase.TOTAL,
                    "Invalid and unauthorized commands leave the active preview intact");
            dispatcher.execute("poptartcore eclipse stop", source);
            h.assertTrue(data.state() instanceof EclipseState.Off, "Stop disables the event");
            dispatcher.execute("poptartcore eclipse start", source);
            h.assertTrue(
                    data.state() instanceof EclipseState.Running running && running.durationTicks() == 3600,
                    "Bare start defaults to three minutes");
            dispatcher.execute("poptartcore eclipse phase clear", source);
            h.assertTrue(
                    data.phaseAt(
                                    server.overworld().getGameTime(),
                                    server.overworld().getDayTime())
                            == EclipsePhase.CLEAR,
                    "Clear restores vanilla");
        } finally {
            data.setState(prior);
            EclipseEvents.sync(server);
        }
        h.succeed();
    }

    private static void reject(GameTestHelper h, Command command) {
        boolean rejected = false;
        try {
            command.run();
        } catch (CommandSyntaxException expected) {
            rejected = true;
        }
        h.assertTrue(rejected, "The command boundary must reject this input");
    }

    @FunctionalInterface
    private interface Command {
        void run() throws CommandSyntaxException;
    }
}
