package dev.poptartking.poptartcore.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.poptartking.poptartcore.eclipse.client.EclipseVisuals;
import dev.poptartking.poptartcore.lunar.client.BloodMoonVisuals;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientLevel.class)
public abstract class CelestialSkyColorMixin {
    @WrapMethod(method = "getSkyColor")
    private Vec3 poptartcore$celestialSky(Vec3 position, float partialTick, Operation<Vec3> original) {
        var sky = EclipseVisuals.darken(original.call(position, partialTick));
        return BloodMoonVisuals.tintSky(sky, position, partialTick);
    }

    // Wrap the completed result so No Man's Land's cancellable return hook cannot bypass dimming.
    @WrapMethod(method = "getCloudColor")
    private Vec3 poptartcore$celestialClouds(float partialTick, Operation<Vec3> original) {
        var clouds = EclipseVisuals.darken(original.call(partialTick));
        return BloodMoonVisuals.tintClouds(clouds, partialTick);
    }
}
