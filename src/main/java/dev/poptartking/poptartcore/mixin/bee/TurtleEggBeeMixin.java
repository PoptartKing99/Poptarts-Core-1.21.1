package dev.poptartking.poptartcore.mixin.bee;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TurtleEggBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TurtleEggBlock.class)
public class TurtleEggBeeMixin {
    @ModifyReturnValue(method = "canDestroyEgg", at = @At("RETURN"))
    private boolean poptartcore$beesSpareEggs(boolean original, Level level, Entity entity) {
        return original && !(entity instanceof Bee);
    }
}
