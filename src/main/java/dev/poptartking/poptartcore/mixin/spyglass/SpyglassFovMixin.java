package dev.poptartking.poptartcore.mixin.spyglass;

import dev.poptartking.poptartcore.spyglass.client.SpyglassZoom;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin({AbstractClientPlayer.class})
public abstract class SpyglassFovMixin {
    @ModifyConstant(
            method = {"getFieldOfViewModifier"},
            constant = {@Constant(floatValue = 0.1F)})
    private float poptartcore$stepSpyglassZoom(float zoom) {
        return SpyglassZoom.getModifier((AbstractClientPlayer) (Object) this, zoom);
    }
}
