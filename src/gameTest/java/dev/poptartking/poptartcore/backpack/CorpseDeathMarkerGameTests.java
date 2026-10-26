package dev.poptartking.poptartcore.backpack;

import com.farcr.ragdoll.content.entity.CorpseEntity;
import com.farcr.ragdoll.util.RagdollDataHolder;
import com.mojang.authlib.GameProfile;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.navigation.CorpseDeathMarkers;
import dev.poptartking.poptartcore.navigation.DestroyedCorpses;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CorpseDeathMarkerGameTests {
    private static ServerPlayer player(GameTestHelper h) {
        var cookie =
                CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "corpse-marker-test"), false);
        var player = new ServerPlayer(
                h.getLevel().getServer(), h.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        new ServerGamePacketListenerImpl(
                h.getLevel().getServer(), new Connection(PacketFlow.SERVERBOUND), player, cookie) {
            @Override
            public void send(Packet<?> packet) {}
        };
        return player;
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void selfOnlyCommandClearsLegacyMarkerWithoutOperatorPermission(GameTestHelper h) {
        var player = player(h);
        var id = UUID.randomUUID();
        ((RagdollDataHolder) player).setRagdoll$boundCorpse(id);
        player.setLastDeathLocation(Optional.of(GlobalPos.of(h.getLevel().dimension(), player.blockPosition())));
        var source = player.createCommandSourceStack().withPermission(0).withSuppressedOutput();
        player.server.getCommands().performPrefixedCommand(source, "cleardeathmarker");
        h.assertTrue(
                CorpseDeathMarkers.deathTarget(player, true).isEmpty(),
                "Non-operator can clear their existing stale marker");
        h.assertTrue(
                DestroyedCorpses.get(player.server).contains(id), "Manual dismissal persists for that corpse identity");
        h.assertTrue(player.getLastDeathLocation().isPresent(), "Command preserves death history");
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void destroyingLatestCorpseClearsMarkerWithoutErasingDeathHistory(GameTestHelper h) {
        var player = player(h);
        var corpse = CorpseEntity.createPlayerRagdoll(player, false);
        ((RagdollDataHolder) player).setRagdoll$boundCorpse(corpse.getUUID());
        var death = GlobalPos.of(h.getLevel().dimension(), corpse.blockPosition());
        player.setLastDeathLocation(Optional.of(death));
        CorpseDeathMarkers.sync(player);
        h.assertTrue(
                !player.getData(PoptartCoreAttachments.CORPSE_DEATH_MARKER_REMOVED),
                "Live corpse marker stays visible");
        corpse.remove(Entity.RemovalReason.KILLED);
        CorpseDeathMarkers.sync(player);
        h.assertTrue(
                player.getData(PoptartCoreAttachments.CORPSE_DEATH_MARKER_REMOVED),
                "Actual corpse removal hides marker");
        h.assertTrue(
                CorpseDeathMarkers.deathTarget(player, true).isEmpty(), "HUD has no target for the removed corpse");
        h.assertTrue(player.getLastDeathLocation().equals(Optional.of(death)), "Vanilla death history remains intact");
        var data = DestroyedCorpses.get(player.server);
        var saved = data.save(new net.minecraft.nbt.CompoundTag(), h.getLevel().registryAccess());
        h.assertTrue(
                DestroyedCorpses.load(saved, h.getLevel().registryAccess()).contains(corpse.getUUID()),
                "Removal survives world save and reload");
        h.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 40)
    public static void chunkUnloadOlderCorpsesAndNewDeathsDoNotClearCurrentMarker(GameTestHelper h) {
        var player = player(h);
        var unloaded = CorpseEntity.createPlayerRagdoll(player, false);
        ((RagdollDataHolder) player).setRagdoll$boundCorpse(unloaded.getUUID());
        unloaded.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        CorpseDeathMarkers.sync(player);
        h.assertTrue(
                !player.getData(PoptartCoreAttachments.CORPSE_DEATH_MARKER_REMOVED),
                "Chunk unloading does not count as corpse destruction");
        var old = CorpseEntity.createPlayerRagdoll(player, false);
        var latest = CorpseEntity.createPlayerRagdoll(player, false);
        ((RagdollDataHolder) player).setRagdoll$boundCorpse(latest.getUUID());
        old.remove(Entity.RemovalReason.DISCARDED);
        CorpseDeathMarkers.sync(player);
        h.assertTrue(
                !player.getData(PoptartCoreAttachments.CORPSE_DEATH_MARKER_REMOVED),
                "Destroying old corpse does not hide latest death");
        latest.remove(Entity.RemovalReason.DISCARDED);
        CorpseDeathMarkers.sync(player);
        h.assertTrue(
                player.getData(PoptartCoreAttachments.CORPSE_DEATH_MARKER_REMOVED),
                "Discarded latest corpse clears marker");
        ((RagdollDataHolder) player).setRagdoll$boundCorpse(UUID.randomUUID());
        CorpseDeathMarkers.sync(player);
        h.assertTrue(
                !player.getData(PoptartCoreAttachments.CORPSE_DEATH_MARKER_REMOVED),
                "Next death restores marker even at same position");
        h.succeed();
    }
}
