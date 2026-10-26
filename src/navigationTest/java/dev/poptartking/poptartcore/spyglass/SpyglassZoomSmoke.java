package dev.poptartking.poptartcore.spyglass;

import com.mojang.logging.LogUtils;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.backpack.BackpackAttachments;
import dev.poptartking.poptartcore.backpack.client.BackpackAttachmentClient;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.equipment.client.UtilityClient;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import dev.poptartking.poptartcore.spyglass.client.SpyglassZoom;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class SpyglassZoomSmoke {
    private static int ticks;
    private static boolean done;

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static KeyMapping key(int mode) throws Exception {
        var f = (mode == 3 ? UtilityClient.class : BackpackAttachmentClient.class)
                .getDeclaredField(mode == 3 ? "USE_UTILITY" : "USE_ATTACHMENT");
        f.setAccessible(true);
        return (KeyMapping) f.get(null);
    }

    private static void scroll(Minecraft mc, double amount) throws Exception {
        var method = net.minecraft.client.MouseHandler.class.getDeclaredMethod(
                "onScroll", long.class, double.class, double.class);
        method.setAccessible(true);
        method.invoke(mc.mouseHandler, mc.getWindow().getWindow(), 0D, amount);
    }

    private static void zoom(Minecraft mc, float expected) {
        require(
                Math.abs(mc.player.getFieldOfViewModifier() - expected) < .0001F,
                "Actual player FOV matches zoom level " + expected);
        require(mc.player.getInventory().selected == 0, "Zoom scroll preserves hotbar selection");
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("poptartcore.spyglassSmoke")) return;
        var mc = Minecraft.getInstance();
        if (done || mc.player == null || mc.level == null || mc.screen != null) return;
        try {
            mc.setWindowActive(true);
            int mode = ticks / 120;
            int step = ++ticks % 120;
            if (step == 10) {
                mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
                mc.options.fovEffectScale().set(1D);
                final int target = mode;
                mc.getSingleplayerServer().execute(() -> {
                    var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                    player.getInventory().clearContent();
                    player.getInventory().selected = 0;
                    var slots = AccessoryInventory.get(player);
                    slots.setStackInSlot(AccessoryInventory.BACK_SLOT, ItemStack.EMPTY);
                    slots.setStackInSlot(AccessoryInventory.UTILITY_SLOT, ItemStack.EMPTY);
                    var spyglass = new ItemStack(Items.SPYGLASS);
                    player.setItemInHand(
                            InteractionHand.MAIN_HAND, target == 0 ? spyglass : new ItemStack(Items.APPLE));
                    if (target == 3) slots.setStackInSlot(AccessoryInventory.UTILITY_SLOT, spyglass);
                    else if (target > 0) {
                        var bag = new ItemStack((target == 1 ? PoptartCoreBlocks.BACKPACK : PoptartCoreBlocks.RUCKSACK)
                                .get()
                                .asItem());
                        BackpackAttachments.set(bag, 1, spyglass);
                        slots.setStackInSlot(AccessoryInventory.BACK_SLOT, bag);
                    }
                });
            }
            if (step == 40 || step == 90) {
                mc.player.getInventory().selected = 0;
                if (mode == 0) {
                    mc.options.keyUse.setDown(true);
                    mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                } else key(mode).setDown(true);
            }
            if (step == 45) {
                require(SpyglassZoom.isZooming(), "Spyglass scopes for mode " + mode);
                zoom(mc, .1F);
            }
            if (step == 46) {
                scroll(mc, 1);
                zoom(mc, .05F);
            }
            if (step == 52) {
                var f = net.minecraft.client.renderer.GameRenderer.class.getDeclaredField("fov");
                f.setAccessible(true);
                require(f.getFloat(mc.gameRenderer) < .1F, "Renderer permits stronger-than-vanilla zoom");
                scroll(mc, 1);
                zoom(mc, .05F);
            }
            if (step == 54) {
                scroll(mc, -1);
                zoom(mc, .1F);
            }
            if (step == 55) {
                scroll(mc, -1);
                zoom(mc, .2F);
            }
            if (step == 56) {
                scroll(mc, -1);
                zoom(mc, .4F);
            }
            if (step == 57) {
                scroll(mc, -1);
                zoom(mc, .4F);
            }
            if (step == 70 || step == 100) {
                if (mode == 0) {
                    mc.options.keyUse.setDown(false);
                    mc.gameMode.releaseUsingItem(mc.player);
                } else key(mode).setDown(false);
            }
            if (step == 76) {
                require(!SpyglassZoom.isZooming(), "Scope released for mode " + mode);
                scroll(mc, 1);
                require(mc.player.getInventory().selected != 0, "Mouse wheel changes hotbar normally outside scope");
                mc.player.getInventory().selected = 0;
            }
            if (step == 95) zoom(mc, .1F);
            if (step == 110) {
                if (mode > 0) require(mc.player.getMainHandItem().is(Items.APPLE), "Original hand preserved");
                LogUtils.getLogger()
                        .info("SPYGLASS_ZOOM_MODE_PASS mode={} four-levels bounds renderer reset scroll hand", mode);
                if (mode == 3) {
                    LogUtils.getLogger().info("SPYGLASS_ZOOM_PASS");
                    done = true;
                    mc.stop();
                }
            }
        } catch (Throwable e) {
            LogUtils.getLogger().error("SPYGLASS_ZOOM_FAIL tick={}", ticks, e);
            done = true;
            mc.stop();
        }
    }
}
