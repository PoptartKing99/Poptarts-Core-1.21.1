package dev.poptartking.poptartcore.millstone;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.poptartking.poptartcore.registry.PoptartCoreBlockEntities;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MillstoneRotorBlockEntity extends KineticBlockEntity {
    public static final float SPEED_LIMIT = 64.0F;
    public static final float STRESS_IMPACT = 16.0F;
    public float angle;
    public float prevAngle;

    public MillstoneRotorBlockEntity(BlockPos pos, BlockState state) {
        super(PoptartCoreBlockEntities.MILLSTONE_ROTOR.get(), pos, state);
    }

    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
    }

    public float calculateStressApplied() {
        this.lastStressApplied = 16.0F;
        return 16.0F;
    }

    public boolean isOverspeed() {
        return Math.abs(this.getSpeed()) > 64.0F;
    }

    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (this.isOverspeed()) {
            tooltip.add(Component.literal("    ")
                    .append(Component.translatable("poptartcore.millstone.too_fast")
                            .withStyle(ChatFormatting.RED)));
            added = true;
        }

        return added;
    }

    public void tick() {
        super.tick();
        if (this.level != null && this.level.isClientSide) {
            this.prevAngle = this.angle;
            if (this.isOverspeed()) {
                this.spawnOverspeedParticles();
            } else {
                this.angle = this.angle + this.getSpeed() * 3.0F / 10.0F;
                if (this.angle >= 360.0F) {
                    this.angle -= 360.0F;
                    this.prevAngle -= 360.0F;
                }

                if (this.angle <= -360.0F) {
                    this.angle += 360.0F;
                    this.prevAngle += 360.0F;
                }
            }
        }
    }

    private void spawnOverspeedParticles() {
        Vec3 center = this.getBlockPos().getCenter();
        if (this.level.random.nextFloat() < 0.6F) {
            double angle = this.level.random.nextDouble() * Math.PI * 2.0;
            double radius = 0.35 + this.level.random.nextDouble() * 0.25;
            this.level.addParticle(
                    ParticleTypes.CRIT,
                    center.x + Math.cos(angle) * radius,
                    center.y + 0.08 + this.level.random.nextDouble() * 0.5,
                    center.z + Math.sin(angle) * radius,
                    Math.cos(angle) * 0.05,
                    0.04,
                    Math.sin(angle) * 0.05);
        }

        for (int i = 0; i < 2; i++) {
            if (!(this.level.random.nextFloat() > 0.7F)) {
                double angle = this.level.random.nextDouble() * Math.PI * 2.0;
                double radius = 1.52 + this.level.random.nextDouble() * 0.18;
                this.level.addParticle(
                        ParticleTypes.CRIT,
                        center.x + Math.cos(angle) * radius,
                        center.y - 0.45 + this.level.random.nextDouble() * 0.4,
                        center.z + Math.sin(angle) * radius,
                        Math.cos(angle) * 0.12,
                        0.03,
                        Math.sin(angle) * 0.12);
            }
        }
    }
}
