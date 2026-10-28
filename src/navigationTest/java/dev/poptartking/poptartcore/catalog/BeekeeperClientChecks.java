package dev.poptartking.poptartcore.catalog;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.armor.client.PoptartCoreModelLayers;
import dev.poptartking.poptartcore.registry.PoptartArmorItems;
import dev.poptartking.poptartcore.registry.PoptartCoreMenus;
import dev.poptartking.poptartcore.workbench.client.WorkbenchScreen;
import dev.poptartking.poptartcore.workbench.menu.WorkbenchMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class BeekeeperClientChecks {
    private BeekeeperClientChecks() {}

    static void verify(Minecraft mc) throws Exception {
        var model = PoptartCoreModelLayers.BEEKEEPER_ARMOR_MODEL;
        check(model != null, "Beekeeper layer is registered and baked");
        check(
                !model.head.getChild("hood").isEmpty()
                        && model.head.getChild("hood").hasChild("flap"),
                "Hood geometry");
        check(
                !model.body.getChild("jacket").isEmpty()
                        && !model.leggings.getChild("skirt").isEmpty(),
                "Jacket and skirt geometry");
        check(
                !model.rightFoot.getChild("right_boot").isEmpty()
                        && !model.leftFoot.getChild("left_boot").isEmpty(),
                "Both boots have geometry");
        check(
                !model.rightArm.getChild("right_sleeve").isEmpty()
                        && !model.leftArm.getChild("left_sleeve").isEmpty(),
                "Both sleeves have geometry");
        var texture = PoptartCore.location("textures/armor/beekeeper_armor.png");
        try (var stream = mc.getResourceManager()
                        .getResource(texture)
                        .orElseThrow()
                        .open();
                var image = NativeImage.read(stream)) {
            check(image.getWidth() == 64 && image.getHeight() == 64, "Beekeeper armor uses the 64 by 64 texture");
        }
        var renderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(mc.player);
        var original = mc.player.getItemBySlot(EquipmentSlot.CHEST);
        var armorType = RenderType.armorCutoutNoCull(texture);
        try {
            for (boolean right : new boolean[] {false, true}) {
                for (boolean equipped : new boolean[] {false, true}) {
                    mc.player.setItemSlot(
                            EquipmentSlot.CHEST,
                            equipped
                                    ? new ItemStack(PoptartArmorItems.BEEKEEPER_CHESTPLATE.get())
                                    : new ItemStack(Items.IRON_CHESTPLATE));
                    var armor = new Vertices();
                    var other = new Vertices();
                    MultiBufferSource buffers = type -> type == armorType ? armor : other;
                    if (right) renderer.renderRightHand(new PoseStack(), buffers, LightTexture.FULL_BRIGHT, mc.player);
                    else renderer.renderLeftHand(new PoseStack(), buffers, LightTexture.FULL_BRIGHT, mc.player);
                    check(
                            equipped ? armor.count > 0 : armor.count == 0,
                            "Actual first-person sleeve hook for side=" + right + " equipped=" + equipped);
                    if (equipped)
                        check(armor.maxU > .9F && armor.maxU <= 1, "Sleeve UVs use the intended texture dimensions");
                }
            }
        } finally {
            mc.player.setItemSlot(EquipmentSlot.CHEST, original);
        }
        var factory =
                MenuScreens.getScreenFactory(PoptartCoreMenus.WORKBENCH.get()).orElseThrow();
        var screen = factory.create(
                new WorkbenchMenu(1, mc.player.getInventory()), mc.player.getInventory(), Component.literal("test"));
        check(screen instanceof WorkbenchScreen, "Workbench screen factory is registered");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class Vertices implements VertexConsumer {
        int count;
        float maxU;

        public VertexConsumer addVertex(float x, float y, float z) {
            count++;
            return this;
        }

        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        public VertexConsumer setUv(float u, float v) {
            maxU = Math.max(maxU, u);
            return this;
        }

        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        public VertexConsumer setNormal(float x, float y, float z) {
            return this;
        }
    }
}
