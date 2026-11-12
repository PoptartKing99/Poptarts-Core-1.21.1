package dev.poptartking.poptartcore.mixin.navigation;

import dev.poptartking.poptartcore.navigation.Lodestones;
import dev.poptartking.poptartcore.navigation.Waypoints;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({CompassItem.class})
public abstract class NavigationCompassItemMixin {
    @Inject(
            method = {"useOn"},
            at = {@At("HEAD")},
            cancellable = true)
    private void poptartcore$bindLodestone(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if ((player == null || !player.isSpectator()) && Lodestones.is(level.getBlockState(pos))) {
            if (player instanceof ServerPlayer serverPlayer) {
                GlobalPos lodestone = GlobalPos.of(level.dimension(), pos);
                if (serverPlayer.isSecondaryUseActive()) {
                    Waypoints.unbindLodestone(context.getItemInHand(), serverPlayer, lodestone);
                } else {
                    Lodestones.bindCompass(context.getItemInHand(), serverPlayer, lodestone);
                }
            }

            cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
        }
    }

    @Inject(
            method = {"isFoil"},
            at = {@At("HEAD")},
            cancellable = true)
    private void poptartcore$noLodestoneGlint(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(
            method = {"getDescriptionId(Lnet/minecraft/world/item/ItemStack;)Ljava/lang/String;"},
            at = {@At("HEAD")},
            cancellable = true)
    private void poptartcore$plainName(ItemStack stack, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(((Item) (Object) this).getDescriptionId());
    }
}
