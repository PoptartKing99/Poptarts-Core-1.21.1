package dev.poptartking.poptartcore.equipment.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AgilityCape;
import dev.poptartking.poptartcore.equipment.ArmorStandCape;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ArmorStandCapeLayer extends RenderLayer<ArmorStand, ArmorStandArmorModel> {
    private final ModelPart cape;

    public ArmorStandCapeLayer(ArmorStandRenderer renderer) {
        super(renderer);
        var mesh = new MeshDefinition();
        mesh.getRoot()
                .addOrReplaceChild(
                        "cape", CubeListBuilder.create().texOffs(0, 0).addBox(-5, 0, -1, 10, 16, 1), PartPose.ZERO);
        cape = LayerDefinition.create(mesh, 64, 32).bakeRoot().getChild("cape");
    }

    @SubscribeEvent
    public static void addLayer(EntityRenderersEvent.AddLayers event) {
        if (event.getRenderer(EntityType.ARMOR_STAND) instanceof ArmorStandRenderer renderer)
            renderer.addLayer(new ArmorStandCapeLayer(renderer));
    }

    @Override
    public void render(
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            ArmorStand stand,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float age,
            float headYaw,
            float headPitch) {
        var texture = AgilityCape.texture(ArmorStandCape.get(stand));
        if (texture == null || stand.isInvisible() || stand.isMarker()) return;
        pose.pushPose();
        if (stand.isSmall()) {
            pose.scale(0.5F, 0.5F, 0.5F);
            pose.translate(0, 1.5F, 0);
        }
        getParentModel().body.translateAndRotate(pose);
        pose.translate(0, 0, stand.getItemBySlot(EquipmentSlot.CHEST).isEmpty() ? 0.125F : 0.1875F);
        pose.mulPose(Axis.XP.rotationDegrees(6));
        pose.mulPose(Axis.YP.rotationDegrees(180));
        cape.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
