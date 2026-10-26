package dev.poptartking.poptartcore.mixin.navigation;

import dev.poptartking.poptartcore.navigation.client.hud.CompassHud;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class NavigationUseMixin {
    @Inject(method = "useItem", at = @At("HEAD"))
    private void poptartcore$markCompassUse(
            Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {
        if (player.getItemInHand(hand).is(Items.COMPASS))
            CompassHud.markLocalUse(player.level().getGameTime());
    }
}
