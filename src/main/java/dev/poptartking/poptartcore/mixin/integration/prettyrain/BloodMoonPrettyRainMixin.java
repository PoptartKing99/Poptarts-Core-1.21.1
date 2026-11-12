package dev.poptartking.poptartcore.mixin.integration.prettyrain;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.poptartking.poptartcore.lunar.client.BloodMoonVisuals;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "com.leclowndu93150.particlerain.particle.RainParticle", remap = false)
public abstract class BloodMoonPrettyRainMixin extends TextureSheetParticle {
    protected BloodMoonPrettyRainMixin(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @WrapMethod(method = "render")
    private void poptartcore$drawBloodRain(
            VertexConsumer buffer, Camera camera, float partialTick, Operation<Void> original) {
        float blend = BloodMoonVisuals.rainStrength(partialTick);
        if (blend <= 0) {
            original.call(buffer, camera, partialTick);
            return;
        }

        float red = rCol;
        float green = gCol;
        float blue = bCol;
        setColor(Mth.lerp(blend, red, 1F), Mth.lerp(blend, green, 0.18F), Mth.lerp(blend, blue, 0.09F));
        try {
            original.call(buffer, camera, partialTick);
        } finally {
            setColor(red, green, blue);
        }
    }

    @Override
    protected int getLightColor(float partialTick) {
        int light = super.getLightColor(partialTick);
        // Keep the drops readable against the dark sky and water.
        int minimum = Mth.floor(13F * BloodMoonVisuals.rainStrength(partialTick));
        return LightTexture.block(light) >= minimum ? light : LightTexture.pack(minimum, LightTexture.sky(light));
    }
}
