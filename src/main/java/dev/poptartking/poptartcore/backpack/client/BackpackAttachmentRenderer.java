package dev.poptartking.poptartcore.backpack.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.backpack.BackpackAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class BackpackAttachmentRenderer {
    private static final float[][] BACKPACK = {{12.5F, 9, 9, -170, -77.5F, -180}, {3.5F, 9, 9, -170, 77.5F, 180}};
    private static final float[][] RUCKSACK = {
        {13.75F, 13.5F, 11.25F, -167.5F, -70, -180},
        {2.25F, 13.5F, 11.25F, -167.5F, 70, 180},
        {13.25F, 2, 13, -167.5F, -47.5F, -180},
        {2.75F, 2, 13, -167.5F, 47.5F, 180}
    };

    private static ModelResourceLocation model(String name) {
        return ModelResourceLocation.standalone(PoptartCore.location("item/backpack_attachments/" + name));
    }

    @SubscribeEvent
    public static void models(ModelEvent.RegisterAdditional event) {
        for (var name : java.util.List.of(
                "compass",
                "clock_day",
                "clock_night",
                "recovery_compass",
                "heavy_core",
                "bundle",
                "rucksack_banner",
                "spyglass")) event.register(model(name));
    }

    public static void render(
            boolean rucksack,
            ItemContainerContents contents,
            PoseStack pose,
            MultiBufferSource buffers,
            int light,
            int overlay,
            Level level,
            boolean hidePrimary) {
        var items = BackpackAttachments.items(contents);
        var renderer = Minecraft.getInstance().getItemRenderer();
        for (int i = 0; i < BackpackAttachments.slots(rucksack); i++) {
            var stack = items.get(i);
            if (!BackpackAttachments.fits(rucksack, i, stack) || hidePrimary && i == 1) continue;
            String special =
                    switch (BackpackAttachments.id(stack)) {
                        case "minecraft:compass" -> "compass";
                        case "minecraft:recovery_compass" -> "recovery_compass";
                        case "minecraft:clock" ->
                            level != null && Math.floorMod(level.getDayTime(), 24000) >= 12000
                                    ? "clock_night"
                                    : "clock_day";
                        case "minecraft:heavy_core" -> "heavy_core";
                        case "minecraft:bundle" -> "bundle";
                        case "minecraft:spyglass" -> "spyglass";
                        default -> i == 0 ? "rucksack_banner" : null;
                    };
            pose.pushPose();
            if ("spyglass".equals(special)) {
                // Upper mounts run diagonally between pouches. Bottom mounts hang upright at the front corners.
                boolean right = i % 2 == 1;
                boolean lower = i >= 3;
                float edge = rucksack ? (lower ? 15.2F : 15.9F) : 14.7F;
                float height = rucksack ? (lower ? 6F : 15.5F) : 9F;
                float depth = rucksack ? (lower ? 14F : 11F) : 10.5F;
                pose.translate((right ? edge : 16F - edge) / 16F, height / 16F, depth / 16F);
                if (!lower) {
                    pose.mulPose(Axis.XP.rotationDegrees(-45));
                    pose.mulPose(Axis.ZP.rotationDegrees(right ? -8 : 8));
                }
            } else if (i > 0) {
                var mount = (rucksack ? RUCKSACK : BACKPACK)[i - 1];
                pose.translate(mount[0] / 16, mount[1] / 16, mount[2] / 16);
                pose.mulPose(Axis.XP.rotationDegrees(mount[3]));
                pose.mulPose(Axis.YP.rotationDegrees(mount[4]));
                pose.mulPose(Axis.ZP.rotationDegrees(mount[5]));
                pose.translate(0, 0, -1.0 / 16);
            }
            if (special != null) {
                if (!"spyglass".equals(special)) pose.translate(.5, .5, .5);
                renderer.render(
                        stack,
                        "spyglass".equals(special) ? ItemDisplayContext.HEAD : ItemDisplayContext.NONE,
                        false,
                        pose,
                        buffers,
                        light,
                        overlay,
                        Minecraft.getInstance().getModelManager().getModel(model(special)));
            } else {
                pose.scale(.4F, .4F, .4F);
                renderer.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffers, level, 0);
            }
            pose.popPose();
        }
    }
}
