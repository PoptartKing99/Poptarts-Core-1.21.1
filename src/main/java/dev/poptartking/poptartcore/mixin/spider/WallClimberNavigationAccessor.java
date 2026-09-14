package dev.poptartking.poptartcore.mixin.spider;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WallClimberNavigation.class)
public interface WallClimberNavigationAccessor {
    @Accessor("pathToPosition")
    void poptartcore$setClimbingTarget(@Nullable BlockPos target);
}
