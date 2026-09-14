package dev.poptartking.poptartcore.spider;

import dev.poptartking.poptartcore.PoptartCore;
import dev.poptartking.poptartcore.registry.PoptartCoreBlocks;
import dev.poptartking.poptartcore.registry.PoptartCoreEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(PoptartCore.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WebProjectileGameTests {
    private WebProjectileGameTests() {}

    @GameTest(template = "millstone_test", timeoutTicks = 20)
    public static void deniedShooterCannotPlaceWebOrTriggerMelee(GameTestHelper helper) {
        var shooter = helper.spawn(net.minecraft.world.entity.EntityType.SPIDER, new BlockPos(1, 2, 1));
        var victim = helper.spawn(net.minecraft.world.entity.EntityType.PIG, new BlockPos(1, 2, 6));
        shooter.setNoAi(true);
        shooter.setTarget(victim);
        boolean[] consulted = {false};
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityMobGriefingEvent> deny = event -> {
            if (event.getEntity() == shooter) {
                consulted[0] = true;
                event.setCanGrief(false);
            }
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(deny);
        try {
            WebProjectile shot = new WebProjectile(helper.getLevel(), shooter);
            shot.onHitEntity(new net.minecraft.world.phys.EntityHitResult(victim));
            helper.assertTrue(consulted[0], "Permission hook did not receive shooter");
            helper.assertBlockPresent(Blocks.AIR, new BlockPos(1, 2, 6));
            helper.assertTrue(
                    !((WebShootingSpider) shooter).poptartcore$isPursuingWebbedTarget(victim),
                    "Denied web triggered melee alert");
            helper.assertTrue(shot.isRemoved(), "Denied projectile remained alive");
        } finally {
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(deny);
        }
        helper.succeed();
    }

    @GameTest(template = "millstone_test", timeoutTicks = 120)
    public static void projectilePlacesExpiringWebAboveSolidBlock(GameTestHelper helper) {
        BlockPos floor = new BlockPos(2, 1, 2);
        helper.setBlock(floor, Blocks.STONE);
        helper.setBlock(floor.above(), Blocks.AIR);
        WebProjectile projectile = helper.spawn(PoptartCoreEntities.WEB_PROJECTILE.get(), new BlockPos(2, 4, 2));
        helper.runAtTickTime(30, () -> {
            helper.assertBlockPresent(PoptartCoreBlocks.TEMPORARY_COBWEB.get(), floor.above());
            helper.assertBlockPresent(Blocks.STONE, floor);
            helper.assertTrue(projectile.isRemoved(), "Projectile remained after impact");
        });
        helper.runAtTickTime(115, () -> {
            helper.assertBlockPresent(Blocks.AIR, floor.above());
            helper.succeed();
        });
    }

    @GameTest(template = "millstone_test", timeoutTicks = 20)
    public static void projectileCannotReplaceSolidImpactSpace(GameTestHelper helper) {
        BlockPos floor = new BlockPos(2, 1, 2);
        helper.setBlock(floor, Blocks.STONE);
        helper.setBlock(floor.above(), Blocks.STONE);
        WebProjectile projectile = new WebProjectile(PoptartCoreEntities.WEB_PROJECTILE.get(), helper.getLevel());
        BlockPos absolute = helper.absolutePos(floor);
        projectile.onHitBlock(new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
        helper.assertBlockPresent(Blocks.STONE, floor);
        helper.assertBlockPresent(Blocks.STONE, floor.above());
        helper.assertTrue(projectile.isRemoved(), "Blocked projectile did not disappear");
        helper.succeed();
    }
}
