package dev.poptartking.poptartcore.mixin.navigation;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.poptartking.poptartcore.navigation.sable.SableCompat;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper.AssemblyTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin({SubLevelAssemblyHelper.class})
public abstract class NavigationSableAssemblyMixin {
    @WrapMethod(method = {"moveBlocks"})
    private static void poptartcore$keepLodestones(
            ServerLevel level, AssemblyTransform transform, Iterable<BlockPos> blocks, Operation<Void> original) {
        SableCompat.moveBlocks(level, transform, blocks, () -> original.call(new Object[] {level, transform, blocks}));
    }
}
