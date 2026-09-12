package dev.poptartking.poptartcore.mixin.wax;

import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import net.mehvahdjukaar.moonlight.api.block.IWaxable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(IWaxable.class)
public interface IWaxableMoonlightMixin<T> {
    @Redirect(
            method = "tryWaxingWithItem",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;",
                            ordinal = 0))
    private Item poptartcore$substituteWax(ItemStack stack) {
        if (stack.is(PoptartCoreItems.WAX.get())) {
            return Items.HONEYCOMB;
        }
        return stack.is(Items.HONEYCOMB) ? Items.BARRIER : stack.getItem();
    }

    @Redirect(
            method = "tryWaxingWithItem",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;",
                            ordinal = 1))
    private Item poptartcore$returnWaxItem(ItemStack stack) {
        return PoptartCoreItems.WAX.get();
    }
}
