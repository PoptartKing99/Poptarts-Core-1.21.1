package dev.poptartking.poptartcore.mixin.eclipse;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.poptartking.poptartcore.eclipse.EclipseDaylight;
import dev.poptartking.poptartcore.eclipse.EclipseLighting;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Level.class)
public abstract class EclipseDaylightMixin implements EclipseDaylight {
    @Shadow
    private int skyDarken;

    @Unique
    private int poptartcore$skyDarkenWithoutEclipse;

    @WrapMethod(method = "updateSkyBrightness")
    private void poptartcore$eclipseDaylight(Operation<Void> original) {
        original.call();
        poptartcore$skyDarkenWithoutEclipse = skyDarken;
        skyDarken = Math.max(skyDarken, (int) EclipseLighting.darkness((Level) (Object) this));
    }

    @Override
    public int poptartcore$skyDarkenWithoutEclipse() {
        return poptartcore$skyDarkenWithoutEclipse;
    }
}
