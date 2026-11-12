package dev.poptartking.poptartcore.lunar;

import com.mojang.brigadier.context.CommandContext;
import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class BloodMoonCommands {
    private BloodMoonCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher()
                .register(Commands.literal("poptartcore")
                        .then(Commands.literal("blood_moon")
                                .requires(source -> source.hasPermission(4))
                                .executes(BloodMoonCommands::status)
                                .then(Commands.literal("start").executes(BloodMoonCommands::start))
                                .then(Commands.literal("stop").executes(BloodMoonCommands::stop))
                                .then(Commands.literal("natural")
                                        .then(Commands.literal("on").executes(context -> natural(context, true)))
                                        .then(Commands.literal("off").executes(context -> natural(context, false))))));
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        var level = context.getSource().getServer().overworld();
        var data = BloodMoonData.get(level);
        long time = level.getDayTime();
        String state = data.isBloodMoon(time)
                ? (level.isRaining() ? "waiting for clear skies" : "active")
                : data.scheduled(time) ? "scheduled for tonight" : "clear";
        context.getSource()
                .sendSuccess(
                        () -> Component.literal("Blood Moon: " + state + ". Natural events "
                                + (data.natural() ? "enabled." : "disabled.")),
                        false);
        return 1;
    }

    private static int start(CommandContext<CommandSourceStack> context) {
        var level = context.getSource().getServer().overworld();
        var data = BloodMoonData.get(level);
        data.start(level.getDayTime());
        BloodMoonEvents.sync(level);
        return reply(
                context,
                data.isBloodMoon(level.getDayTime())
                        ? "Blood Moon selected for the rest of tonight."
                        : "Blood Moon scheduled for tonight.");
    }

    private static int stop(CommandContext<CommandSourceStack> context) {
        var level = context.getSource().getServer().overworld();
        BloodMoonData.get(level).stop(level.getDayTime());
        BloodMoonEvents.sync(level);
        return reply(context, "Tonight's Blood Moon has been cleared.");
    }

    private static int natural(CommandContext<CommandSourceStack> context, boolean enabled) {
        var level = context.getSource().getServer().overworld();
        BloodMoonData.get(level).setNatural(enabled, level.getDayTime());
        BloodMoonEvents.sync(level);
        return reply(context, enabled ? "Natural Blood Moons enabled." : "Natural Blood Moons disabled.");
    }

    private static int reply(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> Component.literal(message), true);
        return 1;
    }
}
