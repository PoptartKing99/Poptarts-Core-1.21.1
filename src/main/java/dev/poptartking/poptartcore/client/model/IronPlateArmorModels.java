package dev.poptartking.poptartcore.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public final class IronPlateArmorModels {

    private IronPlateArmorModels() {}

    public static LayerDefinition helmet() {
        return PoptartCoreArmorModel.createArmorModel(
                (mesh,
                        root,
                        head,
                        body,
                        rightArm,
                        leftArm,
                        leggings,
                        rightLegging,
                        leftLegging,
                        rightFoot,
                        leftFoot) -> {
                    head.addOrReplaceChild(
                            "iron_plate_helmet",
                            CubeListBuilder.create()
                                    .texOffs(0, 0)
                                    .addBox(-4.5F, -9.0F, -4.5F, 9.0F, 10.0F, 9.0F, new CubeDeformation(0.3F))
                                    .texOffs(0, 20)
                                    .addBox(-4.5F, -9.0F, -4.5F, 9.0F, 10.0F, 9.0F, new CubeDeformation(0.7F)),
                            PartPose.ZERO);

                    return LayerDefinition.create(mesh, 64, 64);
                });
    }

    public static LayerDefinition chestplate() {
        return PoptartCoreArmorModel.createArmorModel(
                (mesh,
                        root,
                        head,
                        body,
                        rightArm,
                        leftArm,
                        leggings,
                        rightLegging,
                        leftLegging,
                        rightFoot,
                        leftFoot) -> {
                    leftArm.addOrReplaceChild(
                            "iron_plate_left_arm",
                            CubeListBuilder.create()
                                    .texOffs(21, 19)
                                    .addBox(-1.0F, -2.5F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(-0.1F)),
                            PartPose.ZERO);
                    rightArm.addOrReplaceChild(
                            "iron_plate_right_arm",
                            CubeListBuilder.create()
                                    .texOffs(0, 19)
                                    .addBox(-4.0F, -2.5F, -2.5F, 5.0F, 6.0F, 5.0F, new CubeDeformation(-0.1F)),
                            PartPose.ZERO);
                    body.addOrReplaceChild(
                            "iron_plate_body",
                            CubeListBuilder.create()
                                    .texOffs(0, 0)
                                    .addBox(-4.5F, -0.5F, -2.5F, 9.0F, 14.0F, 5.0F, new CubeDeformation(0.01F)),
                            PartPose.ZERO);

                    return LayerDefinition.create(mesh, 64, 64);
                });
    }

    public static LayerDefinition leggings() {
        return PoptartCoreArmorModel.createArmorModel(
                (mesh,
                        root,
                        head,
                        body,
                        rightArm,
                        leftArm,
                        leggings,
                        rightLegging,
                        leftLegging,
                        rightFoot,
                        leftFoot) -> {
                    leftLegging.addOrReplaceChild(
                            "iron_plate_left_leg",
                            CubeListBuilder.create()
                                    .texOffs(0, 16)
                                    .mirror()
                                    .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F))
                                    .mirror(false),
                            PartPose.ZERO);
                    rightLegging.addOrReplaceChild(
                            "iron_plate_right_leg",
                            CubeListBuilder.create()
                                    .texOffs(0, 16)
                                    .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)),
                            PartPose.ZERO);
                    leggings.addOrReplaceChild(
                            "iron_plate_waist",
                            CubeListBuilder.create()
                                    .texOffs(0, 0)
                                    .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
                            PartPose.ZERO);

                    return LayerDefinition.create(mesh, 32, 32);
                });
    }

    public static LayerDefinition boots() {
        return PoptartCoreArmorModel.createArmorModel(
                (mesh,
                        root,
                        head,
                        body,
                        rightArm,
                        leftArm,
                        leggings,
                        rightLegging,
                        leftLegging,
                        rightFoot,
                        leftFoot) -> {
                    leftFoot.addOrReplaceChild(
                            "iron_plate_left_boot",
                            CubeListBuilder.create()
                                    .texOffs(0, 0)
                                    .mirror()
                                    .addBox(-2.4F, 1.3F, -2.5F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.0F))
                                    .mirror(false),
                            PartPose.ZERO);
                    rightFoot.addOrReplaceChild(
                            "iron_plate_right_boot",
                            CubeListBuilder.create()
                                    .texOffs(0, 0)
                                    .addBox(-2.6F, 1.3F, -2.5F, 5.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)),
                            PartPose.ZERO);

                    return LayerDefinition.create(mesh, 32, 32);
                });
    }
}
