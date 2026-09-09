package dev.poptartking.poptartcore.hammer;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.mixin.item.GameModeDestroyAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class BlockBreakingEvents {
    private BlockBreakingEvents() {}

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BlockBreakProgress.get(player.serverLevel()).endAttempt(player.getUUID());
            GameModeDestroyAccessor mining = (GameModeDestroyAccessor) player.gameMode;
            if (mining.poptartcore$isDestroyingBlock()) {
                player.serverLevel().destroyBlockProgress(player.getId(), mining.poptartcore$getDestroyPos(), -1);
            }
            mining.poptartcore$setDestroyingBlock(false);
            mining.poptartcore$setDelayedDestroy(false);
        }
        HammerMining.endMining(event.getEntity());
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockBreakProgress progress = BlockBreakProgress.get(level);
        for (ServerPlayer player : level.players()) {
            GameModeDestroyAccessor mining = (GameModeDestroyAccessor) player.gameMode;
            if (!mining.poptartcore$isDestroyingBlock() || player.getAbilities().instabuild) {
                continue;
            }

            BlockPos pos = mining.poptartcore$getDestroyPos();
            if (progress.isResumedAttempt(player.getUUID(), pos) && progress.fractionAt(pos) >= 1.0F) {
                var state = level.getBlockState(pos);
                progress.clear(pos);
                if (player.gameMode.destroyBlock(pos)) {
                    player.connection.send(new ClientboundLevelEventPacket(2001, pos, Block.getId(state), false));
                }
            }
        }
        progress.tick(level.getGameTime());
    }
}
