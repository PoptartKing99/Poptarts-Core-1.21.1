package dev.poptartking.poptartcore.mixin.equipment;

import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class AccessoryDeathDropMixin {
    @Inject(method = "dropEquipment", at = @At("TAIL"))
    private void poptartcore$dropAccessories(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!player.level().isClientSide && !player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            AccessoryInventory.dropAll(player);
        }
    }
}
