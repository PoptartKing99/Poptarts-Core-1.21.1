package dev.poptartking.poptartcore.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collections;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class PoptartCoreArmorModel extends HumanoidModel<LivingEntity> {
    public EquipmentSlot slot;
    public ModelPart root;
    public ModelPart head;
    public ModelPart body;
    public ModelPart leftArm;
    public ModelPart rightArm;
    public ModelPart leggings;
    public ModelPart leftLegging;
    public ModelPart rightLegging;
    public ModelPart leftFoot;
    public ModelPart rightFoot;

    public interface ILodestoneArmorModelBuilder {
        LayerDefinition createArmorLayer(MeshDefinition meshDefinition, PartDefinition partDefinition, PartDefinition partDefinition2, PartDefinition partDefinition3, PartDefinition partDefinition4, PartDefinition partDefinition5, PartDefinition partDefinition6, PartDefinition partDefinition7, PartDefinition partDefinition8, PartDefinition partDefinition9, PartDefinition partDefinition10);
    }

    public PoptartCoreArmorModel(ModelPart root) {
        super(root);
        this.root = root;
        this.head = getPart("head");
        this.body = getPart("body");
        this.leggings = getPart("leggings");
        this.leftArm = getPart("left_arm");
        this.rightArm = getPart("right_arm");
        this.leftLegging = getPart("left_legging");
        this.rightLegging = getPart("right_legging");
        this.leftFoot = getPart("left_foot");
        this.rightFoot = getPart("right_foot");
    }

    public ModelPart getPart(String name) {
        return getPart(this.root, name);
    }

    public static PartDefinition createHumanoidAlias(MeshDefinition mesh) {
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", new CubeListBuilder(), PartPose.ZERO);
        root.addOrReplaceChild("body", new CubeListBuilder(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", new CubeListBuilder(), PartPose.ZERO);
        root.addOrReplaceChild("left_arm", new CubeListBuilder(), PartPose.ZERO);
        root.addOrReplaceChild("leggings", new CubeListBuilder(), PartPose.ZERO);
        root.addOrReplaceChild("right_legging", new CubeListBuilder(), PartPose.ZERO);
        root.addOrReplaceChild("left_legging", new CubeListBuilder(), PartPose.ZERO);
        root.addOrReplaceChild("right_foot", new CubeListBuilder(), PartPose.ZERO);
        root.addOrReplaceChild("left_foot", new CubeListBuilder(), PartPose.ZERO);
        return root;
    }

    public static LayerDefinition createArmorModel(ILodestoneArmorModelBuilder modelBuilder) {
        MeshDefinition mesh = HumanoidModel.createMesh(new CubeDeformation(0.0f), 0.0f);
        PartDefinition root = createHumanoidAlias(mesh);
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");
        PartDefinition right_arm = root.getChild("right_arm");
        PartDefinition left_arm = root.getChild("left_arm");
        PartDefinition leggings = root.getChild("leggings");
        PartDefinition right_legging = root.getChild("right_legging");
        PartDefinition left_legging = root.getChild("left_legging");
        PartDefinition right_foot = root.getChild("right_foot");
        PartDefinition left_foot = root.getChild("left_foot");
        return modelBuilder.createArmorLayer(mesh, root, head, body, right_arm, left_arm, leggings, right_legging, left_legging, right_foot, left_foot);
    }

    protected Iterable<ModelPart> headParts() {
        return this.slot == EquipmentSlot.HEAD ? ImmutableList.of(this.head) : ImmutableList.of();
    }

    protected Iterable<ModelPart> bodyParts() {
        if (this.slot == EquipmentSlot.CHEST) {
            return ImmutableList.of(this.body, this.leftArm, this.rightArm);
        }
        if (this.slot == EquipmentSlot.LEGS) {
            return ImmutableList.of(this.leftLegging, this.rightLegging, this.leggings);
        }
        if (this.slot == EquipmentSlot.FEET) {
            return ImmutableList.of(this.leftFoot, this.rightFoot);
        }
        return ImmutableList.of();
    }

    public void renderToBuffer(PoseStack matrixStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int p_350361_) {
        if (this.slot == EquipmentSlot.LEGS) {
            this.leggings.copyFrom(this.body);
            this.leftLegging.copyFrom(this.leftLeg);
            this.rightLegging.copyFrom(this.rightLeg);
        } else if (this.slot == EquipmentSlot.FEET) {
            this.leftFoot.copyFrom(this.leftLeg);
            this.rightFoot.copyFrom(this.rightLeg);
        }
        super.renderToBuffer(matrixStack, vertexConsumer, packedLight, packedOverlay, p_350361_);
    }

    public void copyFromDefault(HumanoidModel model) {
        this.leggings.copyFrom(model.body);
        this.body.copyFrom(model.body);
        this.head.copyFrom(model.head);
        this.leftArm.copyFrom(model.leftArm);
        this.rightArm.copyFrom(model.rightArm);
        this.leftLegging.copyFrom(model.leftLeg);
        this.rightLegging.copyFrom(model.rightLeg);
        this.leftFoot.copyFrom(model.leftLeg);
        this.rightFoot.copyFrom(model.rightLeg);
    }

    public static ModelPart getPart(ModelPart root, String name) {
        return root.getChild(name);
    }
}
