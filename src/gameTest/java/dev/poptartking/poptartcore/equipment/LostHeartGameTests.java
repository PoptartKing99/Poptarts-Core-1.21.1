package dev.poptartking.poptartcore.equipment;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.lostheart.Hearts;
import dev.poptartking.poptartcore.registry.PoptartCoreEntities;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LostHeartGameTests {
    private LostHeartGameTests() {}

    @GameTest(template = "millstone_test", timeoutTicks = 400)
    public static void spacedPunchesKeepProgressAndRestoreExactlyOneHeart(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        Hearts.set(player, 9);
        var heart = PoptartCoreEntities.LOST_HEART.get().create(helper.getLevel());
        CompoundTag data = new CompoundTag();
        data.putUUID("owner", player.getUUID());
        data.putInt("token", 0);
        heart.load(data);
        heart.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(4, 2, 4)));
        helper.getLevel().addFreshEntity(heart);
        heart.hurt(player.damageSources().playerAttack(player), 1);
        helper.assertTrue(heart.cracks() == 1, "First punch must advance heart stage");
        for (int stage = 2; stage <= 4; stage++) {
            int expected = stage;
            helper.runAtTickTime((stage - 1) * 100, () -> {
                helper.assertTrue(heart.cracks() == expected - 1, "Waiting five seconds must not erase punch progress");
                heart.hurt(player.damageSources().playerAttack(player), 1);
                if (expected < 4) {
                    helper.assertTrue(heart.cracks() == expected, "Spaced punch must advance stage");
                } else {
                    helper.assertTrue(
                            heart.isRemoved() && Hearts.get(player) == 10, "Fourth punch must restore the lost heart");
                    heart.hurt(player.damageSources().playerAttack(player), 1);
                    helper.assertTrue(Hearts.get(player) == 10, "Removed heart must not grant additional health");
                    helper.succeed();
                }
            });
        }
    }
}
