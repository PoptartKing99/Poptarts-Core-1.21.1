package dev.poptartking.poptartcore.mixin.bee;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.poptartking.poptartcore.beekeeping.SmokedHives;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeehiveBlock.class)
public class BeehiveSmokedMixin {
    @WrapOperation(
            method = "useItemOn",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/block/CampfireBlock;isSmokeyPos(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean poptartcore$smokerCalmsHive(Level level, BlockPos pos, Operation<Boolean> original) {
        return original.call(level, pos) || SmokedHives.isSmoked(level, pos);
    }
}
