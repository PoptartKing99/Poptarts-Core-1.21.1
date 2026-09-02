package dev.poptartking.poptartcore.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class RawHideArmorModel extends PoptartCoreArmorModel {

    public RawHideArmorModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return createArmorModel((mesh, root, head, body, right_arm, left_arm, leggings, right_legging, left_legging, right_foot, left_foot) -> {
            head.addOrReplaceChild(
                    "hood",
                    CubeListBuilder.create()
                            .texOffs(0, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 10.0F, 9.0F, new CubeDeformation(0.1F)),
                    PartPose.ZERO
            );

            body.addOrReplaceChild(
                    "tunic",
                    CubeListBuilder.create()
                            .texOffs(0, 29).addBox(-4.5F, -0.5F, -2.5F, 9.0F, 16.0F, 5.0F, new CubeDeformation(0.25F)),
                    PartPose.ZERO
            );

            right_arm.addOrReplaceChild(
                    "right_sleeve",
                    CubeListBuilder.create()
                            .texOffs(32, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
                    PartPose.ZERO
            );

            PartDefinition belt = leggings.addOrReplaceChild(
                    "belt",
                    CubeListBuilder.create()
                            .texOffs(36, 0).addBox(-4.5F, 8.5F, -2.5F, 9.0F, 4.0F, 5.0F, CubeDeformation.NONE),
                    PartPose.ZERO
            );

            belt.addOrReplaceChild(
                    "strap",
                    CubeListBuilder.create()
                            .texOffs(41, 9).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 5.0F, 0.0F, CubeDeformation.NONE),
                    PartPose.offsetAndRotation(0.0F, 9.5F, -2.5F, -0.0436F, 0.0F, 0.0F)
            );

            right_legging.addOrReplaceChild(
                    "right_wrap",
                    CubeListBuilder.create()
                            .texOffs(32, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.333F)),
                    PartPose.ZERO
            );

            left_legging.addOrReplaceChild(
                    "left_wrap",
                    CubeListBuilder.create()
                            .texOffs(48, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.33F)),
                    PartPose.ZERO
            );

            return LayerDefinition.create(mesh, 64, 64);
        });
    }
}