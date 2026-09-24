package dev.poptartking.poptartcore.mixin.integration.ragdoll;

import com.farcr.ragdoll.content.entity.CorpseEntity;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.poptartking.poptartcore.lostheart.LostHeartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Minecraft.class)
public abstract class LostHeartAttackTargetMixin {
    @WrapMethod(method = "startAttack")
    private boolean poptartcore$targetHeartInsteadOfCorpse(Operation<Boolean> original) {
        Minecraft minecraft = (Minecraft) (Object) this;
        HitResult previous = minecraft.hitResult;
        if (!(previous instanceof EntityHitResult entityHit)
                || !(entityHit.getEntity() instanceof CorpseEntity corpse)
                || minecraft.player == null
                || minecraft.level == null
                || !minecraft.player.getUUID().equals(corpse.getOwnerUUID())) {
            return original.call();
        }

        var hearts = minecraft.level.getEntitiesOfClass(LostHeartEntity.class,
                corpse.getBoundingBox().inflate(4), heart -> heart.corpseId() == corpse.getId());
        if (hearts.isEmpty()) {
            return original.call();
        }

        Vec3 eye = minecraft.player.getEyePosition(1.0F);
        Vec3 end = eye.add(minecraft.player.getViewVector(1.0F)
                .scale(minecraft.player.entityInteractionRange()));
        BlockHitResult blockHit = minecraft.level.clip(new ClipContext(
                eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, minecraft.player));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        LostHeartEntity targeted = null;
        Vec3 targetPoint = null;
        double nearest = Double.MAX_VALUE;
        for (LostHeartEntity heart : hearts) {
            var intersection = heart.getBoundingBox().clip(eye, end);
            if (intersection.isPresent()) {
                Vec3 point = intersection.get();
                double distance = eye.distanceToSqr(point);
                if (distance < nearest) {
                    nearest = distance;
                    targeted = heart;
                    targetPoint = point;
                }
            }
        }

        minecraft.hitResult = targeted == null
                ? BlockHitResult.miss(end, Direction.getNearest(end.subtract(eye)), BlockPos.containing(end))
                : new EntityHitResult(targeted, targetPoint);
        try {
            return original.call();
        } finally {
            minecraft.hitResult = previous;
        }
    }
}
