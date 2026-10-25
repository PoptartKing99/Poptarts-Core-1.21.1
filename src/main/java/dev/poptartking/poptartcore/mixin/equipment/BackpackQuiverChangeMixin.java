package dev.poptartking.poptartcore.mixin.equipment;

import dev.poptartking.poptartcore.backpack.BackpackAttachmentEffects;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class BackpackQuiverChangeMixin {
    @Inject(method = "set", at = @At("RETURN"))
    private <T> void poptartcore$quiverChanged(DataComponentType<T> type, T value, CallbackInfoReturnable<T> cir) {
        BackpackAttachmentEffects.itemChanged((ItemStack) (Object) this);
    }
}
