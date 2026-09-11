package dev.poptartking.poptartcore.millstone.client;

import dev.poptartking.poptartcore.millstone.MillstoneBlockEntity;
import dev.poptartking.poptartcore.millstone.MillstoneRotorBlockEntity;
import dev.poptartking.poptartcore.millstone.MillstoneStructure;
import dev.poptartking.poptartcore.registry.PoptartCoreSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MillstoneLoopSound extends AbstractTickableSoundInstance {
    private final BlockPos controllerPos;
    private final net.minecraft.world.level.Level level;

    public MillstoneLoopSound(net.minecraft.world.level.Level level, BlockPos controllerPos) {
        super(PoptartCoreSounds.MILLSTONE_LOOP.get(), SoundSource.BLOCKS, RandomSource.create());
        this.controllerPos = controllerPos;
        this.level = level;
        this.looping = true;
        this.delay = 0;
        this.x = controllerPos.getX() + 0.5;
        this.y = controllerPos.getY() + 1.0;
        this.z = controllerPos.getZ() + 0.5;
        this.pitch = 1.0F;
        this.volume = volumeFor(this.currentSpeed());
    }

    private static float volumeFor(float speed) {
        return Mth.clamp(0.6F + speed / 128.0F, 0.6F, 1.0F);
    }

    private float currentSpeed() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != level || !level.hasChunkAt(controllerPos)) {
            return 0.0F;
        } else if (!(minecraft.level.getBlockEntity(this.controllerPos) instanceof MillstoneBlockEntity)) {
            return 0.0F;
        } else {
            BlockPos rotorPos = this.controllerPos.offset(MillstoneStructure.ROTOR_OFFSET);
            if (minecraft.level.getBlockEntity(rotorPos) instanceof MillstoneRotorBlockEntity rotor) {
                return rotor.isOverspeed() ? 0.0F : Math.abs(rotor.getSpeed());
            } else {
                return 0.0F;
            }
        }
    }

    public void tick() {
        float speed = this.currentSpeed();
        if (speed == 0.0F) {
            this.stop();
        } else {
            this.volume = volumeFor(speed);
        }
    }
}
