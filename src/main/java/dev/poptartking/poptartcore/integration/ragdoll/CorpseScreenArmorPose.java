package dev.poptartking.poptartcore.integration.ragdoll;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class CorpseScreenArmorPose {
    private static final ThreadLocal<Frame> FRAME = new ThreadLocal<>();

    private CorpseScreenArmorPose() {
    }

    public static void begin(Matrix4f pose, Matrix3f normal) {
        FRAME.set(new Frame(pose, normal));
    }

    public static void resetForPart(PoseStack stack) {
        Frame frame = FRAME.get();
        if (frame != null) {
            stack.last().pose().set(frame.pose());
            stack.last().normal().set(frame.normal());
        }
    }

    public static void end() {
        FRAME.remove();
    }

    private record Frame(Matrix4f pose, Matrix3f normal) {
    }
}
