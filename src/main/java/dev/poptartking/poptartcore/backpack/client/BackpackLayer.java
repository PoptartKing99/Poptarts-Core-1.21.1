package dev.poptartking.poptartcore.backpack.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class BackpackLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final net.minecraft.client.resources.model.ModelResourceLocation STRAPS =
            net.minecraft.client.resources.model.ModelResourceLocation.standalone(
                    PoptartCore.location("block/backpack_straps"));
    private static final net.minecraft.client.resources.model.ModelResourceLocation RUCKSACK_STRAPS =
            net.minecraft.client.resources.model.ModelResourceLocation.standalone(
                    PoptartCore.location("block/rucksack_straps"));

    @SubscribeEvent
    public static void registerModels(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional event) {
        event.register(STRAPS);
        event.register(RUCKSACK_STRAPS);
    }

    public BackpackLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            var renderer = event.getSkin(skin);
            if (renderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer playerRenderer) {
                playerRenderer.addLayer(new BackpackLayer(playerRenderer));
            }
        }
    }

    @Override
    public void render(
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float age,
            float yaw,
            float pitch) {
        if (player.isInvisible()
                || player.isSpectator()
                || player.getData(PoptartCoreAttachments.BASIC_BACKPACK_VISUAL).isEmpty()) return;
        var item = (dev.poptartking.poptartcore.backpack.BackpackItem)
                player.getData(PoptartCoreAttachments.BASIC_BACKPACK_VISUAL).getItem();
        var block = (dev.poptartking.poptartcore.backpack.BackpackBlock) item.getBlock();
        boolean rucksack = block.rows() == 6;
        pose.pushPose();
        if (!dev.poptartking.poptartcore.integration.ragdoll.CorpseBackpackRender.poseCorpseTorso(
                getParentModel().body, pose)) {
            getParentModel().body.translateAndRotate(pose);
        }
        pose.scale(-1, -1, 1);
        pose.translate(-0.5, rucksack ? -1.125 : -0.75, -0.25);
        var visual = player.getData(PoptartCoreAttachments.BASIC_BACKPACK_VISUAL);
        boolean dyed = visual.has(net.minecraft.core.component.DataComponents.DYED_COLOR);
        boolean bodyDyed = visual.has(dev.poptartking.poptartcore.backpack.BackpackColors.BODY.get());
        var state = block.defaultBlockState()
                .setValue(dev.poptartking.poptartcore.backpack.BackpackBlock.FACING, net.minecraft.core.Direction.SOUTH)
                .setValue(dev.poptartking.poptartcore.backpack.BackpackBlock.DYED, dyed)
                .setValue(dev.poptartking.poptartcore.backpack.BackpackBlock.BODY_DYED, bodyDyed);
        pose.pushPose();
        // World models are grounded; keep the original rucksack position on the wearer.
        if (rucksack) pose.translate(0, 3.0 / 16.0, 0);
        BackpackModelRenderer.render(
                state,
                pose,
                buffers,
                light,
                OverlayTexture.NO_OVERLAY,
                dev.poptartking.poptartcore.backpack.BackpackColors.color(
                        visual, dev.poptartking.poptartcore.backpack.BackpackColors.Part.FLAPS),
                dev.poptartking.poptartcore.backpack.BackpackColors.color(
                        visual, dev.poptartking.poptartcore.backpack.BackpackColors.Part.BODY));
        pose.popPose();
        BackpackAttachmentRenderer.render(
                rucksack,
                dev.poptartking.poptartcore.backpack.BackpackAttachments.contents(visual),
                pose,
                buffers,
                light,
                OverlayTexture.NO_OVERLAY,
                player.level(),
                player.getY(),
                dev.poptartking.poptartcore.equipment.UtilityWield.isAttachmentWielding(player)
                        || player.getData(PoptartCoreAttachments.BACKPACK_ATTACHMENT_WIELDING));
        var model = Minecraft.getInstance().getModelManager().getModel(rucksack ? RUCKSACK_STRAPS : STRAPS);
        var vertices = buffers.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(
                net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS));
        Minecraft.getInstance()
                .getBlockRenderer()
                .getModelRenderer()
                .renderModel(
                        pose.last(),
                        vertices,
                        block.defaultBlockState(),
                        model,
                        1,
                        1,
                        1,
                        light,
                        OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
