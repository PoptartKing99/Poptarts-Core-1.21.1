package dev.poptartking.poptartcore.navigation;

import com.farcr.ragdoll.content.entity.CorpseEntity;
import com.farcr.ragdoll.util.RagdollDataHolder;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class CorpseDeathMarkers {
    private CorpseDeathMarkers() {}

    public static void removed(CorpseEntity corpse, Entity.RemovalReason reason) {
        if (corpse.level().isClientSide || corpse.isOffline() || !reason.shouldDestroy()) return;
        var server = corpse.level().getServer();
        DestroyedCorpses.get(server).record(corpse.getUUID());
        var owner =
                corpse.getOwnerUUID() == null ? null : server.getPlayerList().getPlayer(corpse.getOwnerUUID());
        if (owner != null) sync(owner);
    }

    public static java.util.Optional<net.minecraft.core.GlobalPos> deathTarget(
            net.minecraft.world.entity.player.Player player, boolean recovery) {
        return recovery && !player.getData(PoptartCoreAttachments.CORPSE_DEATH_MARKER_REMOVED)
                ? player.getLastDeathLocation()
                : java.util.Optional.empty();
    }

    public static boolean dismissCurrent(ServerPlayer player) {
        var corpse = ((RagdollDataHolder) player).getRagdoll$boundCorpse();
        if (corpse == null || player.getLastDeathLocation().isEmpty()) return false;
        DestroyedCorpses.get(player.server).record(corpse);
        sync(player);
        return true;
    }

    @SubscribeEvent
    public static void commands(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        event.getDispatcher()
                .register(net.minecraft.commands.Commands.literal("cleardeathmarker")
                        .requires(source -> source.getEntity() instanceof ServerPlayer)
                        .executes(context -> {
                            var player = context.getSource().getPlayerOrException();
                            if (!dismissCurrent(player)) {
                                context.getSource()
                                        .sendFailure(net.minecraft.network.chat.Component.translatable(
                                                "poptartcore.navigation.no_death_marker"));
                                return 0;
                            }
                            context.getSource()
                                    .sendSuccess(
                                            () -> net.minecraft.network.chat.Component.translatable(
                                                    "poptartcore.navigation.death_marker_cleared"),
                                            false);
                            return 1;
                        }));
    }

    public static void sync(ServerPlayer player) {
        var corpse = ((RagdollDataHolder) player).getRagdoll$boundCorpse();
        boolean removed = DestroyedCorpses.get(player.server).contains(corpse);
        if (player.getData(PoptartCoreAttachments.CORPSE_DEATH_MARKER_REMOVED) != removed)
            player.setData(PoptartCoreAttachments.CORPSE_DEATH_MARKER_REMOVED, removed);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 10 == 0) sync(player);
    }
}
