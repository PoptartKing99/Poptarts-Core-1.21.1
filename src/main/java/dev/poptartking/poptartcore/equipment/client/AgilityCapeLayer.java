package dev.poptartking.poptartcore.equipment.client;

import com.farcr.ragdoll.content.entity.RagdollPlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.equipment.AgilityCape;
import dev.poptartking.poptartcore.integration.ragdoll.CorpseBackpackRender;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class AgilityCapeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final ModelPart cape;
    private final ModelPart corpseCape;

    public AgilityCapeLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
        var mesh = new MeshDefinition();
        mesh.getRoot()
                .addOrReplaceChild(
                        "cape", CubeListBuilder.create().texOffs(0, 0).addBox(-5, 0, -1, 10, 16, 1), PartPose.ZERO);
        cape = LayerDefinition.create(mesh, 64, 32).bakeRoot().getChild("cape");
        var corpseMesh = new MeshDefinition();
        corpseMesh
                .getRoot()
                .addOrReplaceChild(
                        "cloak",
                        CubeListBuilder.create().texOffs(0, 0).addBox(-5, 0, -1, 10, 16, 1),
                        PartPose.offsetAndRotation(0, 0, 2, 0, Mth.PI, 0));
        corpseCape = LayerDefinition.create(corpseMesh, 64, 32).bakeRoot();
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            var renderer = event.getSkin(skin);
            if (renderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer playerRenderer)
                playerRenderer.addLayer(new AgilityCapeLayer(playerRenderer));
        }
    }

    @Override
    public void render(
            PoseStack stack,
            MultiBufferSource buffers,
            int light,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float age,
            float headYaw,
            float headPitch) {
        if (!AgilityCape.equipped(player)
                || player.isInvisible()
                || player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) return;
        var vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(AgilityCape.texture(player)));
        if (player instanceof RagdollPlayer) {
            CorpseBackpackRender.render(
                    corpseCape, getParentModel().body, stack, vertices, light, OverlayTexture.NO_OVERLAY, -1);
        } else {
            stack.pushPose();
            // Aether's CapeModel offsets and vanilla CapeLayer movement, without an Aether dependency.
            boolean armored = !player.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
            cape.y = player.isCrouching() ? (armored ? 0.8F : 1.85F) : (armored ? -0.85F : 0);
            cape.z = player.isCrouching() ? (armored ? 0.3F : 1.4F) : (armored ? -1.1F : 0);
            stack.translate(0, 0, 0.125F);
            double dx = Mth.lerp(partialTick, player.xCloakO, player.xCloak)
                    - Mth.lerp(partialTick, player.xo, player.getX());
            double dy = Mth.lerp(partialTick, player.yCloakO, player.yCloak)
                    - Mth.lerp(partialTick, player.yo, player.getY());
            double dz = Mth.lerp(partialTick, player.zCloakO, player.zCloak)
                    - Mth.lerp(partialTick, player.zo, player.getZ());
            float yaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
            double sin = Mth.sin(yaw), cos = -Mth.cos(yaw);
            float lift = Mth.clamp((float) dy * 10, -6, 32);
            float forward = Mth.clamp((float) (dx * sin + dz * cos) * 100, 0, 150);
            float sideways = Mth.clamp((float) (dx * cos - dz * sin) * 100, -20, 20);
            lift += Mth.sin(Mth.lerp(partialTick, player.walkDistO, player.walkDist) * 6)
                    * 32
                    * Mth.lerp(partialTick, player.oBob, player.bob);
            if (player.isCrouching()) lift += 25;
            stack.mulPose(Axis.XP.rotationDegrees(6 + forward / 2 + lift));
            stack.mulPose(Axis.ZP.rotationDegrees(sideways / 2));
            stack.mulPose(Axis.YP.rotationDegrees(180 - sideways / 2));
            cape.render(stack, vertices, light, OverlayTexture.NO_OVERLAY);
            stack.popPose();
        }
    }
}
