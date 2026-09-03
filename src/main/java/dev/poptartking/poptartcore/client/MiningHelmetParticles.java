package dev.poptartking.poptartcore.client;

import dev.poptartking.poptartcore.registry.PoptartCoreItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class MiningHelmetParticles {

    private MiningHelmetParticles() {
    }

    public static void tick(Minecraft minecraft) {
        ClientLevel level = minecraft.level;

        if (level == null || minecraft.isPaused()) {
            return;
        }

        for (Player player : level.players()) {
            spawn(level, player);
        }
    }

    private static void spawn(ClientLevel level, LivingEntity wearer) {
        if (!wearer.isAlive()
                || wearer.isSpectator()
                || !wearer.getItemBySlot(EquipmentSlot.HEAD).is(PoptartCoreItems.MINING_HELMET.get())) {
            return;
        }

        // Only attempt to spawn particles every other tick on average.
        if (level.random.nextBoolean()) {
            return;
        }

        // Head rotation
        double yaw = Math.toRadians(wearer.yHeadRot);
        double pitch = Math.toRadians(wearer.getXRot());

        double sinYaw = Math.sin(yaw);
        double cosYaw = Math.cos(yaw);
        double sinPitch = Math.sin(pitch);
        double cosPitch = Math.cos(pitch);

        // Forward direction
        double forwardX = -sinYaw * cosPitch;
        double forwardY = -sinPitch;
        double forwardZ = cosYaw * cosPitch;

        // Up direction
        double upX = -sinPitch * sinYaw;
        double upY = cosPitch;
        double upZ = sinPitch * cosYaw;

        // Position of the helmet lamp
        Vec3 eye = wearer.getEyePosition();

        double x = eye.x + upX * 0.62D + forwardX * 0.28D;
        double y = eye.y + upY * 0.62D + forwardY * 0.28D;
        double z = eye.z + upZ * 0.62D + forwardZ * 0.28D;

        // Don't spawn flames underwater.
        if (level.getFluidState(BlockPos.containing(x, y, z)).is(FluidTags.WATER)) {
            return;
        }

        // Flame
        if (wearer.getRandom().nextFloat() < 0.5F) {
            level.addParticle(
                    ParticleTypes.SMALL_FLAME,
                    x, y, z,
                    0.0D, 0.0D, 0.0D
            );
        }

        // Smoke
        if (wearer.getRandom().nextFloat() < 0.1F) {
            level.addParticle(
                    ParticleTypes.SMOKE,
                    x, y + 0.05D, z,
                    0.0D, 0.01D, 0.0D
            );
        }
    }
}