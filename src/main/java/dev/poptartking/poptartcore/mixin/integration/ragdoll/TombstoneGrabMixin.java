package dev.poptartking.poptartcore.mixin.integration.ragdoll;

import com.farcr.ragdoll.content.entity.CorpseEntity;
import com.farcr.ragdoll.networking.packets.GrabRagdollC2SPacket;
import dev.poptartking.poptartcore.integration.newworld.TombstoneCorpse;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.farcr.ragdoll.networking.RagdollServerPacketHandler", remap = false)
public class TombstoneGrabMixin {
    @Inject(method = "handleGrabRagdollPacket", at = @At("HEAD"), cancellable = true)
    private static void poptartcore$ownerOnlyCarry(
            GrabRagdollC2SPacket packet, @Coerce Object context, CallbackInfo callback) {
        if (packet.limb() == null || packet.limb().isEmpty()) {
            return;
        }
        // Veil's packet-context API is bundled at runtime by Ragdoll, but is not a direct
        // compile dependency of this mod. Its public player() method supplies the sender.
        try {
            Class<?> packetContext = Class.forName("foundry.veil.api.network.handler.ServerPacketContext");
            ServerPlayer player = (ServerPlayer) packetContext.getMethod("player").invoke(context);
            if (player.level().getEntity(packet.entityID()) instanceof CorpseEntity corpse
                    && !TombstoneCorpse.canCarry(player, corpse)) {
                callback.cancel();
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot inspect Ragdoll grab packet sender", e);
        }
    }
}
