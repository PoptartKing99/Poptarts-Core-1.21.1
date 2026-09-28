package dev.poptartking.poptartcore.lostheart;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class HeartsCommand {
    private HeartsCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher()
                .register(Commands.literal("poptartcore")
                        .requires(source -> source.hasPermission(4))
                        .then(Commands.literal("hearts")
                                .then(Commands.literal("get")
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(context ->
                                                        get(context, EntityArgument.getPlayer(context, "target")))))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 512))
                                                        .executes(context -> set(
                                                                context,
                                                                EntityArgument.getPlayers(context, "targets"),
                                                                IntegerArgumentType.getInteger(context, "amount"))))))
                                .then(Commands.literal("add")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .then(Commands.argument(
                                                                "amount", IntegerArgumentType.integer(-512, 512))
                                                        .executes(context -> add(
                                                                context,
                                                                EntityArgument.getPlayers(context, "targets"),
                                                                IntegerArgumentType.getInteger(context, "amount"))))))
                                .then(Commands.literal("reset")
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(context -> set(
                                                        context,
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        Hearts.MAX))))));
    }

    private static int get(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        int hearts = Hearts.get(player);
        context.getSource()
                .sendSuccess(
                        () -> Component.literal(player.getName().getString() + " has " + hearts + " hearts."), false);
        return hearts;
    }

    private static int set(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, int amount) {
        for (ServerPlayer player : players) {
            player.setData(PoptartCoreAttachments.MAX_HEARTS.get(), amount);
            Hearts.apply(player);
        }
        int count = players.size();
        context.getSource()
                .sendSuccess(
                        () -> Component.literal(
                                "Set hearts to " + amount + " for " + count + (count == 1 ? " player." : " players.")),
                        true);
        return count;
    }

    private static int add(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, int delta) {
        for (ServerPlayer player : players) {
            player.setData(PoptartCoreAttachments.MAX_HEARTS.get(), Math.max(1, Hearts.get(player) + delta));
            Hearts.apply(player);
        }
        int count = players.size();
        context.getSource()
                .sendSuccess(
                        () -> Component.literal("Adjusted hearts by " + delta + " for " + count
                                + (count == 1 ? " player." : " players.")),
                        true);
        return count;
    }
}
