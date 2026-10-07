package dev.poptartking.poptartcore.mixin.conduit;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.poptartking.poptartcore.registry.PoptartCoreSounds;
import dev.poptartking.poptartcore.conduit.ConduitHeartbeatTiming;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConduitBlockEntity.class)
public abstract class ConduitHeartbeatMixin {
    @WrapOperation(
            method = "serverTick",
            at = @At(value = "INVOKE", target =
                    "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private static void poptartcore$heartbeatIntervalByFrameSize(
            Level level, Player player, BlockPos soundPos, SoundEvent sound,
            SoundSource source, float volume, float pitch, Operation<Void> original,
            Level tickLevel, BlockPos conduitPos, BlockState state, ConduitBlockEntity conduit) {
        if (sound != SoundEvents.CONDUIT_AMBIENT || conduit.isHunting()) {
            original.call(level, player, soundPos, sound, source, volume, pitch);
        }
    }

    @Inject(method = "serverTick", at = @At("TAIL"))
    private static void poptartcore$playSingleHeartbeat(Level level, BlockPos pos,
            BlockState state, ConduitBlockEntity conduit, CallbackInfo callback) {
        // The clip lasts 0.8 seconds, followed by one second of silence.
        if (conduit.isActive() && !conduit.isHunting()
                && level.getGameTime() % ConduitHeartbeatTiming.SINGLE_PAIR_INTERVAL_TICKS == 0L) {
            level.playSound(null, pos, PoptartCoreSounds.CONDUIT_HEARTBEAT_SINGLE.get(),
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}
