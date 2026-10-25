package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public record BackpackGesturePayload(BlockPos pos, boolean pickUp) implements CustomPacketPayload {
    public static final Type<BackpackGesturePayload> TYPE = new Type<>(PoptartCore.location("backpack_gesture"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BackpackGesturePayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            BackpackGesturePayload::pos,
            ByteBufCodecs.BOOL,
            BackpackGesturePayload::pickUp,
            BackpackGesturePayload::new);

    @Override
    public Type<BackpackGesturePayload> type() {
        return TYPE;
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(PoptartCore.MOD_ID)
                .playToServer(
                        TYPE,
                        CODEC,
                        (payload, context) -> context.enqueueWork(() -> {
                            var player = context.player();
                            var target = player.pick(player.blockInteractionRange(), 1, false);
                            if (target.getType() != HitResult.Type.BLOCK
                                    || !(target instanceof BlockHitResult hit)
                                    || !hit.getBlockPos().equals(payload.pos)) return;
                            if (payload.pickUp) BackpackGestures.pickUp(player, payload.pos);
                            else BackpackGestures.place(player, hit);
                        }));
    }
}
