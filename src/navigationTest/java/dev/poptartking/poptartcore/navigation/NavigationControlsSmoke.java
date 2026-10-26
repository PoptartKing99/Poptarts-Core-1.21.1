package dev.poptartking.poptartcore.navigation;

import com.mojang.logging.LogUtils;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.backpack.BackpackAttachments;
import dev.poptartking.poptartcore.backpack.client.BackpackAttachmentClient;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.navigation.client.hud.CompassHud;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in live client regression. Never packaged in the mod JAR. */
@EventBusSubscriber(modid = PoptartCore.MOD_ID, value = Dist.CLIENT)
public final class NavigationControlsSmoke {
    private static int ticks;
    private static boolean done;

    private static Object field(String name) throws Exception {
        var f = CompassHud.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(null);
    }

    private static KeyMapping attachmentKey() throws Exception {
        var f = BackpackAttachmentClient.class.getDeclaredField("USE_ATTACHMENT");
        f.setAccessible(true);
        return (KeyMapping) f.get(null);
    }

    private static ItemStack compass(Minecraft mc, int mode) {
        return mode == 0 ? mc.player.getMainHandItem() : CompassAccess.attached(mc.player, Items.COMPASS);
    }

    private static void require(boolean condition, String reason) {
        if (!condition) throw new AssertionError(reason);
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (Boolean.getBoolean("poptartcore.timekeepingSmoke")
                || Boolean.getBoolean("poptartcore.corpseMarkerSmoke")
                || Boolean.getBoolean("poptartcore.spyglassSmoke")) return;
        var mc = Minecraft.getInstance();
        if (done || mc.player == null || mc.level == null || mc.screen != null) return;
        try {
            // Exercise the real G handler even when a developer is reading the test log in another window.
            mc.setWindowActive(true);
            int mode = ticks / 120;
            int step = ++ticks % 120;
            if (step == 10) {
                final int source = mode;
                mc.getSingleplayerServer().execute(() -> {
                    var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                    var meter = new ItemStack(Items.COMPASS);
                    Waypoints.refresh(meter, player);
                    var pos = player.blockPosition().offset(20, 0, 20);
                    player.serverLevel().setBlock(pos, Blocks.LODESTONE.defaultBlockState(), 3);
                    var key = GlobalPos.of(player.level().dimension(), pos);
                    var mark = new LodestoneMark("Client test", "default");
                    LodestoneRegistry.get(player.server).set(key, mark);
                    Waypoints.bindLodestone(meter, player, key, mark);
                    AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, ItemStack.EMPTY);
                    if (source == 0) player.setItemInHand(InteractionHand.MAIN_HAND, meter);
                    else {
                        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE));
                        var bag = new ItemStack((source == 1 ? PoptartCoreBlocks.BACKPACK : PoptartCoreBlocks.RUCKSACK)
                                .get()
                                .asItem());
                        BackpackAttachments.set(bag, 1, meter);
                        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, bag);
                    }
                });
            }
            if (step == 35)
                require(
                        Waypoints.get(compass(mc, mode)).size() >= 2,
                        "Client received target component for mode " + mode);
            if (step == 40 || step == 70) {
                if (mode == 0) {
                    mc.options.keyUse.setDown(true);
                    mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                } else attachmentKey().setDown(true);
            }
            if (step == 43 || step == 90) {
                if (mode == 0) {
                    mc.options.keyUse.setDown(false);
                    mc.gameMode.releaseUsingItem(mc.player);
                } else attachmentKey().setDown(false);
            }
            if (step == 60)
                require(Waypoints.get(compass(mc, mode)).navigationHidden(), "Tap toggles navigation for mode " + mode);
            if (step == 75) {
                var origin = Waypoints.get(compass(mc, mode))
                        .waypoints()
                        .getFirst()
                        .pos()
                        .pos();
                mc.player.setYRot((float) Math.toDegrees(
                        Math.atan2(-(origin.getX() + .5 - mc.player.getX()), origin.getZ() + .5 - mc.player.getZ())));
            }
            if (step == 80) {
                require((float) field("open") > .4F, "Hold opens selector for mode " + mode);
                require((int) field("highlighted") == 0, "Facing origin highlights it for mode " + mode);
                require(CompassHud.tryHide(), "Left-click hide recognized for mode " + mode);
            }
            if (step == 84) {
                require(Waypoints.get(compass(mc, mode)).isHidden(0), "Hide reached actual compass for mode " + mode);
                require(CompassHud.tryHide(), "Left-click restore recognized for mode " + mode);
            }
            if (step == 88) require(!Waypoints.get(compass(mc, mode)).isHidden(0), "Marker restored for mode " + mode);
            if (step == 110) {
                require(
                        Waypoints.get(compass(mc, mode)).getSelectedIndex() == 0,
                        "Release selects highlighted origin for mode " + mode);
                if (mode > 0) require(mc.player.getMainHandItem().is(Items.APPLE), "G preserved original hand");
                LogUtils.getLogger().info("NAVIGATION_CONTROL_MODE_PASS mode={} tap hold hide restore select", mode);
                if (mode == 2) {
                    LogUtils.getLogger().info("NAVIGATION_CONTROLS_PASS");
                    done = true;
                    mc.stop();
                }
            }
        } catch (Throwable e) {
            LogUtils.getLogger().error("NAVIGATION_CONTROLS_FAIL tick={}", ticks, e);
            done = true;
            mc.stop();
        }
    }
}
