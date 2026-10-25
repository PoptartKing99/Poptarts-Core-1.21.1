package dev.poptartking.poptartcore.mixin.equipment;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.poptartking.poptartcore.backpack.BackpackAttachments;
import dev.poptartking.poptartcore.backpack.BackpackItem;
import dev.poptartking.poptartcore.backpack.client.BackpackAttachmentRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class BackpackAttachmentItemMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void poptartcore$attachments(
            ItemStack stack,
            ItemDisplayContext context,
            boolean leftHand,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay,
            BakedModel model,
            CallbackInfo ci) {
        if (!(stack.getItem() instanceof BackpackItem)) return;
        pose.pushPose();
        model = net.neoforged.neoforge.client.ClientHooks.handleCameraTransforms(pose, model, context, leftHand);
        pose.translate(-.5, -.5, -.5);
        dev.poptartking.poptartcore.backpack.client.BackpackModelRenderer.render(
                model,
                null,
                pose,
                buffers,
                light,
                overlay,
                dev.poptartking.poptartcore.backpack.BackpackColors.color(
                        stack, dev.poptartking.poptartcore.backpack.BackpackColors.Part.FLAPS),
                dev.poptartking.poptartcore.backpack.BackpackColors.color(
                        stack, dev.poptartking.poptartcore.backpack.BackpackColors.Part.BODY));
        BackpackAttachmentRenderer.render(
                BackpackAttachments.rucksack(stack),
                BackpackAttachments.contents(stack),
                pose,
                buffers,
                light,
                overlay,
                Minecraft.getInstance().level,
                false);
        pose.popPose();
        ci.cancel();
    }
}
