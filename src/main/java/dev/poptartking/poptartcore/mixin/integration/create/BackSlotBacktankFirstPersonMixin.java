package dev.poptartking.poptartcore.mixin.integration.create;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.equipment.armor.NetheriteBacktankFirstPersonRenderer;
import dev.poptartking.poptartcore.integration.create.BackSlotBacktank;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NetheriteBacktankFirstPersonRenderer.class)
public abstract class BackSlotBacktankFirstPersonMixin {
    @ModifyExpressionValue(
            method = "clientTick",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/player/LocalPlayer;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack poptartcore$backtankSleeves(ItemStack original) {
        return BackSlotBacktank.visualChestStack(Minecraft.getInstance().player, original);
    }
}
