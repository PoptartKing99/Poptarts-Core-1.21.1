package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** The stand owns one saved, synced cape, independently of its vanilla armor slots. */
public final class ArmorStandCape {
    private ArmorStandCape() {}

    public static ItemStack get(ArmorStand stand) {
        return stand.getData(PoptartCoreAttachments.ARMOR_STAND_CAPE);
    }

    public static InteractionResult interact(ArmorStand stand, Player player, InteractionHand hand) {
        if (stand.isRemoved() || stand.isMarker() || player.isSpectator()) return InteractionResult.PASS;
        var held = player.getItemInHand(hand);
        var cape = get(stand);
        boolean taking = player.isShiftKeyDown() && held.isEmpty() && !cape.isEmpty();
        if (!taking && !CapeColors.isCape(held)) return InteractionResult.PASS;
        if (!player.getAbilities().mayBuild || !stand.level().mayInteract(player, stand.blockPosition()))
            return InteractionResult.FAIL;
        if (!taking && !cape.isEmpty()) return InteractionResult.FAIL;
        if (!stand.level().isClientSide) {
            if (taking) {
                stand.setData(PoptartCoreAttachments.ARMOR_STAND_CAPE, ItemStack.EMPTY);
                player.setItemInHand(hand, cape);
            } else {
                stand.setData(PoptartCoreAttachments.ARMOR_STAND_CAPE, held.copyWithCount(1));
                held.consume(1, player);
            }
        }
        return InteractionResult.sidedSuccess(stand.level().isClientSide);
    }

    public static void drop(ArmorStand stand) {
        if (stand.level().isClientSide) return;
        var cape = get(stand);
        if (cape.isEmpty()) return;
        stand.setData(PoptartCoreAttachments.ARMOR_STAND_CAPE, ItemStack.EMPTY);
        stand.spawnAtLocation(cape);
    }
}
