package dev.poptartking.poptartcore.hammer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** One original block in a swing. Replacements cannot rejoin that swing. */
public final class HammerTarget {
    private final BlockPos pos;
    private final BlockState state;
    private boolean valid = true;

    HammerTarget(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.state = state;
    }

    public BlockPos pos() {
        return pos;
    }

    public BlockState state() {
        return state;
    }

    public boolean isValid() {
        return valid;
    }

    void invalidate() {
        valid = false;
    }
}
