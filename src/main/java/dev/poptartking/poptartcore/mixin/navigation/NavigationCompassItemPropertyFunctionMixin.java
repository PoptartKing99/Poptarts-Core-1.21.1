package dev.poptartking.poptartcore.mixin.navigation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.poptartking.poptartcore.navigation.CompassWaypoints;
import dev.poptartking.poptartcore.navigation.Waypoint;
import dev.poptartking.poptartcore.navigation.Waypoints;
import dev.poptartking.poptartcore.navigation.client.ClientWaypoints;
import dev.poptartking.poptartcore.navigation.client.hud.CompassHud;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction.CompassTarget;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({CompassItemPropertyFunction.class})
public abstract class NavigationCompassItemPropertyFunctionMixin {
    @WrapOperation(
            method = {"getCompassRotation"},
            at = {
                @At(
                        value = "INVOKE",
                        target =
                                "Lnet/minecraft/client/renderer/item/CompassItemPropertyFunction$CompassTarget;getPos(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/core/GlobalPos;")
            })
    private GlobalPos poptartcore$aimAtWaypoint(
            CompassTarget target, ClientLevel level, ItemStack stack, Entity entity, Operation<GlobalPos> original) {
        if (stack.is(Items.RECOVERY_COMPASS) && entity instanceof net.minecraft.world.entity.player.Player player) {
            return dev.poptartking.poptartcore.navigation.CorpseDeathMarkers.deathTarget(player, true)
                    .orElse(null);
        }
        Waypoint aim = poptartcore$getAim(stack);
        return aim != null
                ? ClientWaypoints.getTarget(level, aim.pos())
                : (GlobalPos) original.call(new Object[] {target, level, stack, entity});
    }

    @Unique
    @Nullable
    private static Waypoint poptartcore$getAim(ItemStack stack) {
        if (!stack.is(Items.COMPASS)) {
            return null;
        } else {
            CompassWaypoints waypoints = Waypoints.get(stack);
            if (waypoints.isEmpty()) {
                return null;
            } else {
                Waypoint live = waypoints.get(CompassHud.getLiveHighlight(stack));
                return live != null ? live : waypoints.getSelected();
            }
        }
    }
}
