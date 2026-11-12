package dev.poptartking.poptartcore.lunar;

import com.mojang.authlib.GameProfile;
import dev.poptartking.poptartcore.PoptartCore;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BloodMoonGameTests {
    @GameTest(template = "millstone_test")
    public static void naturalBloodMoonRespectsNightBoundariesAndSavedRolls(GameTestHelper h) {
        var data = new BloodMoonData();
        var roll = new NightRoll();
        h.assertFalse(data.update(12_499, roll), "Daylight never rolls an event");
        h.assertTrue(data.update(12_500, roll) && roll.calls == 1, "Dusk rolls once and starts the selected event");
        h.assertTrue(data.isBloodMoon(23_999) && !data.isBloodMoon(24_000), "The event ends at dawn");
        h.assertFalse(data.claimSiegeRoll(17_999), "The siege is not rolled before midnight");
        h.assertTrue(data.claimSiegeRoll(18_000), "Midnight permits one siege roll");

        var registries = h.getLevel().registryAccess();
        var saved = BloodMoonData.load(data.save(new CompoundTag(), registries), registries);
        h.assertTrue(
                saved.isBloodMoon(18_000) && !saved.claimSiegeRoll(18_000), "Reload keeps the event and siege result");
        h.assertFalse(saved.update(18_000, roll), "A frozen clock cannot choose a second event");
        for (int day = 1; day <= 4; day++) {
            h.assertFalse(saved.update(day * 24_000L + 12_500, roll), "Four nights stay clear between events");
        }
        h.assertTrue(roll.calls == 1, "Cooldown nights do not roll");
        long nextNight = 5 * 24_000L + 12_500;
        h.assertTrue(saved.update(nextNight, roll) && roll.calls == 2, "The fifth night may select a Blood Moon");
        saved.update(6 * 24_000L, roll);
        saved.update(12_500, roll);
        h.assertFalse(saved.isBloodMoon(12_500), "A rewind after dawn keeps the previous event in the past");
        saved.setNatural(false, 12_500);
        var disabled = BloodMoonData.load(saved.save(new CompoundTag(), registries), registries);
        h.assertFalse(disabled.natural() || disabled.update(nextNight, roll), "Natural off persists after reload");
        h.succeed();
    }

    @GameTest(template = "millstone_test")
    public static void bloodMoonCommandsAffectVanillaSleepSpawnRangeAndSiege(GameTestHelper h) throws Exception {
        var level = h.getLevel();
        var server = level.getServer();
        var original = BloodMoonData.get(level);
        var data = new BloodMoonData();
        level.getDataStorage().set(BloodMoonData.FILE_ID, data);
        var source = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        var commands = server.getCommands().getDispatcher();
        long oldTime = level.getDayTime();
        float oldRain = level.getRainLevel(1);
        var foot = h.absolutePos(new BlockPos(1, 2, 1));
        var head = foot.south();
        var bed = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH);
        level.setBlock(foot.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(head.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(foot, bed.setValue(BedBlock.PART, BedPart.FOOT), 3);
        level.setBlock(head, bed.setValue(BedBlock.PART, BedPart.HEAD), 3);
        level.setBlock(foot.above(), Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(head.above(), Blocks.AIR.defaultBlockState(), 3);
        var profile = new GameProfile(UUID.randomUUID(), "BloodMoonTest");
        var player = new ServerPlayer(server, level, profile, ClientInformation.createDefault());
        var cookie = CommonListenerCookie.createInitial(profile, false);
        new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), player, cookie) {
            @Override
            public void send(Packet<?> packet) {}
        };
        player.setPos(Vec3.atBottomCenterOf(foot));
        var zombie = EntityType.ZOMBIE.create(level);
        zombie.setPos(Vec3.atBottomCenterOf(foot.east()));
        var canSpawn = NaturalSpawner.class.getDeclaredMethod(
                "isValidPositionForMob", net.minecraft.server.level.ServerLevel.class, Mob.class, double.class);
        canSpawn.setAccessible(true);
        try {
            level.setRainLevel(0);
            level.setDayTime(6_000);
            commands.execute("poptartcore blood_moon natural off", source);
            commands.execute("poptartcore blood_moon start", source);
            h.assertTrue(
                    data.scheduled(6_000) && level.getDayTime() == 6_000, "Start queues tonight without changing time");
            h.assertFalse(BloodMoonData.active(level), "A scheduled Blood Moon does not affect daylight");
            level.setDayTime(18_002);
            level.updateSkyBrightness();
            h.assertTrue(BloodMoonData.active(level), "Tonight's effects are active at midnight");
            h.assertTrue(
                    player.startSleepInBed(foot).left().orElse(null) == BedSleepingProblem.OTHER_PROBLEM,
                    "The actual player sleep path rejects Blood Moon sleep");
            h.assertTrue(foot.equals(player.getRespawnPosition()), "Beds can still set respawn");
            h.assertFalse(
                    (boolean) canSpawn.invoke(null, level, zombie, 80 * 80.0),
                    "Natural monsters cannot spawn 80 blocks away");
            new VillageSiege().tick(level, true, true);
            h.assertFalse(
                    data.claimSiegeRoll(level.getDayTime()), "The real siege tick rolls once even past exact midnight");
            h.assertFalse(
                    BloodMoonData.active(server.getLevel(Level.NETHER)), "Other dimensions keep their normal rules");
            level.setRainLevel(1);
            h.assertFalse(BloodMoonData.active(level), "Rain hides the Blood Moon and pauses its gameplay effects");
            level.setRainLevel(0);
            commands.execute("poptartcore blood_moon stop", source);
            h.assertFalse(BloodMoonData.active(level), "Stop clears the event for this night");
            h.assertTrue(
                    (boolean) canSpawn.invoke(null, level, zombie, 80 * 80.0),
                    "Ordinary nights restore vanilla spawning distance");
            h.assertTrue(player.startSleepInBed(foot).right().isPresent(), "Ordinary nights restore vanilla sleep");
        } finally {
            if (player.isSleeping()) player.stopSleepInBed(true, false);
            level.setDayTime(oldTime);
            level.setRainLevel(oldRain);
            level.getDataStorage().set(BloodMoonData.FILE_ID, original);
            level.updateSkyBrightness();
        }
        h.succeed();
    }

    private static final class NightRoll extends LegacyRandomSource {
        private int calls;

        private NightRoll() {
            super(0);
        }

        @Override
        public int nextInt(int bound) {
            if (bound != 110) throw new AssertionError("Expected the standard night and Blood Moon weights");
            calls++;
            return 0;
        }
    }
}
