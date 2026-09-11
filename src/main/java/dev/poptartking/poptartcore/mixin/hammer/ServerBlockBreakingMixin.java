package dev.poptartking.poptartcore.mixin.hammer;

import dev.poptartking.poptartcore.hammer.BlockBreakProgress;
import dev.poptartking.poptartcore.hammer.HammerMining;
import dev.poptartking.poptartcore.hammer.HammerTarget;
import dev.poptartking.poptartcore.hammer.PersistentMiningMath;
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
    private List<HammerTarget> poptartcore$hammerTargets = List.of();

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
            HammerMining.beginMining(player, pos, face);
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
        float savedFraction = progress.fractionAt(pos);
        if (HammerMining.isAreaMining(player)) {
            for (BlockPos target : HammerMining.findTargets(player, pos)) {
                savedFraction = PersistentMiningMath.leastProgress(savedFraction, progress.fractionAt(target));
            }
        }
        progress.beginAttempt(player.getUUID(), pos, savedFraction);
        float rate = level.getBlockState(pos).getDestroyProgress(player, level, pos);
        int resumedStart = PersistentMiningMath.resumedStart(
                savedFraction, rate, mining.poptartcore$getGameTicks(), mining.poptartcore$getDestroyProgressStart());
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
        progress.updateAttempt(player.getUUID(), pos, fraction);
        progress.record(pos, fraction, rate, areaMining, level.getGameTime());

        if (!areaMining) {
            return;
        }

        for (BlockPos target : HammerMining.findTargets(player, pos)) {
            if (!target.equals(pos)) {
                progress.accrue(target, rate, rate, true, level.getGameTime());
            }
        }
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void poptartcore$captureHammerTargets(BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        if (!poptartcore$breakingHammerTargets) {
            poptartcore$hammerTargets = HammerMining.targetsForBreak(player, pos);
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
            poptartcore$hammerTargets = List.of();
            return;
        }

        List<HammerTarget> targets = poptartcore$hammerTargets;
        poptartcore$breakingHammerTargets = true;
        try {
            ServerPlayerGameMode gameMode = (ServerPlayerGameMode) (Object) this;
            for (HammerTarget target : targets) {
                if (!target.pos().equals(pos)
                        && player.getMainHandItem().is(PoptartCoreTags.HAMMERS)
                        && HammerMining.canBreakTarget(player, target)) {
                    gameMode.destroyBlock(target.pos());
                }
            }
        } finally {
            HammerMining.endMining(player);
            poptartcore$breakingHammerTargets = false;
            poptartcore$hammerTargets = List.of();
        }
    }
}
