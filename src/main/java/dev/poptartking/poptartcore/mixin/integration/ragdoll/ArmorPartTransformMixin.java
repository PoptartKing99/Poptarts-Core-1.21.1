package dev.poptartking.poptartcore.mixin.integration.ragdoll;

import com.farcr.ragdoll.content.entity.RagdollPlayer;
import com.farcr.ragdoll.mixin.ModelPartAccessor;
import com.farcr.ragdoll.system.EntityGeometry;
import com.farcr.ragdoll.system.RagdollRenderManager;
import com.farcr.ragdoll.util.ModelPartHelper;
import com.farcr.ragdoll.util.NameHolder;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.poptartking.poptartcore.armor.client.PoptartCoreModelLayers;
import dev.poptartking.poptartcore.integration.ragdoll.RagdollArmorCompat;
import dev.poptartking.poptartcore.integration.ragdoll.CorpseScreenArmorPose;
import java.util.Set;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ModelPartHelper.class)
public abstract class ArmorPartTransformMixin {
    @Unique
    private static final Set<String> POPTARTCORE$PARTS_NEEDING_PLAYER_SCALE = Set.of(
            "metal_body",
            "metal_waist",
            "metal_left_leg",
            "metal_right_leg",
            "metal_left_boot",
            "metal_right_boot",
            "right_boot",
            "left_boot",
            "helmet",
            "clip_right",
            "clip_left");

    @WrapMethod(method = "translateAndRotate")
    private static void poptartcore$usePlayerPartScaleForArmor(
            PoseStack poseStack,
            EntityGeometry geometry,
            ModelPartAccessor part,
            Vector3f originOffset,
            Operation<Void> original) {
        if (part != null) {
            CorpseScreenArmorPose.resetForPart(poseStack);
        }
        if (!RagdollArmorCompat.isRenderingOurArmor()
                || !(RagdollRenderManager.getBoundEntity() instanceof RagdollPlayer)
                || part == null
                || geometry == null
                || !(part instanceof NameHolder named)
                || !POPTARTCORE$PARTS_NEEDING_PLAYER_SCALE.contains(named.getHeldName())) {
            original.call(poseStack, geometry, part, originOffset);
            return;
        }

        ModelPart root = RagdollRenderManager.getBoundModel();
        ModelPart playerPart = root == null ? null : ((ModelPartAccessor) (Object) root).children().get(geometry.name);
        if (playerPart == null) {
            original.call(poseStack, geometry, part, originOffset);
            return;
        }

        original.call(poseStack, geometry, (ModelPartAccessor) (Object) playerPart, originOffset);
        if (named.getHeldName().equals("clip_right") || named.getHeldName().equals("clip_left")) {
            poptartcore$applyLocalPose(PoptartCoreModelLayers.MINING_HELMET_MODEL.head.getChild("helmet"), poseStack);
        }
        poptartcore$applyLocalPose((ModelPart) (Object) part, poseStack);
    }

    @Unique
    private static void poptartcore$applyLocalPose(ModelPart part, PoseStack poseStack) {
        poseStack.translate(part.x / 16.0F, part.y / 16.0F, part.z / 16.0F);
        if (part.xRot != 0.0F || part.yRot != 0.0F || part.zRot != 0.0F) {
            poseStack.mulPose(new Quaternionf().rotationZYX(part.zRot, part.yRot, part.xRot));
        }
        if (part.xScale != 1.0F || part.yScale != 1.0F || part.zScale != 1.0F) {
            poseStack.scale(part.xScale, part.yScale, part.zScale);
        }
    }
}
