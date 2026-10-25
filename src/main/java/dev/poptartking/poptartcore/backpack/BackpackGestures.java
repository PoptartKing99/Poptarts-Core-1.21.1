package dev.poptartking.poptartcore.backpack;

import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.equipment.PlayerAccessoryHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class BackpackGestures {
    private BackpackGestures() {}

    public static boolean place(Player player, BlockHitResult hit) {
        if (!canInteract(player, hit.getBlockPos()) || !player.getOffhandItem().isEmpty()) return false;
        var accessories = new PlayerAccessoryHandler(player);
        var worn = accessories.getStackInSlot(AccessoryInventory.BACK_SLOT);
        if (!(worn.getItem() instanceof BackpackItem item)) return false;
        var level = player.level();
        var context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, worn.copy(), hit);
        var target = context.getClickedPos();
        if (!level.mayInteract(player, target)
                || !level.getBlockState(target).canBeReplaced(context)
                || !level.getBlockState(target.below()).isFaceSturdy(level, target.below(), Direction.UP)) return false;
        var snapshot = net.neoforged.neoforge.common.util.BlockSnapshot.create(level.dimension(), level, target);
        var snapshots = new java.util.ArrayList<net.neoforged.neoforge.common.util.BlockSnapshot>();
        snapshots.add(snapshot);
        if (item.getBlock() instanceof RucksackBlock) {
            if (!level.mayInteract(player, target.above())) return false;
            snapshots.add(
                    net.neoforged.neoforge.common.util.BlockSnapshot.create(level.dimension(), level, target.above()));
        }
        // Vanilla placement applies the item's container component; publish the ordinary protection event too.
        if (!item.place(context).consumesAction()) return false;
        BlockEvent.EntityPlaceEvent placement = snapshots.size() == 1
                ? new BlockEvent.EntityPlaceEvent(snapshot, level.getBlockState(hit.getBlockPos()), player)
                : new BlockEvent.EntityMultiPlaceEvent(snapshots, level.getBlockState(hit.getBlockPos()), player);
        if (NeoForge.EVENT_BUS.post(placement).isCanceled()) {
            for (var captured : snapshots) captured.restore();
            return false;
        }
        accessories.setStackInSlot(AccessoryInventory.BACK_SLOT, ItemStack.EMPTY);
        player.inventoryMenu.broadcastChanges();
        return true;
    }

    public static boolean pickUp(Player player, BlockPos pos) {
        if (!canInteract(player, pos)) return false;
        var level = player.level();
        if (level.getBlockState(pos).getBlock() instanceof BackpackBlock block) {
            pos = block.origin(level.getBlockState(pos), pos);
        }
        var accessories = new PlayerAccessoryHandler(player);
        if (!accessories.getStackInSlot(AccessoryInventory.BACK_SLOT).isEmpty()
                || !(level.getBlockEntity(pos) instanceof BackpackBlockEntity backpack)) return false;
        // A placed container must not be removed while another player is changing its contents.
        for (var other : level.players()) {
            if (other.containerMenu instanceof BackpackMenu menu && menu.getContainer() == backpack) return false;
        }
        ItemStack stack = new ItemStack(level.getBlockState(pos).getBlock());
        stack.applyComponents(backpack.collectComponents());
        if (!accessories.isItemValid(AccessoryInventory.BACK_SLOT, stack)) return false;
        if (NeoForge.EVENT_BUS
                .post(new BlockEvent.BreakEvent(level, pos, level.getBlockState(pos), player))
                .isCanceled()) return false;
        if (!level.removeBlock(pos, false)) return false;
        accessories.setStackInSlot(AccessoryInventory.BACK_SLOT, stack);
        player.inventoryMenu.broadcastChanges();
        return true;
    }

    private static boolean canInteract(Player player, BlockPos pos) {
        return !player.level().isClientSide
                && player.isAlive()
                && !player.isSpectator()
                && player.isShiftKeyDown()
                && player.getMainHandItem().isEmpty()
                && player.containerMenu == player.inventoryMenu
                && player.canInteractWithBlock(pos, 0)
                && player.mayBuild()
                && player.level().mayInteract(player, pos);
    }
}
