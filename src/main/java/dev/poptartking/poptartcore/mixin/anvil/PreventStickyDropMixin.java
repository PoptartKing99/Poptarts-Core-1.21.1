package dev.poptartking.poptartcore.mixin.anvil;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.poptartking.poptartcore.anvil.AnvilMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({LocalPlayer.class})
public abstract class PreventStickyDropMixin {
    @Shadow
    public abstract void displayClientMessage(Component var1, boolean var2);

    @WrapOperation(
            method = {"drop"},
            at = {
                @At(
                        value = "INVOKE",
                        target =
                                "Lnet/minecraft/world/entity/player/Inventory;removeFromSelected(Z)Lnet/minecraft/world/item/ItemStack;")
            })
    private ItemStack poptartcore$checkIfSticky(
            Inventory instance, boolean removeStack, Operation<ItemStack> original) {
        ItemStack stack = instance.getSelected();
        if (AnvilMenu.hasModification(stack, "sticky")) {
            this.displayClientMessage(
                    Component.translatable("message.poptartcore.sticky").withStyle(ChatFormatting.GRAY), true);
            return stack;
        } else {
            return (ItemStack) original.call(new Object[] {instance, removeStack});
        }
    }
}
