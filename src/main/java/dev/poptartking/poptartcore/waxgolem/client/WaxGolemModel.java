package dev.poptartking.poptartcore.waxgolem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.poptartking.poptartcore.waxgolem.WaxGolem;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/* JADX INFO: loaded from: wayfarer_core-0.1.0.jar:dev/tazer/wayfarer/client/waxgolem/WaxGolemModel.class */
public class WaxGolemModel extends EntityModel<WaxGolem> {
    public final ModelPart body;
    public final ModelPart head;
    public final ModelPart headDead;
    public final ModelPart armRight;
    public final ModelPart armLeft;
    public final ModelPart legRight;
    public final ModelPart legLeft;
    private final ModelPart belly1;
    private final ModelPart chest1;
    private final ModelPart belly2;
    private final ModelPart chest2;
    private final ModelPart head1;
    private final ModelPart head2;
    private final ModelPart head3;
    private final ModelPart wick1;
    private final ModelPart wick2;
    private final ModelPart wick3;
    private final ModelPart legRightWorn;
    private final ModelPart legLeftWorn;
    private final ModelPart legRightFresh;
    private final ModelPart legLeftFresh;
    private static final float[] WICK_LOCAL = {-9.0f, -8.0f, -5.0f, -4.0f};
    private int stage;

