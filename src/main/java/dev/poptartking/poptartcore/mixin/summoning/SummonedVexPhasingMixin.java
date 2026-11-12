package dev.poptartking.poptartcore.mixin.summoning;

import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import dev.poptartking.poptartcore.summoning.PhasingMover;
import dev.poptartking.poptartcore.summoning.PixieSummons;
import dev.poptartking.poptartcore.summoning.PixieType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class SummonedVexPhasingMixin {
    @Inject(method = "customServerAiStep", at = @At("HEAD"))
    private void poptartcore$vexPhasing(CallbackInfo ci) {
        var self = (Mob) (Object) this;
        if (!(self instanceof Vex vex) || !PixieSummons.isSummoned(vex) || !(vex.level() instanceof ServerLevel level))
            return;
        var phase = vex.getData(PoptartCoreAttachments.PHASE_STATE);
        if (phase.getState() == 0) vex.noPhysics = false;
        var control = vex.getMoveControl();
        if (control.hasWanted() || phase.getState() != 0) {
            var wanted = new Vec3(control.getWantedX(), control.getWantedY(), control.getWantedZ());
            PhasingMover.moveToward(level, vex, phase, wanted, control.getSpeedModifier(), PixieType.VEX);
        }
    }
}
