package dev.poptartking.poptartcore.navigation;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class NavigationMessages {
    private NavigationMessages() {}

    public static void overlay(ServerPlayer player, Component message) {
        player.displayClientMessage(message, true);
    }
}
