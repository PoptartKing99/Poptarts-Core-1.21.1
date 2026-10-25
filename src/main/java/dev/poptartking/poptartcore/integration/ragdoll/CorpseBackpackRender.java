package dev.poptartking.poptartcore.integration.ragdoll;

import com.farcr.ragdoll.content.entity.RagdollPlayer;
import com.farcr.ragdoll.mixin.ModelPartAccessor;
import com.farcr.ragdoll.system.RagdollRenderManager;
import com.farcr.ragdoll.util.ModelPartHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class CorpseBackpackRender {
    private static final ThreadLocal<Boolean> DRAWING = ThreadLocal.withInitial(() -> false);

    private CorpseBackpackRender() {}

    public static boolean drawing() {
        return DRAWING.get();
    }

    /** Applies the corpse's torso pose, removing the GUI preview anchor only outside world rendering. */
    public static boolean poseCorpseTorso(ModelPart body, PoseStack stack) {
        var geometry = RagdollRenderManager.findMatchingGeometry("body");
        if (!(RagdollRenderManager.getBoundEntity() instanceof RagdollPlayer)
                || !RagdollRenderManager.isRenderingLayerOverride()
                || geometry == null
                || !geometry.hasRenderableBox()) return false;
        var center = RagdollRenderManager.isRenderLevel()
                ? new Vector3f()
                : ModelPartHelper.findCenter(RagdollRenderManager.getBoundEntity());
        if (!RagdollRenderManager.isRenderLevel()) {
            var anchor = new PoseStack();
            ModelPartHelper.translateAndRotate(anchor, geometry, null, center);
            stack.last().pose().mul(new Matrix4f(anchor.last().pose()).invert());
            stack.last().normal().mul(new Matrix3f(anchor.last().normal()).invert());
        }
        ModelPartHelper.translateAndRotate(stack, geometry, (ModelPartAccessor) (Object) body, center);
        return true;
    }

    public static void render(
            ModelPart model,
            ModelPart body,
            PoseStack stack,
            VertexConsumer vertices,
            int light,
            int overlay,
            int color) {
        stack.pushPose();
        if (!poseCorpseTorso(body, stack)) {
            stack.popPose();
            model.render(stack, vertices, light, overlay, color);
            return;
        }
        var pose = model.storePose();
        float sx = model.xScale, sy = model.yScale, sz = model.zScale;
        boolean previous = DRAWING.get();
        try {
            // The torso transform is already on the stack; do not apply the copied body pose again.
            model.x = model.y = model.z = 0;
            model.xRot = model.yRot = model.zRot = 0;
            model.xScale = model.yScale = model.zScale = 1;
            DRAWING.set(true);
            model.render(stack, vertices, light, overlay, color);
        } finally {
            DRAWING.set(previous);
            model.loadPose(pose);
            model.xScale = sx;
            model.yScale = sy;
            model.zScale = sz;
            stack.popPose();
        }
    }
}
