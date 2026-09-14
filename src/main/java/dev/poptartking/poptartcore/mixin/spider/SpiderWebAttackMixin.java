package dev.poptartking.poptartcore.mixin.spider;

import dev.poptartking.poptartcore.spider.SpiderWebAttackGoal;
import dev.poptartking.poptartcore.spider.WebShootingSpider;
import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Spider.class)
public abstract class SpiderWebAttackMixin extends Monster implements WebShootingSpider {
    @Unique
    private static final EntityDataAccessor<Boolean> POPTARTCORE_WEB_SHOOTING =
            SynchedEntityData.defineId(Spider.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private UUID poptartcore$webbedTarget;

    @Unique
    private long poptartcore$meleeUntil;

    protected SpiderWebAttackMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void poptartcore$defineWebState(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(POPTARTCORE_WEB_SHOOTING, false);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void poptartcore$addWebAttack(CallbackInfo ci) {
        if (getType() == EntityType.SPIDER) {
            goalSelector.addGoal(3, new SpiderWebAttackGoal((Spider) (Object) this));
        }
    }

    @Override
    public boolean poptartcore$isWebShooting() {
        return entityData.get(POPTARTCORE_WEB_SHOOTING);
    }

    @Override
    public void poptartcore$setWebShooting(boolean shooting) {
        entityData.set(POPTARTCORE_WEB_SHOOTING, shooting);
    }

    @Override
    public void poptartcore$onWebCatch(LivingEntity target) {
        if (!level().isClientSide && getTarget() == target) {
            poptartcore$webbedTarget = target.getUUID();
            poptartcore$meleeUntil = level().getGameTime() + 100;
            poptartcore$setWebShooting(false);
        }
    }

    @Override
    public boolean poptartcore$isPursuingWebbedTarget(LivingEntity target) {
        return target.getUUID().equals(poptartcore$webbedTarget) && level().getGameTime() < poptartcore$meleeUntil;
    }
}
