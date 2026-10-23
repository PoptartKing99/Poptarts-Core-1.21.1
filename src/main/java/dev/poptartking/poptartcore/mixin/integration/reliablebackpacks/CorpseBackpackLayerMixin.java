package dev.poptartking.poptartcore.mixin.integration.reliablebackpacks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.poptartking.poptartcore.integration.ragdoll.CorpseBackpackRender;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.evandev.reliable_backpacks.client.rendering.BackpackLayer", remap = false)
public abstract class CorpseBackpackLayerMixin {
    @Shadow
    @Final
    private ModelPart parentBody;

    @WrapOperation(
            method = "renderBaseLayer",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void poptartcore$base(
            ModelPart model,
            PoseStack stack,
            VertexConsumer vertices,
            int light,
            int overlay,
            Operation<Void> original) {
        CorpseBackpackRender.render(model, parentBody, stack, vertices, light, overlay, -1);
    }

    @WrapOperation(
            method = "renderColoredLayer",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"))
    private void poptartcore$dye(
            ModelPart model,
            PoseStack stack,
            VertexConsumer vertices,
            int light,
            int overlay,
            int color,
            Operation<Void> original) {
        CorpseBackpackRender.render(model, parentBody, stack, vertices, light, overlay, color);
    }
}
