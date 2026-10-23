package dev.poptartking.poptartcore.mixin.equipment;

import com.llamalad7.mixinextras.sugar.Local;
import dev.poptartking.poptartcore.equipment.AgilityCape;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ElytraLayer.class)
public abstract class AgilityCapeElytraMixin {
    @ModifyArg(
            method =
                    "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"),
            index = 0)
    private ResourceLocation poptartcore$capeWings(
            ResourceLocation original, @Local(argsOnly = true) LivingEntity entity) {
        return entity instanceof Player player && AgilityCape.equipped(player) ? AgilityCape.texture(player) : original;
    }
}
