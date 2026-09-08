package dev.poptartking.poptartcore.mixin.item;

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
public abstract class ServerHammerMiningMixin {
    @Shadow
    protected ServerLevel level;

    @Shadow
    protected final ServerPlayer player;

    @Unique
    private boolean poptartcore$breakingHammerTargets;

    @Unique
    private List<BlockPos> poptartcore$hammerTargets = List.of();

    protected ServerHammerMiningMixin(ServerPlayer player) {
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
            poptartcore$clearHammerCracks();
            HammerMining.beginMining(player, face);
        } else if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
            poptartcore$clearHammerCracks();
            HammerMining.endMining(player);
        }
    }

    @Inject(method = "incrementDestroyProgress", at = @At("RETURN"))
    private void poptartcore$mirrorHammerCracks(
            net.minecraft.world.level.block.state.BlockState state,
            BlockPos pos,
            int startTick,
            CallbackInfoReturnable<Float> callback) {
        if (!HammerMining.isAreaMining(player)) {
            poptartcore$clearHammerCracks();
            return;
        }

        poptartcore$hammerTargets = HammerMining.findTargets(player, level, pos);
        int stage = Math.min(9, (int) (callback.getReturnValue() * 10.0F));
        for (BlockPos target : poptartcore$hammerTargets) {
            if (!target.equals(pos)) {
                level.destroyBlockProgress(HammerMining.crackId(target), target, stage);
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
        if (poptartcore$breakingHammerTargets) {
            return;
        }

        if (!callback.getReturnValue()) {
            poptartcore$clearHammerCracks();
            HammerMining.endMining(player);
            return;
        }

        List<BlockPos> targets = poptartcore$hammerTargets;
        poptartcore$clearHammerCracks();
        HammerMining.endMining(player);
        poptartcore$breakingHammerTargets = true;
        try {
            ServerPlayerGameMode gameMode = (ServerPlayerGameMode) (Object) this;
            for (BlockPos target : targets) {
                if (!target.equals(pos) && player.getMainHandItem().is(PoptartCoreTags.HAMMERS)) {
                    gameMode.destroyBlock(target);
                }
            }
        } finally {
            poptartcore$breakingHammerTargets = false;
            poptartcore$hammerTargets = List.of();
        }
    }

    @Unique
    private void poptartcore$clearHammerCracks() {
        for (BlockPos target : poptartcore$hammerTargets) {
            level.destroyBlockProgress(HammerMining.crackId(target), target, -1);
        }
        poptartcore$hammerTargets = List.of();
    }
}
