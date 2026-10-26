package dev.poptartking.poptartcore.mixin.spyglass;

import dev.poptartking.poptartcore.spyglass.client.SpyglassZoom;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin({GameRenderer.class})
public abstract class SpyglassMinimumFovMixin {
    @ModifyConstant(
            method = {"tickFov"},
            constant = {@Constant(floatValue = 0.1F)})
    private float poptartcore$allowDeeperZoom(float minimum) {
        return SpyglassZoom.getMinimumModifier(minimum);
    }
}
