package dev.poptartking.poptartcore.timekeeping;

import com.mojang.logging.LogUtils;
import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.backpack.BackpackAttachments;
import dev.poptartking.poptartcore.backpack.client.BackpackAttachmentClient;
import dev.poptartking.poptartcore.equipment.AccessoryInventory;
import dev.poptartking.poptartcore.equipment.client.UtilityClient;
import dev.poptartking.poptartcore.navigation.CompassAccess;
import dev.poptartking.poptartcore.registry.PoptartEquipmentBlocks;
import dev.poptartking.poptartcore.timekeeping.client.ClockHud;
import dev.poptartking.poptartcore.timekeeping.client.TimekeepingClient;
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
public final class TimekeepingControlsSmoke {
    private static int ticks;
    private static boolean done;

    private static KeyMapping key(int mode) throws Exception {
        var f = (mode == 3 ? UtilityClient.class : BackpackAttachmentClient.class)
                .getDeclaredField(mode == 3 ? "USE_UTILITY" : "USE_ATTACHMENT");
        f.setAccessible(true);
        return (KeyMapping) f.get(null);
    }

    private static ItemStack clock(Minecraft mc, int mode) {
        return mode == 0
                ? mc.player.getMainHandItem()
                : mode == 3
                        ? CompassAccess.carried(mc.player, Items.CLOCK)
                        : CompassAccess.attached(mc.player, Items.CLOCK);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("poptartcore.timekeepingSmoke")) return;
        var mc = Minecraft.getInstance();
        if (done || mc.player == null || mc.level == null || mc.screen != null) return;
        try {
            mc.setWindowActive(true);
            int mode = ticks / 120;
            int step = ++ticks % 120;
            if (step == 10) {
                final int target = mode;
                mc.getSingleplayerServer().execute(() -> {
                    var player = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                    AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, ItemStack.EMPTY);
                    AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.UTILITY_SLOT, ItemStack.EMPTY);
                    var clock = new ItemStack(Items.CLOCK);
                    player.setItemInHand(InteractionHand.MAIN_HAND, target == 0 ? clock : new ItemStack(Items.APPLE));
                    if (target == 3)
                        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.UTILITY_SLOT, clock);
                    else if (target > 0) {
                        var bag = new ItemStack(
                                (target == 1 ? PoptartEquipmentBlocks.BACKPACK : PoptartEquipmentBlocks.RUCKSACK)
                                        .get()
                                        .asItem());
                        BackpackAttachments.set(bag, 1, clock);
                        AccessoryInventory.get(player).setStackInSlot(AccessoryInventory.BACK_SLOT, bag);
                    }
                });
            }
            if (step == 35) {
                require(clock(mc, mode).is(Items.CLOCK), "Clock synchronized for mode " + mode);
                var tooltip = new java.util.ArrayList<net.minecraft.network.chat.Component>();
                ClockHud.appendTooltip(clock(mc, mode), tooltip::add);
                require(!tooltip.isEmpty(), "Clock tooltip shows time");
                require(
                        ClockHud.format(0).equals("06:00")
                                && ClockHud.format(18000).equals("00:00"),
                        "Twenty-four-hour clock formatting");
            }
            if (step == 40 || step == 70) {
                if (mode == 0) mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                else key(mode).setDown(true);
            }
            if ((step == 43 || step == 90) && mode > 0) key(mode).setDown(false);
            if (step == 60) {
                net.minecraft.client.Screenshot.grab(
                        mc.gameDirectory, "timekeeping-mode-" + mode + ".png", mc.getMainRenderTarget(), message -> {});
                require(
                        ((dev.poptartking.poptartcore.mixin.accessor.DepthMeterGuiAccessor) mc.gui)
                                        .poptartcore$overlayMessageTime()
                                == 0,
                        "Clock overlays do not cover the stopwatch HUD");
                require(Stopwatch.isRunning(clock(mc, mode)), "First use starts stopwatch for mode " + mode);
                require(
                        !TimekeepingClient.runningClock(mc.player).isEmpty(),
                        "Running stopwatch found for HUD in mode " + mode);
                require(Stopwatch.getElapsed(mc.level, clock(mc, mode)) > 0, "Timer advances for mode " + mode);
            }
            if (step == 84)
                require(
                        TimekeepingClient.runningClock(mc.player).isEmpty(),
                        "Stopping while still holding G/Z clears the working timer");
            if (step == 100) {
                require(!Stopwatch.isRunning(clock(mc, mode)), "Second use stops stopwatch for mode " + mode);
                if (mode > 0) require(mc.player.getMainHandItem().is(Items.APPLE), "Working clock preserved hand");
                LogUtils.getLogger().info("TIMEKEEPING_MODE_PASS mode={} start stop tooltip elapsed hand", mode);
                if (mode == 3) {
                    LogUtils.getLogger().info("TIMEKEEPING_CONTROLS_PASS");
                    done = true;
                    mc.stop();
                }
            }
        } catch (Throwable e) {
            LogUtils.getLogger().error("TIMEKEEPING_CONTROLS_FAIL tick={}", ticks, e);
            done = true;
            mc.stop();
        }
    }
}
