package dev.poptartking.poptartcore.mixin.equipment;

import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.equipment.AccessorySlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryMenu.class)
public abstract class AccessoryInventoryMenuMixin extends AbstractContainerMenu {
    protected AccessoryInventoryMenuMixin(MenuType<?> type, int id) {
        super(type, id);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void poptartcore$addSlots(Inventory inventory, boolean active, Player owner, CallbackInfo ci) {
        ((SlotPositionAccessor) slots.get(0)).poptartcore$setX(134);
        ((SlotPositionAccessor) slots.get(0)).poptartcore$setY(59);
        for (int i = 0; i < 4; i++) {
            var position = (SlotPositionAccessor) slots.get(i + 1);
            position.poptartcore$setX(125 + (i % 2) * 18);
            position.poptartcore$setY(13 + (i / 2) * 18);
        }
        for (int i = 0; i < AccessoryInventory.SIZE; i++) addSlot(new AccessorySlot(owner, i));
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void poptartcore$quickMove(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index < 0 || index >= slots.size()) return;
        Slot source = slots.get(index);
        if (!source.hasItem() || !source.mayPickup(player)) return;
        ItemStack stack = source.getItem();
        ItemStack original = stack.copy();
        boolean moved = false;
        if (source instanceof AccessorySlot) {
            moved = moveItemStackTo(stack, InventoryMenu.INV_SLOT_START, InventoryMenu.USE_ROW_SLOT_END, false);
            if (!moved) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
        } else if (index >= InventoryMenu.INV_SLOT_START && index < InventoryMenu.USE_ROW_SLOT_END) {
            for (Slot target : slots) {
                if (target instanceof AccessorySlot
                        && target.mayPlace(stack)
                        && (!target.hasItem()
                                || target.getMaxStackSize(stack) > 1
                                        && ItemStack.isSameItemSameComponents(target.getItem(), stack))) {
                    moved = moveItemStackTo(stack, target.index, target.index + 1, false);
                    if (moved) break;
                }
            }
        }
        if (moved) {
            if (stack.isEmpty()) source.setByPlayer(ItemStack.EMPTY, original);
            else source.setChanged();
            source.onTake(player, source instanceof AccessorySlot ? original : stack);
            cir.setReturnValue(original);
        }
    }
}
