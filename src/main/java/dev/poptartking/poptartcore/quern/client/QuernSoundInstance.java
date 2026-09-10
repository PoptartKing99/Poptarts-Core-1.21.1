package dev.poptartking.poptartcore.quern.client;

import dev.poptartking.poptartcore.quern.QuernBlockEntity;
import dev.poptartking.poptartcore.registry.PoptartCoreSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class QuernSoundInstance extends AbstractTickableSoundInstance {
    private final QuernBlockEntity quern;

    public QuernSoundInstance(QuernBlockEntity quern) {
        super(PoptartCoreSounds.QUERN.get(), SoundSource.BLOCKS, RandomSource.create());
        this.quern = quern;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.x = quern.getBlockPos().getX() + 0.5;
        this.y = quern.getBlockPos().getY() + 0.5;
        this.z = quern.getBlockPos().getZ() + 0.5;
    }

    @Override
    public void tick() {
        if (quern.isRemoved() || quern.getLevel() == null || (!quern.isRotating() && !isHoldingUseOnQuern())) {
            stop();
        }
    }

    private boolean isHoldingUseOnQuern() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.options.keyUse.isDown()
                && minecraft.hitResult instanceof BlockHitResult hit
                && hit.getBlockPos().equals(quern.getBlockPos())
                && quern.canCrank();
    }
}
