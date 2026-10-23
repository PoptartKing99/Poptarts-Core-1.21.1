package dev.poptartking.poptartcore.mixin.equipment;

import dev.poptartking.poptartcore.equipment.AccessorySlot;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class AccessoryCreativePacketMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleSetCreativeModeSlot", at = @At("HEAD"), cancellable = true)
    private void poptartcore$creativeEquipment(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
        int index = packet.slotNum();
        if (index == 6) {
            PacketUtils.ensureRunningOnSameThread(
                    packet, (ServerGamePacketListenerImpl) (Object) this, player.serverLevel());
            if (!dev.poptartking.poptartcore.integration.create.BackSlotBacktank.canEquipChest(
                            player, packet.itemStack())
                    || !dev.poptartking.poptartcore.integration.reliablebackpacks.BackSlotBackpack.canEquipChest(
                            player, packet.itemStack())) {
                ci.cancel();
                player.inventoryMenu.sendAllDataToRemote();
                return;
            }
        }
        if (index < 46 || index >= player.inventoryMenu.slots.size()) return;
        Slot slot = player.inventoryMenu.slots.get(index);
        if (!(slot instanceof AccessorySlot)) return;
        PacketUtils.ensureRunningOnSameThread(
                packet, (ServerGamePacketListenerImpl) (Object) this, player.serverLevel());
        ci.cancel();
        ItemStack stack = packet.itemStack();
        if (player.gameMode.isCreative()
                && (stack.isEmpty()
                        || (stack.getCount() > 0
                                && stack.getCount() <= slot.getMaxStackSize(stack)
                                && slot.mayPlace(stack)
                                && stack.isItemEnabled(player.level().enabledFeatures())))) {
            slot.setByPlayer(stack.copy());
            player.inventoryMenu.broadcastChanges();
        } else {
            player.inventoryMenu.sendAllDataToRemote();
        }
    }
}
