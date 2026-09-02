package dev.poptartking.poptartcore.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;


public class MiningHelmetModel extends LodestoneArmorModel {
    public MiningHelmetModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return createArmorModel((mesh, root, head, body, right_arm, left_arm, leggings, right_legging, left_legging, right_foot, left_foot) -> {
            PartDefinition helmet = head.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5f, -10.3f, -4.5f, 9.0f, 4.0f, 9.0f, new CubeDeformation(0.1f)).texOffs(27, 0).addBox(-3.5f, -7.2f, -6.6f, 7.0f, 1.0f, 2.0f, new CubeDeformation(0.025f)).texOffs(44, 0).addBox(-1.0f, -13.3f, -5.5f, 2.0f, 6.0f, 2.0f).texOffs(52, 0).addBox(-1.0f, -13.3f, -5.5f, 2.0f, 6.0f, 2.0f, new CubeDeformation(0.25f)), PartPose.offset(0.0f, 1.0f, 0.0f));
            helmet.addOrReplaceChild("clip_right", CubeListBuilder.create().texOffs(51, 1).addBox(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 0.0f), PartPose.offsetAndRotation(0.1f, -13.8f, -4.5f, 0.0f, -0.7854f, 0.0f));
            helmet.addOrReplaceChild("clip_left", CubeListBuilder.create().texOffs(51, 1).addBox(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 0.0f), PartPose.offsetAndRotation(0.1f, -13.8f, -4.5f, 0.0f, 0.7854f, 0.0f));
            return LayerDefinition.create(mesh, 64, 64);
        });
    }
}