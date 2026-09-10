package dev.poptartking.poptartcore.mixin.hammer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayerGameMode.class)
public interface GameModeDestroyAccessor {
    @Accessor("isDestroyingBlock")
    boolean poptartcore$isDestroyingBlock();

    @Accessor("isDestroyingBlock")
    void poptartcore$setDestroyingBlock(boolean value);

    @Accessor("destroyPos")
    BlockPos poptartcore$getDestroyPos();

    @Accessor("gameTicks")
    int poptartcore$getGameTicks();

    @Accessor("destroyProgressStart")
    int poptartcore$getDestroyProgressStart();

    @Accessor("destroyProgressStart")
    void poptartcore$setDestroyProgressStart(int value);

    @Accessor("hasDelayedDestroy")
    void poptartcore$setDelayedDestroy(boolean value);
}
