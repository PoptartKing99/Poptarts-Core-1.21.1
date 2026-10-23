package dev.poptartking.poptartcore.mixin.integration.supplementaries;

import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import net.mehvahdjukaar.supplementaries.common.entities.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.common.utils.SlotReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.mehvahdjukaar.supplementaries.common.items.QuiverItem", remap = false)
public abstract class BeltQuiverMixin {
    @Inject(method = "findActiveQuiver", at = @At("HEAD"), cancellable = true)
    private static void poptartcore$beltStack(LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (entity instanceof Player player) {
            var stack = AccessoryInventory.get(player).getStackInSlot(5);
            if (stack.getItem() instanceof QuiverItem) cir.setReturnValue(stack);
        }
    }

    @Inject(method = "findActiveQuiverSlot", at = @At("HEAD"), cancellable = true)
    private static void poptartcore$beltReference(LivingEntity entity, CallbackInfoReturnable<SlotReference> cir) {
        if (entity instanceof Player player
                && AccessoryInventory.get(player).getStackInSlot(5).getItem() instanceof QuiverItem) {
            // Supplementaries' existing serializable quiver reference resolves through findActiveQuiver.
            cir.setReturnValue(SlotReference.quiver((IQuiverEntity) player));
        }
    }
}
