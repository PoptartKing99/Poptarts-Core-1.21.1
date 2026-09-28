package dev.poptartking.poptartcore.rift;

import com.teamabnormals.caverns_and_chasms.core.registry.CCParticleTypes;
import net.mehvahdjukaar.supplementaries.common.block.blocks.SconceLeverBlock;

public class CupricSconceLeverBlock extends SconceLeverBlock {
    public CupricSconceLeverBlock(Properties properties) {
        super(properties, CCParticleTypes.CUPRIC_FIRE_FLAME::get);
    }
}