    public WaxGolemModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.headDead = this.body.getChild("head_dead");
        this.armRight = this.body.getChild("arm_right");
        this.armLeft = this.body.getChild("arm_left");
        this.legRight = root.getChild("leg_right");
        this.legLeft = root.getChild("leg_left");
        this.belly1 = this.body.getChild("belly1");
        this.chest1 = this.body.getChild("chest1");
        this.belly2 = this.body.getChild("belly2");
        this.chest2 = this.body.getChild("chest2");
        this.head1 = this.head.getChild("head1");
        this.head2 = this.head.getChild("head2");
        this.head3 = this.head.getChild("head3");
        this.wick1 = this.head.getChild("wick1");
        this.wick2 = this.head.getChild("wick2");
        this.wick3 = this.head.getChild("wick3");
        this.legRightFresh = this.legRight.getChild("fresh");
        this.legLeftFresh = this.legLeft.getChild("fresh");
        this.legRightWorn = this.legRight.getChild("worn");
        this.legLeftWorn = this.legLeft.getChild("worn");
    }

    public static LayerDefinition create() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body =
                root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0f, 24.0f, 0.0f));
        body.addOrReplaceChild(
                "belly1",
                CubeListBuilder.create().texOffs(0, 42).addBox(-3.0f, -16.0f, -1.0f, 6.0f, 6.0f, 3.0f),
                PartPose.ZERO);
        body.addOrReplaceChild(
                "chest1",
                CubeListBuilder.create()
                        .texOffs(32, 27)
                        .addBox(-4.0f, -22.0f, -2.0f, 8.0f, 6.0f, 4.0f, new CubeDeformation(-0.01f)),
                PartPose.ZERO);
        body.addOrReplaceChild(
                "belly2",
                CubeListBuilder.create().texOffs(18, 49).addBox(-3.0f, -14.0f, -1.0f, 6.0f, 4.0f, 3.0f),
                PartPose.ZERO);
        body.addOrReplaceChild(
                "chest2",
                CubeListBuilder.create().texOffs(32, 37).addBox(-4.0f, -20.0f, -2.0f, 8.0f, 6.0f, 4.0f),
                PartPose.ZERO);
        PartDefinition head = body.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(32, 12).addBox(-4.0f, 0.0f, -5.0f, 8.0f, 12.0f, 3.0f),
                PartPose.offset(0.0f, -22.0f, 0.0f));
        head.addOrReplaceChild(
                "head1",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -7.0f, -5.0f, 8.0f, 7.0f, 8.0f),
                PartPose.ZERO);
        head.addOrReplaceChild(
                "head2",
                CubeListBuilder.create().texOffs(0, 15).addBox(-4.0f, -6.0f, -5.0f, 8.0f, 6.0f, 8.0f),
                PartPose.ZERO);
        head.addOrReplaceChild(
                "head3",
                CubeListBuilder.create().texOffs(0, 29).addBox(-4.0f, -3.0f, -5.0f, 8.0f, 4.0f, 8.0f),
                PartPose.ZERO);
        wick(head, "wick1", 18, 42, 20, 42, -7.0f, -2.0f);
        wick(head, "wick2", 22, 42, 24, 42, -5.0f, -2.0f);
        wick(head, "wick3", 26, 42, 28, 42, -3.0f, -2.0f);
        PartDefinition dead = body.addOrReplaceChild(
                "head_dead",
                CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -2.0f, -5.0f, 8.0f, 4.0f, 8.0f),
                PartPose.offset(0.0f, -20.0f, 0.0f));
        wick(dead, "wick", 30, 42, 18, 44, -2.0f, -2.0f);
        body.addOrReplaceChild(
                "arm_right",
                CubeListBuilder.create().texOffs(44, 47).addBox(-1.0f, -1.0f, -1.0f, 2.0f, 12.0f, 2.0f),
                PartPose.offset(5.0f, -21.0f, 0.0f));
        body.addOrReplaceChild(
                "arm_left",
                CubeListBuilder.create().texOffs(36, 47).addBox(-1.0f, -1.0f, -1.0f, 2.0f, 12.0f, 2.0f),
                PartPose.offset(-5.0f, -21.0f, 0.0f));
        PartDefinition legRight =
                root.addOrReplaceChild("leg_right", CubeListBuilder.create(), PartPose.offset(2.0f, 14.0f, 0.0f));
        legRight.addOrReplaceChild(
                "fresh",
                CubeListBuilder.create().texOffs(8, 51).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 10.0f, 2.0f),
                PartPose.ZERO);
        legRight.addOrReplaceChild(
                "worn",
                CubeListBuilder.create()
                        .texOffs(52, 47)
                        .addBox(-1.0f, 0.0f, -1.0f, 2.0f, 10.0f, 2.0f, new CubeDeformation(0.25f)),
                PartPose.ZERO);
        PartDefinition legLeft =
                root.addOrReplaceChild("leg_left", CubeListBuilder.create(), PartPose.offset(-2.0f, 14.0f, 0.0f));
        legLeft.addOrReplaceChild(
                "fresh",
                CubeListBuilder.create().texOffs(0, 51).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 10.0f, 2.0f),
                PartPose.ZERO);
        legLeft.addOrReplaceChild(
                "worn",
                CubeListBuilder.create()
                        .texOffs(54, 12)
                        .addBox(-1.0f, 0.0f, -1.0f, 2.0f, 10.0f, 2.0f, new CubeDeformation(0.25f)),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void wick(
            PartDefinition parent, String name, int u1, int v1, int u2, int v2, float pivotY, float boxY) {
        PartDefinition wick =
                parent.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.offset(0.0f, pivotY, -1.0f));
        wick.addOrReplaceChild(
                "a",
                CubeListBuilder.create().texOffs(u1, v1).addBox(-0.5f, boxY, 0.0f, 1.0f, 2.0f, 0.0f),
                PartPose.rotation(0.0f, 0.7853982f, 0.0f));
        wick.addOrReplaceChild(
                "b",
                CubeListBuilder.create().texOffs(u2, v2).addBox(-0.5f, boxY, 0.0f, 1.0f, 2.0f, 0.0f),
                PartPose.rotation(0.0f, -0.7853982f, 0.0f));
    }

    public void selectStage(int value) {
        this.stage = value;
        this.belly1.visible = value < 3;
        this.chest1.visible = value < 3;
        this.belly2.visible = value == 3;
        this.chest2.visible = value == 3;
        this.head1.visible = value == 0;
        this.head2.visible = value == 1;
        this.head3.visible = value == 2;
        this.wick1.visible = value == 0;
        this.wick2.visible = value == 1;
        this.wick3.visible = value == 2;
        this.head.visible = value < 3;
        this.headDead.visible = value == 3;
        this.legRightFresh.visible = true;
        this.legLeftFresh.visible = true;
        this.legRightWorn.visible = true;
        this.legLeftWorn.visible = true;
    }

    public void setupAnim(
            WaxGolem golem, float limbSwing, float limbSwingAmount, float age, float netHeadYaw, float headPitch) {
        selectStage(golem.stage());
        ModelPart crown = golem.stage() == 3 ? this.headDead : this.head;
        crown.yRot = netHeadYaw * 0.017453292f;
        crown.xRot = headPitch * 0.017453292f;
        float sway = golem.lit() ? Mth.cos(age * 0.06f) * 0.05f : 0.0f;
        this.body.zRot = sway;
        this.body.xRot = 0.0f;
        float swing = Mth.cos(limbSwing * 1.3324f) * 1.4f * limbSwingAmount;
        this.legRight.xRot = swing;
        this.legLeft.xRot = -swing;
        this.armRight.xRot = ((-swing) * 0.7f) + sway;
        this.armLeft.xRot = (swing * 0.7f) - sway;
        this.armRight.zRot = 0.08f;
        this.armLeft.zRot = -0.08f;
        if (golem.sitting()) {
            this.body.y = 32.0f;
            this.legRight.xRot = -1.5707964f;
            this.legLeft.xRot = -1.5707964f;
            this.legRight.y = 23.0f;
            this.legLeft.y = 23.0f;
            this.armRight.xRot = -0.3f;
            this.armLeft.xRot = -0.3f;
        } else {
            this.body.y = 24.0f;
            this.legRight.y = 14.0f;
            this.legLeft.y = 14.0f;
        }
        if (!golem.tool().isEmpty() || !golem.result().isEmpty()) {
            this.armLeft.xRot = (this.armLeft.xRot * 0.5f) - 0.3141593f;
        }
        if (!golem.lit()) {
            this.body.zRot = 0.0f;
            this.armRight.xRot = 0.0f;
            this.armLeft.xRot = 0.0f;
            this.legRight.xRot = 0.0f;
            this.legLeft.xRot = 0.0f;
        }
    }

    public void wickAnchor(PoseStack poseStack) {
        int value = Math.clamp(this.stage, 0, WICK_LOCAL.length - 1);
        this.body.translateAndRotate(poseStack);
        (value == 3 ? this.headDead : this.head).translateAndRotate(poseStack);
        poseStack.translate(0.0d, WICK_LOCAL[value] / 16.0f, -0.0625d);
    }

    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int colour) {
        this.body.render(poseStack, consumer, light, overlay, colour);
        this.legRight.render(poseStack, consumer, light, overlay, colour);
        this.legLeft.render(poseStack, consumer, light, overlay, colour);
    }
}
