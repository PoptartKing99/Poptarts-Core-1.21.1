package dev.poptartking.poptartcore.spider;

import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import dev.poptartking.poptartcore.registry.PoptartCoreEntities;
import dev.poptartking.poptartcore.registry.PoptartCoreSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.event.EventHooks;

public final class WebProjectile extends ThrowableItemProjectile {
    public WebProjectile(EntityType<? extends WebProjectile> type, Level level) {
        super(type, level);
    }

    public WebProjectile(Level level, LivingEntity shooter) {
        super(PoptartCoreEntities.WEB_PROJECTILE.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COBWEB;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.06;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return !(entity instanceof Spider) && super.canHitEntity(entity);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && (tickCount >= 200 || isInWaterOrBubble())) {
            discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        BlockPos pos = hit.getBlockPos();
        if (!level().getBlockState(pos).canBeReplaced()) {
            pos = pos.relative(hit.getDirection());
        }
        placeWebAndDiscard(pos);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        placeWebAndDiscard(hit.getEntity().blockPosition());
    }

    private void placeWebAndDiscard(BlockPos pos) {
        if (level().isClientSide) {
            return;
        }
        playSound(PoptartCoreSounds.SPIDER_WEB_IMPACT.get(), 1, 1);
        if (EventHooks.canEntityGrief(level(), getOwner())
                && level().hasChunkAt(pos)
                && level().isInWorldBounds(pos)
                && level().getWorldBorder().isWithinBounds(pos)) {
            BlockState state = level().getBlockState(pos);
            if (state.canBeReplaced() && state.getFluidState().isEmpty() && !state.hasBlockEntity()) {
                boolean placed = level().setBlock(
                                pos, PoptartCoreBlocks.TEMPORARY_COBWEB.get().defaultBlockState(), 3);
                if (placed && getOwner() instanceof Mob owner && owner instanceof WebShootingSpider spider) {
                    LivingEntity target = owner.getTarget();
                    if (target != null && target.getBoundingBox().intersects(new AABB(pos))) {
                        spider.poptartcore$onWebCatch(target);
                        // Notify once around the shooter; recipients do not relay the alert.
                        for (Spider nearby : level().getEntitiesOfClass(
                                        Spider.class,
                                        owner.getBoundingBox().inflate(10),
                                        candidate -> candidate != owner
                                                && candidate.getType() == EntityType.SPIDER
                                                && candidate.isAlive()
                                                && candidate.getTarget() == target
                                                && candidate.distanceToSqr(owner) <= 100)) {
                            ((WebShootingSpider) nearby).poptartcore$onWebCatch(target);
                        }
                    }
                }
            }
        }
        discard();
    }
}
