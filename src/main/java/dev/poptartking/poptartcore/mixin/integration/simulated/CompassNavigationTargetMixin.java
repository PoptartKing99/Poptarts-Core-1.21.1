package dev.poptartking.poptartcore.mixin.integration.simulated;

import dev.poptartking.poptartcore.navigation.Waypoints;
import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.navigation_targets.CompassNavigationTarget;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(CompassNavigationTarget.class)
public abstract class CompassNavigationTargetMixin {
    @Inject(method = "getTarget", at = @At("HEAD"), cancellable = true)
    private void poptartcore$useSelectedWaypoint(
            NavTableBlockEntity table, ItemStack compass, CallbackInfoReturnable<Vec3> ci) {
        var selected = Waypoints.get(compass).getSelected();
        if (selected != null && table.getLevel() instanceof ServerLevel level) {
            ci.setReturnValue(Waypoints.locate(level, selected));
        }
    }
}
