package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.equipment.PlayerAccessoryHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class BackpackItem extends BlockItem {
    public BackpackItem(Block block, Properties properties) {
        super(block, properties.stacksTo(1));
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    public static boolean equip(Player player, ItemStack stack) {
        var accessories = new PlayerAccessoryHandler(player);
        int slot = AccessoryInventory.BACK_SLOT;
        if (player.isSpectator()
                || !player.isAlive()
                || !accessories.getStackInSlot(slot).isEmpty()
                || !accessories.isItemValid(slot, stack)) return false;
        accessories.setStackInSlot(slot, stack.copyWithCount(1));
        stack.shrink(1);
        player.inventoryMenu.broadcastChanges();
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSpectator()) return InteractionResultHolder.pass(stack);
        if (!level.isClientSide) {
            if (player.isSecondaryUseActive()
                    || !AccessoryInventory.get(player)
                            .getStackInSlot(AccessoryInventory.BACK_SLOT)
                            .isEmpty()) {
                BackpackMenu.open((ServerPlayer) player, stack);
            } else {
                equip(player, stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return context.getPlayer() != null
                        && !context.getPlayer().isSpectator()
                        && context.getPlayer().isSecondaryUseActive()
                ? super.useOn(context)
                : InteractionResult.PASS;
    }
}
