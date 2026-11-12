package dev.poptartking.poptartcore.lunar.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.poptartking.poptartcore.PoptartCore;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class BloodMoonEyesLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final int GLOW_TINT = 0xCCCCCC;
    private final RenderType eyes;

    private BloodMoonEyesLayer(LivingEntityRenderer<T, M> renderer, String mob) {
        super(renderer);
        eyes = RenderType.entityTranslucentEmissive(
                PoptartCore.location("textures/entity/lunar/" + mob + "_eyes.png"), false);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        register(event, EntityType.SKELETON, "skeleton");
        register(event, EntityType.ZOMBIE, "zombie");
        register(event, EntityType.CREEPER, "creeper");
    }

    private static <T extends LivingEntity> void register(
            EntityRenderersEvent.AddLayers event, EntityType<T> type, String mob) {
        EntityRenderer<T> renderer = event.getRenderer(type);
        if (renderer instanceof LivingEntityRenderer<T, ?> living) addLayer(living, mob);
    }

    private static <T extends LivingEntity, M extends EntityModel<T>> void addLayer(
            LivingEntityRenderer<T, M> renderer, String mob) {
        renderer.addLayer(new BloodMoonEyesLayer<>(renderer, mob));
    }

    @Override
    public void render(
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            T mob,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float age,
            float headYaw,
            float headPitch) {
        if (mob.isInvisible() || !(mob.level() instanceof ClientLevel level)) return;
        float strength = BloodMoonVisuals.nightStrength(level);
        if (strength <= 0) return;

        int color = FastColor.ARGB32.color(Math.round(strength * 255), GLOW_TINT);
        getParentModel()
                .renderToBuffer(
                        pose, buffers.getBuffer(eyes), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
    }
}
