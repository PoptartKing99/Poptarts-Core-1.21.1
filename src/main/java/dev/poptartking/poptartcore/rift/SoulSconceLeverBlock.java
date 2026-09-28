package dev.poptartking.poptartcore.rift;

import net.mehvahdjukaar.supplementaries.common.block.blocks.SconceLeverBlock;
import net.minecraft.core.particles.ParticleTypes;

public class SoulSconceLeverBlock extends SconceLeverBlock {
    public SoulSconceLeverBlock(Properties properties) {
        super(properties, () -> ParticleTypes.SOUL_FIRE_FLAME);
    }
}
