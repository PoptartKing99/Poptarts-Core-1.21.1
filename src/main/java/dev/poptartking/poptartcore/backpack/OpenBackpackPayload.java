package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public record OpenBackpackPayload() implements CustomPacketPayload {
    public static final OpenBackpackPayload INSTANCE = new OpenBackpackPayload();
    public static final Type<OpenBackpackPayload> TYPE = new Type<>(PoptartCore.location("open_backpack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenBackpackPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<OpenBackpackPayload> type() {
        return TYPE;
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(PoptartCore.MOD_ID)
                .playToServer(
                        TYPE,
                        CODEC,
                        (payload, context) -> context.enqueueWork(() -> {
                            if (context.player() instanceof ServerPlayer player
                                    && player.isAlive()
                                    && !player.isSpectator()
                                    && player.containerMenu == player.inventoryMenu) {
                                BackpackMenu.open(
                                        player,
                                        AccessoryInventory.get(player).getStackInSlot(AccessoryInventory.BACK_SLOT));
                            }
                        }));
    }
}
