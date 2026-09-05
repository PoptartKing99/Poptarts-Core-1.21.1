package dev.poptartking.poptartcore.client.model;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public final class LeatherArmorModels {

    private LeatherArmorModels() {}

    public static LayerDefinition helm() {
        return PoptartCoreArmorModel.createArmorModel(
                (mesh,
                        root,
                        head,
                        body,
                        right_arm,
                        left_arm,
                        leggings,
                        right_legging,
                        left_legging,
                        right_foot,
                        left_foot) -> {
                    head.addOrReplaceChild(
                            "helm",
                            CubeListBuilder.create()
                                    .texOffs(34, 96)
                                    .addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.6f))
                                    .texOffs(34, 112)
                                    .addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.9f)),
                            PartPose.ZERO);

                    return LayerDefinition.create(mesh, 128, 128);
                });
    }

    public static LayerDefinition tunic() {
        return tunic(true);
    }

    public static LayerDefinition tunicSkirtless() {
        return tunic(false);
    }

    private static LayerDefinition tunic(boolean skirt) {
        return PoptartCoreArmorModel.createArmorModel(
                (mesh,
                        root,
                        head,
                        body,
                        right_arm,
                        left_arm,
                        leggings,
                        right_legging,
                        left_legging,
                        right_foot,
                        left_foot) -> {
                    CubeListBuilder chest = CubeListBuilder.create()
                            .texOffs(62, 61)
                            .addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(0.35f));

                    if (skirt) {
                        chest.texOffs(90, 0).addBox(-4.0f, 9.5f, -2.0f, 8.0f, 7.0f, 4.0f, new CubeDeformation(0.7f));
                    }

                    body.addOrReplaceChild("tunic", chest, PartPose.ZERO);

                    right_arm.addOrReplaceChild(
                            "right_sleeve",
                            CubeListBuilder.create()
                                    .texOffs(40, 16)
                                    .addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.3f)),
                            PartPose.ZERO);

                    left_arm.addOrReplaceChild(
                            "left_sleeve",
                            CubeListBuilder.create()
                                    .texOffs(40, 32)
                                    .addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.3f)),
                            PartPose.ZERO);

                    return LayerDefinition.create(mesh, 128, 128);
                });
    }

    public static LayerDefinition pants() {
        return PoptartCoreArmorModel.createArmorModel(
                (mesh,
                        root,
                        head,
                        body,
                        right_arm,
                        left_arm,
                        leggings,
                        right_legging,
                        left_legging,
                        right_foot,
                        left_foot) -> {
                    leggings.addOrReplaceChild(
                            "waist",
                            CubeListBuilder.create()
                                    .texOffs(90, 12)
                                    .addBox(-4.0f, 9.5f, -2.0f, 8.0f, 7.0f, 4.0f, new CubeDeformation(0.5f)),
                            PartPose.ZERO);

                    right_legging.addOrReplaceChild(
                            "right_leg",
                            CubeListBuilder.create()
                                    .mirror(false)
                                    .texOffs(74, 45)
                                    .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.3f))
                                    .mirror(true)
                                    .texOffs(107, 64)
                                    .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.35f)),
                            PartPose.ZERO);

                    left_legging.addOrReplaceChild(
                            "left_leg",
                            CubeListBuilder.create()
                                    .mirror(false)
                                    .texOffs(90, 45)
                                    .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.3f))
                                    .mirror(true)
                                    .texOffs(90, 64)
                                    .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.4f)),
                            PartPose.ZERO);

                    return LayerDefinition.create(mesh, 128, 128);
                });
    }

    public static LayerDefinition boots() {
        return PoptartCoreArmorModel.createArmorModel(
                (mesh,
                        root,
                        head,
                        body,
                        right_arm,
                        left_arm,
                        leggings,
                        right_legging,
                        left_legging,
                        right_foot,
                        left_foot) -> {
                    right_foot.addOrReplaceChild(
                            "right_boot",
                            CubeListBuilder.create()
                                    .mirror(true)
                                    .texOffs(45, 58)
                                    .addBox(-2.1f, 7.0f, -2.0f, 4.0f, 5.0f, 4.0f, new CubeDeformation(0.6f))
                                    .texOffs(45, 67)
                                    .addBox(-2.1f, 7.0f, -2.0f, 4.0f, 5.0f, 4.0f, new CubeDeformation(0.45f)),
                            PartPose.ZERO);

                    left_foot.addOrReplaceChild(
                            "left_boot",
                            CubeListBuilder.create()
                                    .mirror(true)
                                    .texOffs(45, 77)
                                    .addBox(-1.9f, 7.0f, -2.0f, 4.0f, 5.0f, 4.0f, new CubeDeformation(0.6f))
                                    .texOffs(45, 86)
                                    .addBox(-1.9f, 7.0f, -2.0f, 4.0f, 5.0f, 4.0f, new CubeDeformation(0.45f)),
                            PartPose.ZERO);

                    return LayerDefinition.create(mesh, 128, 128);
                });
    }
}
