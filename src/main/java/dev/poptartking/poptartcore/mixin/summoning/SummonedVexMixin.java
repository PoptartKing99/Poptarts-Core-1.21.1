package dev.poptartking.poptartcore.mixin.summoning;

import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import dev.poptartking.poptartcore.summoning.PixieSummons;
import dev.poptartking.poptartcore.summoning.PixieType;
import dev.poptartking.poptartcore.summoning.VexOwnerTargetGoal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Vex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Vex.class)
public abstract class SummonedVexMixin {
    @Unique
    private boolean poptartcore$targetsInstalled;

    @Inject(method = "tick", at = @At("HEAD"))
    private void poptartcore$summonTick(CallbackInfo ci) {
        var self = (Vex) (Object) this;
        if (!PixieSummons.isSummoned(self) || !(self.level() instanceof ServerLevel level)) return;
        if (!poptartcore$targetsInstalled) {
            self.targetSelector.removeAllGoals(goal -> true);
            self.targetSelector.addGoal(1, new VexOwnerTargetGoal(self));
            poptartcore$targetsInstalled = true;
        }
        var data = self.getData(PoptartCoreAttachments.VEX_DATA);
        var owner = VexOwnerTargetGoal.resolveOwner(self);
        if (owner != null && !PixieSummons.isRegistered(self, owner, data)) {
            PixieSummons.remove(level, self);
            return;
        }
        if (!data.hasOwner()) {
            PixieSummons.tickOwnerlessLifespan(level, self, data, PixieType.VEX);
            return;
        }
        var target = self.getTarget();
        if (target != null && target.getUUID().equals(data.getOwnerUuid())) {
            self.setTarget(null);
            target = null;
        }
        boolean working = owner != null
                && ((target != null && target.isAlive()) || VexOwnerTargetGoal.findTarget(self, owner) != null);
        data.setIdleTicks(working ? 0 : data.getIdleTicks() + 1);
        if (data.getIdleTicks() > 200) PixieSummons.remove(level, self);
    }
}
