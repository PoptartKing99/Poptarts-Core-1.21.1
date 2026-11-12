package dev.poptartking.poptartcore.eclipse;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.farcr.nomansland.client.renderer.SunDogRenderer;
import com.farcr.nomansland.common.worldevent.SunDog;
import com.mojang.logging.LogUtils;
import dev.poptartking.poptartcore.eclipse.client.EclipseVisuals;
import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = "poptartcore", value = Dist.CLIENT)
public final class EclipseVisualSmoke {
    private static final EclipsePhase[] PHASES = {EclipsePhase.CLEAR, EclipsePhase.NEW, EclipsePhase.WANING_GIBBOUS,
            EclipsePhase.WANING_HALF, EclipsePhase.WANING_CRESCENT, EclipsePhase.TOTAL,
            EclipsePhase.WAXING_CRESCENT, EclipsePhase.WAXING_HALF, EclipsePhase.WAXING_GIBBOUS, EclipsePhase.CLEAR};
    private static final EclipsePhase[] NATURAL_PHASES = {EclipsePhase.WANING_GIBBOUS, EclipsePhase.WANING_HALF,
            EclipsePhase.WANING_CRESCENT, EclipsePhase.TOTAL, EclipsePhase.WAXING_CRESCENT,
            EclipsePhase.WAXING_HALF, EclipsePhase.WAXING_GIBBOUS, EclipsePhase.CLEAR};
    private static final int[] NATURAL_TIMES = {1000, 2600, 4200, 6000, 6200, 8100, 10000, 12000};
    private static int ticks;
    private static boolean done;
    private static int baselineSky;
    private static int baselineTorch;
    private static double baselineCloud;
    private static double coveredMoon;
    private static double clearMoon;
    private static volatile EclipsePhase expectedNatural;
    private static long naturalDawn;
    private static volatile Throwable serverFailure;
    private static long clockProbeGameTime;
    private static long clockProbeDayTime;
    private static volatile boolean betterDaysPassed;

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (!done && serverFailure != null) {
            done = true;
            LogUtils.getLogger().error("ECLIPSE_CLIENT_FAIL", serverFailure);
            mc.stop();
        }
        if (done || mc.player == null || mc.level == null || (mc.screen != null && !mc.player.isSleeping())) return;
        try {
            ticks++;
            mc.options.pauseOnLostFocus = false;
            mc.options.hideGui = true;
            mc.options.gamma().set(0.5);
            if (ticks == 1) {
                server(server -> {
                    var level = server.overworld();
                    level.setDayTime(6000);
                    level.setWeatherParameters(0, 100_000, false, false);
                    level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
                    for (int x = 292; x <= 308; x++) for (int z = 292; z <= 308; z++) {
                        level.setBlock(new BlockPos(x, 120, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                        for (int y = 121; y < 129; y++) level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                    }
                    for (int x = 297; x <= 303; x++) for (int y = 121; y <= 124; y++)
                        level.setBlock(new BlockPos(x, y, 298), (x < 300 ? Blocks.WHITE_CONCRETE : Blocks.OAK_PLANKS).defaultBlockState(), 3);
                    level.setBlock(new BlockPos(302, 121, 300), Blocks.TORCH.defaultBlockState(), 3);
                    var player = server.getPlayerList().getPlayer(mc.player.getUUID());
                    player.setGameMode(GameType.SPECTATOR);
                    player.teleportTo(level, 300.5, 123, 307, 180, 10);
                    command(server, "stop");
                    execute(server, "fillbiome 292 112 292 308 136 308 minecraft:snowy_slopes");
                    SunDog.getOrDefault(level).forceStart();
                });
            }
            int sceneTick = ticks - 100;
            if (sceneTick >= 0 && sceneTick < PHASES.length * 60) {
                int index = sceneTick / 60;
                int frame = sceneTick % 60;
                if (frame == 0) server(server -> command(server, "phase " + PHASES[index].id()));
                mc.player.setPos(300.5, 123, 307);
                mc.player.setYRot(180);
                mc.player.setXRot(frame >= 32 ? -89 : 10);
                if (frame == 25) {
                    check(EclipseVisuals.visiblePhase() == PHASES[index], "Phase packet not applied: " + PHASES[index]);
                    check(SunDogRenderer.SUN_DOG_SHADER != null && SunDog.Client.INSTANCE.getOpacity(1) > 0.5F,
                            "The forced sun dog must be active and visible in this cold-biome fixture");
                    resetSunDogMesh();
                    int sky = lightmap(mc).getPixelRGBA(0, 15);
                    int torch = lightmap(mc).getPixelRGBA(15, 15);
                    var cloud = mc.level.getCloudColor(1);
                    if (index == 0) { baselineSky = sky; baselineTorch = torch; baselineCloud = cloud.length(); }
                    LogUtils.getLogger().info("ECLIPSE_CLOUD {} color={} brightness={}", PHASES[index].id(), cloud, EclipseVisuals.brightness());
                    if (PHASES[index] == EclipsePhase.TOTAL) {
                        check(mc.level.getSkyDarken() == 13 && !mc.level.isDay(), "Client gameplay daylight did not match totality");
                        check(luminance(sky) < luminance(baselineSky) * 0.6, "Actual sky lightmap did not darken");
                        check(luminance(torch) > luminance(baselineTorch) * 0.8, "Bright block light was lost");
                        check(cloud.length() < baselineCloud * 0.2, "Cloud color bypassed eclipse dimming");
                    }
                    if (index == PHASES.length - 1) check(Math.abs(luminance(sky) - luminance(baselineSky)) < 3,
                            "Stopping failed to restore the original sky lightmap");
                    LogUtils.getLogger().info("ECLIPSE_PHASE {} sky={} torch={}", PHASES[index].id(), luminance(sky), luminance(torch));
                    screenshot(mc, index + "-" + PHASES[index].id() + "-land");
                }
                if (frame == 50) {
                    screenshot(mc, index + "-" + PHASES[index].id() + "-sun");
                    boolean rendered = sunDogMesh().get(SunDogRenderer.INSTANCE) != null;
                    check(rendered == (PHASES[index].coverage() == 0),
                            "Sun dog should only render with an unobscured sun: " + PHASES[index].id());
                    LogUtils.getLogger().info("ECLIPSE_SUN_DOG {} rendered={}", PHASES[index].id(), rendered);
                }
                if (frame >= 52) mc.player.setXRot(-40);
                if (frame == 58) screenshot(mc, index + "-" + PHASES[index].id() + "-clouds");
            }
            int after = sceneTick - PHASES.length * 60;
            if (after == 0) server(server -> {
                command(server, "phase total");
                server.overworld().setDayTime(18000);
                server.forceTimeSynchronization();
                server.getPlayerList().getPlayer(mc.player.getUUID())
                        .teleportTo(server.overworld(), 300.5, 360, 307, 180, -89);
            });
            if (after >= 0 && after <= 85) {
                mc.player.setPos(300.5, 360, 307);
                mc.player.setYRot(180);
                mc.player.setXRot(-89);
            }
            if (after == 30) {
                check(EclipseVisuals.visiblePhase() == EclipsePhase.TOTAL && EclipseVisuals.darkness() == 0,
                        "An eclipse must not darken midnight further");
                coveredMoon = centralSkyLuminance(mc);
                screenshot(mc, "night-moon-total");
                server(server -> command(server, "stop"));
            }
            if (after == 50) {
                check(EclipseVisuals.visiblePhase() == EclipsePhase.CLEAR, "Moon restoration phase packet not applied");
                clearMoon = centralSkyLuminance(mc);
                check(clearMoon > coveredMoon + 25, "Clear sky must restore the moon after totality");
                screenshot(mc, "night-moon-clear");
                server(server -> command(server, "phase waning_gibbous"));
            }
            if (after == 70) {
                check(EclipseVisuals.visiblePhase() == EclipsePhase.WANING_GIBBOUS && EclipseVisuals.darkness() == 0,
                        "Partial eclipses must remain active at night without darkening it");
                double partialMoon = centralSkyLuminance(mc);
                check(partialMoon < clearMoon * 0.25, "The moon must stay hidden on partial-eclipse nights");
                screenshot(mc, "night-moon-partial");
                LogUtils.getLogger().info("ECLIPSE_MOON clear={} total={} partial={}", clearMoon, coveredMoon, partialMoon);
                server(server -> command(server, "stop"));
            }
            if (after == 85) {
                check(EclipseVisuals.visiblePhase() == EclipsePhase.CLEAR && centralSkyLuminance(mc) > clearMoon * 0.8,
                        "The moon must return when the partial eclipse clears");
                screenshot(mc, "night-moon-restored");
                server(server -> {
                    command(server, "phase total");
                    server.getPlayerList().getPlayer(mc.player.getUUID())
                            .teleportTo(server.getLevel(Level.NETHER), 0, 120, 0, 0, 0);
                });
            }
            if (after == 100) {
                check(mc.level.dimension() == Level.NETHER && EclipseVisuals.visiblePhase() == EclipsePhase.CLEAR
                        && EclipseVisuals.brightness() == 1, "Eclipse leaked into the Nether");
                server(server -> {
                    server.overworld().setDayTime(6000);
                    server.getPlayerList().getPlayer(mc.player.getUUID()).teleportTo(server.overworld(), 300.5, 123, 307, 180, 10);
                });
            }
            if (after == 170) {
                check(mc.level.dimension() == Level.OVERWORLD && EclipseVisuals.visiblePhase() == EclipsePhase.TOTAL,
                        "Returning to the Overworld did not restore the active phase");
                server(server -> command(server, "start 1"));
            }
            if (after == 230) {
                check(EclipseVisuals.visiblePhase() == EclipsePhase.CLEAR, "Timed event failed to end on the client");
                server(server -> {
                    var level = server.overworld();
                    level.setDayTime(6000);
                    command(server, "natural on");
                    for (int day = 0; day < 200 && !(EclipseData.get(server).state() instanceof EclipseState.Daylight); day++) {
                        level.setDayTime(level.getDayTime() + 24000);
                        EclipseEvents.sync(server);
                    }
                    naturalDawn = Math.floorDiv(level.getDayTime(), 24000) * 24000;
                    level.setDayTime(naturalDawn + NATURAL_TIMES[0]);
                    EclipseEvents.sync(server);
                    expectedNatural = EclipseLighting.phase(level);
                    check(EclipseData.get(server).state() instanceof EclipseState.Daylight
                            && expectedNatural.coverage() > 0 && expectedNatural.coverage() <= 3,
                            "The natural schedule failed to produce a morning eclipse");
                    server.forceTimeSynchronization();
                });
            }
            if (after >= 290 && after <= 430 && (after - 290) % 20 == 0) {
                int step = (after - 290) / 20;
                check(expectedNatural != null && EclipseVisuals.visiblePhase() == NATURAL_PHASES[step],
                        "Natural event lost its daylight sequence at time " + NATURAL_TIMES[step]);
                LogUtils.getLogger().info("ECLIPSE_NATURAL_TIME {} phase={}", NATURAL_TIMES[step], EclipseVisuals.visiblePhase().id());
                screenshot(mc, "natural-" + NATURAL_TIMES[step] + "-" + NATURAL_PHASES[step].id());
                server(server -> {
                    var level = server.overworld();
                    if (step < NATURAL_PHASES.length - 1) {
                        level.setDayTime(naturalDawn + NATURAL_TIMES[step + 1]);
                        EclipseEvents.sync(server);
                        server.forceTimeSynchronization();
                    } else {
                        check(Math.floorDiv(level.getDayTime(), 24000) == Math.floorDiv(naturalDawn, 24000),
                                "All visible phases must complete on the same calendar day");
                        level.setDayTime(naturalDawn + 18000);
                        EclipseEvents.sync(server);
                        check(EclipseData.get(server).state() instanceof EclipseState.Waiting waiting
                                && waiting.clearDaysRemaining() >= 20 && waiting.clearDaysRemaining() <= 30,
                                "The completed daylight event did not schedule a rare clear period");
                        server.forceTimeSynchronization();
                        command(server, "natural off");
                    }
                });
            }
            if (after == 460) {
                check(EclipseVisuals.visiblePhase() == EclipsePhase.CLEAR, "Natural off did not clear the client phase");
                if (ModList.get().isLoaded("betterdays")) {
                    server(server -> {
                        var level = server.overworld();
                        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, server);
                        beginClockProbe(level, 6000);
                        server.forceTimeSynchronization();
                    });
                } else {
                    finish(mc);
                }
            }
            if (ModList.get().isLoaded("betterdays")) {
                if (after == 520) {
                    check(Math.abs(mc.level.getDayTimePerTick() - 1F / 9) < 0.0001, "The client did not receive the ninety-minute day rate");
                    server(server -> {
                        var level = server.overworld();
                        checkClockProbe(level, 1.0 / 9, "day");
                        beginClockProbe(level, 18000);
                        server.forceTimeSynchronization();
                    });
                }
                if (after == 580) {
                    check(Math.abs(mc.level.getDayTimePerTick() - 1F / 3) < 0.0001, "The client did not receive the thirty-minute night rate");
                    server(server -> {
                        var level = server.overworld();
                        checkClockProbe(level, 1.0 / 3, "night");
                        var foot = new BlockPos(300, 121, 303);
                        var head = foot.south();
                        var bed = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH);
                        level.setBlock(foot, bed.setValue(BedBlock.PART, BedPart.FOOT), 3);
                        level.setBlock(head, bed.setValue(BedBlock.PART, BedPart.HEAD), 3);
                        var player = server.getPlayerList().getPlayer(mc.player.getUUID());
                        player.setGameMode(GameType.SURVIVAL);
                        player.teleportTo(level, 300.5, 121, 302.5, 0, 15);
                        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                        player.setRespawnPosition(level.dimension(), null, 0, false, false);
                        var hit = new BlockHitResult(foot.getCenter(), Direction.UP, foot, false);
                        player.gameMode.useItemOn(player, level, ItemStack.EMPTY, InteractionHand.MAIN_HAND, hit);
                        check(player.isSleeping() && head.equals(player.getRespawnPosition()), "Normal bed interaction must allow sleeping and set respawn");
                        clockProbeGameTime = level.getGameTime();
                        clockProbeDayTime = level.getDayTime();
                    });
                }
                if (after == 730) server(server -> {
                    var level = server.overworld();
                    var player = server.getPlayerList().getPlayer(mc.player.getUUID());
                    long elapsed = level.getGameTime() - clockProbeGameTime;
                    check(elapsed >= 100 && !player.isSleeping(), "Vanilla sleeping did not complete after its normal sleep timer");
                    check(level.getDayTime() >= 24000 && level.getDayTime() < 25000, "Vanilla sleeping must skip to the next morning");
                    LogUtils.getLogger().info("ECLIPSE_BETTER_DAYS_PASS sleptFor={} before={} morning={}", elapsed, clockProbeDayTime, level.getDayTime());
                    betterDaysPassed = true;
                });
                if (after == 750) {
                    check(betterDaysPassed && !mc.player.isSleeping(), "The client must wake after the server's normal night skip");
                    finish(mc);
                }
            }
        } catch (Throwable error) {
            done = true;
            LogUtils.getLogger().error("ECLIPSE_CLIENT_FAIL", error);
            mc.stop();
        }
    }

    private static void beginClockProbe(ServerLevel level, long time) {
        level.setDayTime(time);
        clockProbeDayTime = time;
        clockProbeGameTime = level.getGameTime();
    }

    private static void checkClockProbe(ServerLevel level, double rate, String period) {
        long elapsed = level.getGameTime() - clockProbeGameTime;
        long advanced = level.getDayTime() - clockProbeDayTime;
        check(elapsed >= 20 && Math.abs(advanced - elapsed * rate) <= 2, "Unexpected actual " + period + " clock speed: " + advanced + "/" + elapsed);
        check(Math.abs(level.getDayTimePerTick() - rate) < 0.0001, "Unexpected server " + period + " rate");
        LogUtils.getLogger().info("ECLIPSE_BETTER_DAYS_RATE {} advanced={} elapsed={} rate={}", period, advanced, elapsed, level.getDayTimePerTick());
    }

    private static void finish(Minecraft mc) {
        done = true;
        LogUtils.getLogger().info("ECLIPSE_CLIENT_PASS");
        mc.stop();
    }

    private static NativeImage lightmap(Minecraft mc) throws ReflectiveOperationException {
        var texture = mc.gameRenderer.lightTexture();
        var pixels = texture.getClass().getDeclaredField("lightPixels");
        pixels.setAccessible(true);
        return (NativeImage) pixels.get(texture);
    }

    private static java.lang.reflect.Field sunDogMesh() throws ReflectiveOperationException {
        var field = SunDogRenderer.class.getDeclaredField("sunDogMesh");
        field.setAccessible(true);
        return field;
    }

    private static void resetSunDogMesh() throws ReflectiveOperationException {
        var field = sunDogMesh();
        var mesh = (VertexBuffer) field.get(SunDogRenderer.INSTANCE);
        if (mesh != null) mesh.close();
        field.set(SunDogRenderer.INSTANCE, null);
    }

    private static float luminance(int color) {
        return FastColor.ABGR32.red(color) * 0.299F + FastColor.ABGR32.green(color) * 0.587F + FastColor.ABGR32.blue(color) * 0.114F;
    }

    private static double centralSkyLuminance(Minecraft mc) {
        try (var pixels = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            int radius = Math.max(8, pixels.getHeight() / 40);
            int centerX = pixels.getWidth() / 2;
            int centerY = pixels.getHeight() / 2;
            double sum = 0;
            for (int y = centerY - radius; y <= centerY + radius; y++)
                for (int x = centerX - radius; x <= centerX + radius; x++) sum += luminance(pixels.getPixelRGBA(x, y));
            return sum / ((radius * 2 + 1) * (radius * 2 + 1));
        }
    }

    private static void screenshot(Minecraft mc, String name) {
        Screenshot.grab(new File("."), "eclipse-" + name + ".png", mc.getMainRenderTarget(), text -> {});
    }

    private static void server(java.util.function.Consumer<MinecraftServer> action) {
        var server = Minecraft.getInstance().getSingleplayerServer();
        server.execute(() -> {
            try { action.accept(server); } catch (Throwable error) { serverFailure = error; }
        });
    }

    private static void command(MinecraftServer server, String command) {
        execute(server, "poptartcore eclipse " + command);
    }

    private static void execute(MinecraftServer server, String command) {
        try {
            server.getCommands().getDispatcher().execute(command,
                    server.createCommandSourceStack().withPermission(4).withSuppressedOutput());
        } catch (Exception error) { throw new AssertionError("Eclipse command failed", error); }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
