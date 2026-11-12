package dev.poptartking.poptartcore.eclipse;

import com.mojang.authlib.GameProfile;
import dev.poptartking.poptartcore.PoptartCore;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EclipseLightingGameTests {
    @GameTest(template = "millstone_test", timeoutTicks = 80)
    public static void gameplayDaylightControlsSunburnAndSpawnChecksWithoutChangingBlockLight(GameTestHelper h) {
        var lamp = new BlockPos(2, 2, 2);
        h.setBlock(lamp, Blocks.GLOWSTONE);
        h.runAfterDelay(10, () -> {
            var level = h.getLevel();
            var data = EclipseData.get(level);
            var prior = new Snapshot(level);
            var pos = h.absolutePos(lamp).atY(level.getMaxBuildHeight() - 4);
            var lampPos = h.absolutePos(lamp);
            try {
                level.setDayTime(6000);
                level.setRainLevel(0);
                level.setThunderLevel(0);
                command(level, "stop");
                level.updateSkyBrightness();
                h.assertTrue(
                        level.isDay() && level.getMaxLocalRawBrightness(pos) == 15,
                        "The outdoor fixture has full natural daylight");
                int lampBrightness = level.getMaxLocalRawBrightness(lampPos);
                h.assertTrue(lampBrightness >= 14, "The fixture's block lighting has settled");
                var zombie = new SunburnProbe(level);
                zombie.setPos(Vec3.atBottomCenterOf(pos));
                h.assertTrue(
                        burnChecks(zombie) > 0 && spawnChecks(level, pos) == 0,
                        "Normal noon triggers vanilla sunburn and rejects light-sensitive monster spawns");

                command(level, "phase waning_gibbous");
                h.assertTrue(level.getSkyDarken() == 2 && level.isDay(), "A slight eclipse is still daylight");
                command(level, "phase waning_half");
                h.assertTrue(level.getSkyDarken() == 5 && !level.isDay(), "Half coverage darkens gameplay daylight");
                command(level, "phase total");
                h.assertTrue(
                        level.getSkyDarken() == 13 && level.getMaxLocalRawBrightness(pos) == 2,
                        "Totality affects the actual effective light used by gameplay");
                h.assertTrue(
                        burnChecks(zombie) == 0 && spawnChecks(level, pos) > 0,
                        "The same vanilla sunburn and spawn predicates respond to totality");
                h.assertTrue(
                        level.getBrightness(LightLayer.SKY, pos) == 15
                                && level.getMaxLocalRawBrightness(lampPos) == lampBrightness
                                && spawnChecks(level, lampPos) == 0,
                        "Stored sky light and block light remain intact, and lit areas reject spawns");

                for (var dimension : java.util.List.of(Level.NETHER, Level.END)) {
                    var other = level.getServer().getLevel(dimension);
                    other.updateSkyBrightness();
                    h.assertTrue(
                            other.getSkyDarken() == ((EclipseDaylight) other).poptartcore$skyDarkenWithoutEclipse(),
                            "Other dimensions keep their original daylight");
                }
                command(level, "stop");
                h.assertTrue(
                        level.isDay() && level.getMaxLocalRawBrightness(pos) == 15,
                        "Stopping immediately restores daytime gameplay lighting");
                level.setDayTime(18000);
                level.updateSkyBrightness();
                int night = level.getSkyDarken();
                command(level, "phase total");
                h.assertTrue(
                        level.getSkyDarken() == night && night == 11,
                        "Totality does not darken an already nighttime sky");
                h.assertTrue(level.getDayTime() == 18000, "Changing phases never moves the world clock");
            } finally {
                prior.restore(level, data);
            }
            h.succeed();
        });
    }

    @GameTest(template = "millstone_test", timeoutTicks = 60)
    public static void bedsKeepNormalHoursDuringAnEclipse(GameTestHelper h) {
        var level = h.getLevel();
        var data = EclipseData.get(level);
        var prior = new Snapshot(level);
        var foot = h.absolutePos(new BlockPos(2, 2, 2));
        var head = foot.south();
        level.setBlock(foot.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(head.below(), Blocks.STONE.defaultBlockState(), 3);
        var bed = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH);
        level.setBlock(foot, bed.setValue(BedBlock.PART, BedPart.FOOT), 3);
        level.setBlock(head, bed.setValue(BedBlock.PART, BedPart.HEAD), 3);
        level.setBlock(foot.above(), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(head.above(), Blocks.AIR.defaultBlockState(), 3);
        var player = player(h, foot);
        try {
            level.setDayTime(6000);
            level.setRainLevel(0);
            level.setThunderLevel(0);
            command(level, "phase total");
            level.updateSkyBrightness();
            h.assertTrue(
                    player.startSleepInBed(foot).left().orElse(null) == BedSleepingProblem.NOT_POSSIBLE_NOW,
                    "Dark totality does not unlock daytime sleeping");
            level.setDayTime(18000);
            level.updateSkyBrightness();
            h.assertTrue(
                    player.startSleepInBed(foot).right().isPresent() && player.isSleeping(),
                    "The held eclipse phase does not block ordinary nighttime sleeping");
            level.setDayTime(6000);
            level.updateSkyBrightness();
            player.doTick();
            h.assertTrue(!player.isSleeping(), "Players wake at normal daylight even when totality looks like night");

            level.setRainLevel(1);
            level.setThunderLevel(1);
            command(level, "phase waning_crescent");
            player.setPos(Vec3.atBottomCenterOf(foot));
            h.assertTrue(
                    level.getRainLevel(1) == 0 && level.getThunderLevel(1) == 0,
                    "A covered eclipse clears an existing storm");
            h.assertTrue(
                    player.startSleepInBed(foot).left().orElse(null) == BedSleepingProblem.NOT_POSSIBLE_NOW,
                    "The cleared storm no longer permits daytime sleep");

            command(level, "stop");
            level.setRainLevel(1);
            level.setThunderLevel(1);
            level.updateSkyBrightness();
            h.assertTrue(
                    !EclipseLighting.keepsNormalBedtime(level)
                            && player.startSleepInBed(foot).right().isPresent(),
                    "Normal weather-based daytime sleeping remains available");
        } finally {
            if (player.isSleeping()) player.stopSleepInBed(true, false);
            prior.restore(level, data);
        }
        h.succeed();
    }

    private static int burnChecks(SunburnProbe zombie) {
        int count = 0;
        for (int i = 0; i < 256; i++) if (zombie.sunBurns()) count++;
        return count;
    }

    private static int spawnChecks(ServerLevel level, BlockPos pos) {
        var random = RandomSource.create(42);
        int count = 0;
        for (int i = 0; i < 256; i++) if (Monster.isDarkEnoughToSpawn(level, pos, random)) count++;
        return count;
    }

    private static void command(ServerLevel level, String command) {
        try {
            level.getServer()
                    .getCommands()
                    .getDispatcher()
                    .execute(
                            "poptartcore eclipse " + command,
                            level.getServer()
                                    .createCommandSourceStack()
                                    .withPermission(4)
                                    .withSuppressedOutput());
        } catch (Exception error) {
            throw new AssertionError(error);
        }
    }

    private static ServerPlayer player(GameTestHelper h, BlockPos pos) {
        var server = h.getLevel().getServer();
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "eclipse-bed-test"), false);
        var player = new ServerPlayer(server, h.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), player, cookie) {
            @Override
            public void send(Packet<?> packet) {}
        };
        player.setGameMode(GameType.CREATIVE);
        player.setPos(Vec3.atBottomCenterOf(pos));
        return player;
    }

    private record Snapshot(EclipseState state, long time, float rain, float thunder) {
        Snapshot(ServerLevel level) {
            this(EclipseData.get(level).state(), level.getDayTime(), level.getRainLevel(1), level.getThunderLevel(1));
        }

        void restore(ServerLevel level, EclipseData data) {
            data.setState(state);
            level.setDayTime(time);
            level.setRainLevel(rain);
            level.setThunderLevel(thunder);
            EclipseEvents.sync(level.getServer());
            level.updateSkyBrightness();
        }
    }

    private static final class SunburnProbe extends Zombie {
        SunburnProbe(ServerLevel level) {
            super(EntityType.ZOMBIE, level);
        }

        boolean sunBurns() {
            return isSunBurnTick();
        }
    }
}
