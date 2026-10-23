package dev.poptartking.poptartcore.mixin.integration.ragdoll;

import com.farcr.ragdoll.api.inventory.CorpseSlots;
import com.farcr.ragdoll.content.entity.CorpseEntity;
import com.farcr.ragdoll.screen.CorpseMenu;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.integration.ragdoll.AccessoryCorpseProvider;
import dev.poptartking.poptartcore.integration.ragdoll.CorpseAccessorySlot;
import dev.poptartking.poptartcore.integration.ragdoll.CorpseInventoryLayout;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CorpseMenu.class)
public abstract class CorpseAccessoryMenuMixin extends AbstractContainerMenu {
    protected CorpseAccessoryMenuMixin(MenuType<?> type, int id) {
        super(type, id);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void poptartcore$addSlots(
            LivingEntity entity, int id, Inventory inventory, Container container, CallbackInfo ci) {
        CorpseInventoryLayout.alignVanillaSlots(this);
        int base = CorpseSlots.baseSlot(AccessoryCorpseProvider.ID);
        if (base < 0 || container.getContainerSize() < base + AccessoryInventory.SIZE) return;
        for (int i = 0; i < AccessoryInventory.SIZE; i++)
            addSlot(new CorpseAccessorySlot(
                    container, base, i, entity instanceof CorpseEntity, entity == null, inventory.player));
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void poptartcore$quickMove(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index < 0 || index >= slots.size() || !(slots.get(index) instanceof CorpseAccessorySlot source)) return;
        cir.setReturnValue(ItemStack.EMPTY);
        if (!source.hasItem() || !source.mayPickup(player)) return;
        ItemStack stack = source.getItem();
        ItemStack original = stack.copy();
        // Match Ragdoll's curse handling and only move into the player's 36 regular slots.
        CorpseMenu.pickupItem(player, stack);
        if (moveItemStackTo(stack, 41, 77, true)) {
            if (stack.isEmpty()) source.set(ItemStack.EMPTY);
            else source.setChanged();
            source.onTake(player, stack);
            cir.setReturnValue(original);
        }
    }
}
