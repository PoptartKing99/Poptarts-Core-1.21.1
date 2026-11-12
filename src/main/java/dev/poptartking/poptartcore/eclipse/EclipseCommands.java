package dev.poptartking.poptartcore.eclipse;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.poptartking.poptartcore.PoptartCore;
import java.util.Arrays;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class EclipseCommands {
    private static final int DEFAULT_SECONDS = 180;
    private static final SimpleCommandExceptionType UNKNOWN_PHASE = new SimpleCommandExceptionType(
            Component.literal("Unknown eclipse phase. Use tab completion to choose a phase."));

    private EclipseCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher()
                .register(Commands.literal("poptartcore")
                        .requires(source -> source.hasPermission(4))
                        .then(Commands.literal("eclipse")
                                .executes(context -> status(context.getSource()))
                                .then(Commands.literal("start")
                                        .executes(context -> start(context.getSource(), DEFAULT_SECONDS))
                                        .then(Commands.argument("seconds", IntegerArgumentType.integer(1, 3600))
                                                .executes(context -> start(
                                                        context.getSource(),
                                                        IntegerArgumentType.getInteger(context, "seconds")))))
                                .then(Commands.literal("stop")
                                        .executes(context -> change(
                                                context.getSource(),
                                                new EclipseState.Off(),
                                                "Eclipse stopped. Normal visuals restored.")))
                                .then(Commands.literal("natural")
                                        .then(Commands.literal("on")
                                                .executes(context -> enableNatural(context.getSource())))
                                        .then(Commands.literal("off")
                                                .executes(context -> change(
                                                        context.getSource(),
                                                        new EclipseState.Off(),
                                                        "Natural eclipses disabled. Normal visuals restored."))))
                                .then(Commands.literal("phase")
                                        .then(Commands.argument("phase", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                        Arrays.stream(EclipsePhase.values())
                                                                .map(EclipsePhase::id),
                                                        builder))
                                                .executes(context -> {
                                                    EclipsePhase phase = EclipsePhase.byName(
                                                                    StringArgumentType.getString(context, "phase"))
                                                            .orElseThrow(UNKNOWN_PHASE::create);
                                                    EclipseState state = phase == EclipsePhase.CLEAR
                                                            ? new EclipseState.Off()
                                                            : new EclipseState.Preview(phase);
                                                    return change(
                                                            context.getSource(),
                                                            state,
                                                            "Eclipse phase: " + phase.id()
                                                                    + ". Use /poptartcore eclipse stop to clear it.");
                                                })))));
    }

    private static int enableNatural(CommandSourceStack source) {
        var state = EclipseData.get(source.getServer()).state();
        return change(
                source,
                state instanceof EclipseState.Daily
                        ? state
                        : EclipseState.Waiting.start(
                                source.getServer().overworld().getDayTime(),
                                source.getServer().overworld().getRandom()),
                "Natural eclipses enabled. Totality occurs around noon, with 20-30 clear days between events.");
    }

    private static int start(CommandSourceStack source, int seconds) {
        return change(
                source,
                new EclipseState.Running(source.getServer().overworld().getGameTime(), seconds * 20),
                "Started a " + seconds + " second eclipse in the Overworld.");
    }

    private static int change(CommandSourceStack source, EclipseState state, String message) {
        EclipseData.get(source.getServer()).setState(state);
        EclipseEvents.sync(source.getServer());
        source.sendSuccess(() -> Component.literal(message), true);
        return 1;
    }

    private static int status(CommandSourceStack source) {
        EclipseData data = EclipseData.get(source.getServer());
        long now = source.getServer().overworld().getGameTime();
        long dayTime = source.getServer().overworld().getDayTime();
        String mode =
                switch (data.state()) {
                    case EclipseState.Off ignored -> "off";
                    case EclipseState.Preview ignored -> "held preview";
                    case EclipseState.Daylight ignored -> "natural event, total at noon and clear at sunset";
                    case EclipseState.Waiting waiting ->
                        "natural cycle, " + waiting.clearDaysRemaining() + " clear days until the next eclipse";
                    case EclipseState.Running running ->
                        "running, "
                                + Math.max(0, (running.durationTicks() - (now - running.startedAt()) + 19) / 20)
                                + " seconds remaining";
                };
        source.sendSuccess(
                () -> Component.literal("Eclipse: " + data.phaseAt(now, dayTime).id() + " (" + mode
                        + "). Commands: natural on/off, start [seconds], phase <phase>, stop."),
                false);
        return 1;
    }
}
