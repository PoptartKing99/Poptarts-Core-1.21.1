package dev.poptartking.poptartcore.armor.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public class BeekeeperArmorModel extends PoptartCoreArmorModel {
    public BeekeeperArmorModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return createArmorModel(
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
                    var hood = head.addOrReplaceChild(
                            "hood",
                            CubeListBuilder.create()
                                    .texOffs(1, 16)
                                    .addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.2F))
                                    .texOffs(0, 0)
                                    .addBox(-3.5F, -7.5F, -5.5F, 7.0F, 7.0F, 2.0F, new CubeDeformation(0.2F)),
                            PartPose.ZERO);
                    hood.addOrReplaceChild(
                            "flap",
                            CubeListBuilder.create().texOffs(0, 11).addBox(-4.0F, 0.0F, 0.0F, 7.0F, 2.0F, 2.0F),
                            PartPose.offsetAndRotation(0.5F, -8.75F, 4.65F, -0.3491F, 0.0F, 0.0F));
                    body.addOrReplaceChild(
                            "jacket",
                            CubeListBuilder.create()
                                    .texOffs(18, 0)
                                    .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.6F))
                                    .texOffs(32, 32)
                                    .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)),
                            PartPose.ZERO);
                    leggings.addOrReplaceChild(
                            "skirt",
                            CubeListBuilder.create()
                                    .texOffs(1, 34)
                                    .addBox(-4.5F, 9.0F, -2.5F, 9.0F, 9.0F, 5.0F, new CubeDeformation(0.5F)),
                            PartPose.ZERO);
                    rightArm.addOrReplaceChild(
                            "right_sleeve",
                            CubeListBuilder.create()
                                    .texOffs(42, 0)
                                    .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.3F))
                                    .mirror()
                                    .texOffs(43, 16)
                                    .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)),
                            PartPose.ZERO);
                    leftArm.addOrReplaceChild(
                            "left_sleeve",
                            CubeListBuilder.create()
                                    .mirror()
                                    .texOffs(42, 0)
                                    .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.3F))
                                    .mirror(false)
                                    .texOffs(43, 16)
                                    .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)),
                            PartPose.ZERO);
                    rightLegging.addOrReplaceChild(
                            "right_wrap",
                            CubeListBuilder.create()
                                    .texOffs(0, 48)
                                    .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.3F))
                                    .texOffs(16, 48)
                                    .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
                            PartPose.ZERO);
                    leftLegging.addOrReplaceChild(
                            "left_wrap",
                            CubeListBuilder.create()
                                    .mirror()
                                    .texOffs(0, 48)
                                    .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.3F))
                                    .mirror(false)
                                    .texOffs(16, 48)
                                    .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.4F)),
                            PartPose.ZERO);
                    rightFoot.addOrReplaceChild(
                            "right_boot",
                            CubeListBuilder.create()
                                    .texOffs(32, 48)
                                    .addBox(-2.1F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.65F))
                                    .texOffs(48, 48)
                                    .addBox(-2.1F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.5F)),
                            PartPose.ZERO);
                    leftFoot.addOrReplaceChild(
                            "left_boot",
                            CubeListBuilder.create()
                                    .mirror()
                                    .texOffs(32, 48)
                                    .addBox(-1.9F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.65F))
                                    .mirror(false)
                                    .texOffs(48, 48)
                                    .addBox(-1.9F, 7.0F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.5F)),
                            PartPose.ZERO);
                    return LayerDefinition.create(mesh, 64, 64);
                });
    }
}
