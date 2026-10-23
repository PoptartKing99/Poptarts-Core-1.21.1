package dev.poptartking.poptartcore.mixin.anvil;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.poptartking.poptartcore.anvil.AnvilMenu;
import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.extensions.IItemStackExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemStack.class})
public abstract class WornUpgradeMixin implements IItemStackExtension {
    public boolean canPerformAction(ItemAbility ability) {
        ItemStack stack = (ItemStack) (Object) this;
        return !AnvilMenu.isWornOut(stack) && stack.getItem().canPerformAction(stack, ability);
    }

    @ModifyVariable(
            method = {"setDamageValue"},
            at = @At("HEAD"),
            argsOnly = true)
    private int poptartcore$upgradedNeverBreak(int damage) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!AnvilMenu.isUpgraded(stack)) {
            return damage;
        } else {
            int max = stack.getMaxDamage();
            return max > 1 ? Math.min(damage, max - 1) : damage;
        }
    }

    @ModifyExpressionValue(
            method = {
                "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"
            },
            at = {@At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxDamage()I")})
    private int poptartcore$upgradedSurvivesTheBreakCheck(int original) {
        return AnvilMenu.isUpgraded((ItemStack) (Object) this) ? Integer.MAX_VALUE : original;
    }

    @ModifyReturnValue(
            method = {"getDestroySpeed"},
            at = {@At("RETURN")})
    private float poptartcore$wornOutMinesLikeAFist(float original) {
        return AnvilMenu.isWornOut((ItemStack) (Object) this) ? 1.0F : original;
    }

    @ModifyReturnValue(
            method = {"isCorrectToolForDrops"},
            at = {@At("RETURN")})
    private boolean poptartcore$wornOutDropsNothing(boolean original) {
        return original && !AnvilMenu.isWornOut((ItemStack) (Object) this);
    }

    @Inject(
            method = {"forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"},
            at = {@At("HEAD")},
            cancellable = true)
    private void poptartcore$wornOutGivesNothing(
            EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> consumer, CallbackInfo ci) {
        if (AnvilMenu.isWornOut((ItemStack) (Object) this)) {
            ci.cancel();
        }
    }
}
