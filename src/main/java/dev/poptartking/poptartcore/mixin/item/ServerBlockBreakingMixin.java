package dev.poptartking.poptartcore.mixin.item;

import dev.poptartking.poptartcore.hammer.BlockBreakProgress;
import dev.poptartking.poptartcore.hammer.HammerMining;
import dev.poptartking.poptartcore.registry.PoptartCoreTags;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerBlockBreakingMixin {
    @Shadow
    protected ServerLevel level;

    @Shadow
    protected final ServerPlayer player;

    @Unique
    private boolean poptartcore$breakingHammerTargets;

    @Unique
    private List<BlockPos> poptartcore$hammerTargets = List.of();

    protected ServerBlockBreakingMixin(ServerPlayer player) {
        this.player = player;
    }

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
    private void poptartcore$trackHammerMining(
            BlockPos pos,
            ServerboundPlayerActionPacket.Action action,
            Direction face,
            int maxBuildHeight,
            int sequence,
            CallbackInfo callback) {
        if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            HammerMining.beginMining(player, face);
        } else if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
            BlockBreakProgress.get(level).endAttempt(player.getUUID());
            HammerMining.endMining(player);
        }
    }

    @Inject(method = "handleBlockBreakAction", at = @At("RETURN"))
    private void poptartcore$resumePersistentDamage(
            BlockPos pos,
            ServerboundPlayerActionPacket.Action action,
            Direction face,
            int maxBuildHeight,
            int sequence,
            CallbackInfo callback) {
        if (action != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            return;
        }

        GameModeDestroyAccessor mining = (GameModeDestroyAccessor) this;
        if (!mining.poptartcore$isDestroyingBlock() || !pos.equals(mining.poptartcore$getDestroyPos())) {
            return;
        }

        BlockBreakProgress progress = BlockBreakProgress.get(level);
        progress.beginAttempt(player.getUUID(), pos);
        float rate = level.getBlockState(pos).getDestroyProgress(player, level, pos);
        int resumedStart = progress.resumedStart(
                pos, rate, mining.poptartcore$getGameTicks(), mining.poptartcore$getDestroyProgressStart());
        mining.poptartcore$setDestroyProgressStart(resumedStart);
    }

    @Inject(method = "incrementDestroyProgress", at = @At("RETURN"))
    private void poptartcore$recordPersistentDamage(
            net.minecraft.world.level.block.state.BlockState state,
            BlockPos pos,
            int startTick,
            CallbackInfoReturnable<Float> callback) {
        BlockBreakProgress progress = BlockBreakProgress.get(level);
        float rate = state.getDestroyProgress(player, level, pos);
        float fraction = callback.getReturnValue();
        boolean areaMining = HammerMining.isAreaMining(player);
        progress.record(pos, fraction, rate, areaMining, level.getGameTime());

        if (!areaMining) {
            poptartcore$hammerTargets = List.of();
            return;
        }

        poptartcore$hammerTargets = HammerMining.findTargets(player, level, pos);
        for (BlockPos target : poptartcore$hammerTargets) {
            if (!target.equals(pos)) {
                progress.record(target, fraction, rate, true, level.getGameTime());
            }
        }
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void poptartcore$captureHammerTargets(BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        if (!poptartcore$breakingHammerTargets) {
            poptartcore$hammerTargets = HammerMining.findTargets(player, level, pos);
        }
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void poptartcore$breakHammerTargets(BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        BlockBreakProgress progress = BlockBreakProgress.get(level);
        progress.endAttempt(player.getUUID());
        progress.clear(pos);
        if (poptartcore$breakingHammerTargets) {
            return;
        }

        if (!callback.getReturnValue()) {
            HammerMining.endMining(player);
            return;
        }

        List<BlockPos> targets = poptartcore$hammerTargets;
        HammerMining.endMining(player);
        poptartcore$breakingHammerTargets = true;
        try {
            ServerPlayerGameMode gameMode = (ServerPlayerGameMode) (Object) this;
            for (BlockPos target : targets) {
                if (!target.equals(pos) && player.getMainHandItem().is(PoptartCoreTags.HAMMERS)) {
                    progress.clear(target);
                    gameMode.destroyBlock(target);
                }
            }
        } finally {
            poptartcore$breakingHammerTargets = false;
            poptartcore$hammerTargets = List.of();
        }
    }
}
