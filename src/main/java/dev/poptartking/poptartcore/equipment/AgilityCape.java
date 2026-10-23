package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID)
public final class AgilityCape {
    public static final int SLOT = 2;

    private AgilityCape() {}

    public static boolean equipped(Player player) {
        return texture(player) != null;
    }

    public static ResourceLocation texture(Player player) {
        return texture(
                player.level().isClientSide
                        ? player.getData(PoptartCoreAttachments.CAPE_VISUAL)
                        : AccessoryInventory.get(player).getStackInSlot(SLOT));
    }

    public static ResourceLocation texture(ItemStack stack) {
        if (!CapeColors.isCape(stack)) return null;
        return PoptartCore.location(
                "textures/entity/cape/" + CapeColors.color(stack).getName() + ".png");
    }

    @SubscribeEvent
    public static void equipOnRightClick(PlayerInteractEvent.RightClickItem event) {
        var player = event.getEntity();
        var held = event.getItemStack();
        if (texture(held) == null || !AccessoryType.CAPE.accepts(held)) return;
        var inventory = AccessoryInventory.get(player);
        if (!player.isAlive()
                || player.isSpectator()
                || !inventory.getStackInSlot(SLOT).isEmpty()) return;
        if (!player.level().isClientSide) {
            new PlayerAccessoryHandler(player).setStackInSlot(SLOT, held.copyWithCount(1));
            held.consume(1, player);
            sync(new PlayerTickEvent.Post(player));
            player.inventoryMenu.broadcastChanges();
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide));
    }

    @SubscribeEvent
    public static void sync(PlayerTickEvent.Post event) {
        var player = event.getEntity();
        if (player.level().isClientSide) return;
        var stack = AccessoryInventory.get(player).getStackInSlot(SLOT);
        var visual = texture(stack) == null ? ItemStack.EMPTY : stack.copyWithCount(1);
        if (!ItemStack.matches(visual, player.getData(PoptartCoreAttachments.CAPE_VISUAL)))
            player.setData(PoptartCoreAttachments.CAPE_VISUAL, visual);
    }
}
